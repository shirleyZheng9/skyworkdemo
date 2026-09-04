package com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.convertor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwhalecloud.bote.entity.loop.evaluation.ExperimentEntity;
import com.iwhalecloud.bote.entity.loop.evaluation.ExptEvaluatorRefEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.CreditCost;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationConfiguration;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Experiment;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptEvaluatorVersionRef;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptStatus;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.SourceType;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 实验转换器
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/mysql/convert/expt.go
 * - 功能: 实验DO和PO之间的转换
 * - 转换方向: DO ↔ PO
 * - 特殊处理: JSON序列化/反序列化、指针类型转换
 * <p>
 * Java实现说明:
 * - 对应Go的ExptConverter结构体
 * - 处理复杂的JSON字段转换
 * - 支持评估器引用列表转换
 * <p>
 * 技术栈迁移:
 * - Go func DO2PO -> Java convertToPO
 * - Go func PO2DO -> Java convertToDO
 * - Go json.Marshal -> Java ObjectMapper.writeValueAsString
 * - Go json.Unmarshal -> Java ObjectMapper.readValue
 * - Go gptr.Of -> Java 包装类型赋值
 * - Go gptr.Indirect -> Java 包装类型取值
 */
public final class ExptConvertor {

  private ExptConvertor() {
    // 工具类，禁止实例化
  }

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * 将DO转换为PO
   * 迁移对应关系: Go语言DO2PO
   *
   * @param experiment 领域对象
   * @return 持久化对象
   * @throws BssException 转换失败时抛出
   */
  public static ExperimentEntity convertToPO(Experiment experiment) {
    if (experiment == null) {
      return null;
    }

    try {
      ExperimentEntity.ExperimentEntityBuilder builder = buildBasicExperimentEntity(experiment);
      setStatusMessage(builder, experiment);
      setMaxAliveTime(builder, experiment);
      setEvalConf(builder, experiment);

      return builder.build();
    }
    catch (JsonProcessingException e) {
      throw new BssException("EvaluationConfiguration JSON序列化失败: " + e.getMessage(), e);
    }
  }

  private static ExperimentEntity.ExperimentEntityBuilder buildBasicExperimentEntity(Experiment experiment) {
    Date now = new Date();
    return ExperimentEntity.builder()
      .id(experiment.getId())
      .spaceId(experiment.getSpaceId())
      .createdBy(experiment.getCreatedBy())
      .name(experiment.getName())
      .description(experiment.getDescription())
      .evalSetVersionId(experiment.getEvalSetVersionId())
      .evalSetId(experiment.getEvalSetId())
      .targetVersionId(experiment.getTargetVersionId())
      .targetType(convertTargetType(experiment.getTargetType()))
      .targetId(experiment.getTargetId())
      .status(convertStatus(experiment.getStatus()))
      .statusMessage(experiment.getStatusMessage())
      .startAt(experiment.getStartAt())
      .endAt(experiment.getEndAt())
      .latestRunId(experiment.getLatestRunId())
      .creditCost(convertCreditCost(experiment.getCreditCost()))
      .sourceType(convertSourceType(experiment.getSourceType()))
      .sourceId(experiment.getSourceId())
      .exptType(convertExptType(experiment.getExptType()))
      .catalogItemId(experiment.getCatalogItemId())
      .createdAt(now)  // 设置创建时间为当前时间
      .updatedAt(now)  // 设置更新时间为当前时间
      .deletedAt(0L);  // 设置删除时间为0（表示未删除）
  }

  private static Long convertTargetType(EvalTargetType targetType) {
    return targetType != null ? (long) targetType.getValue() : null;
  }

  private static Integer convertStatus(ExptStatus status) {
    return status != null ? status.getValue().intValue() : null;
  }

  private static Integer convertCreditCost(CreditCost creditCost) {
    return creditCost != null ? creditCost.getValue() : null;
  }

  private static Integer convertSourceType(SourceType sourceType) {
    return sourceType != null ? sourceType.getValue() : null;
  }

  private static Integer convertExptType(ExptType exptType) {
    return exptType != null ? exptType.getValue() : null;
  }

  private static void setStatusMessage(ExperimentEntity.ExperimentEntityBuilder builder, Experiment experiment) {
    if (experiment.getStatusMessage() != null) {
      builder.statusMessage(experiment.getStatusMessage());
    }
  }

  private static void setMaxAliveTime(ExperimentEntity.ExperimentEntityBuilder builder, Experiment experiment) {
    if (experiment.getMaxAliveTime() != null && experiment.getMaxAliveTime() != 0) {
      builder.maxAliveTime(experiment.getMaxAliveTime());
    }
  }

  private static void setEvalConf(ExperimentEntity.ExperimentEntityBuilder builder, Experiment experiment) throws JsonProcessingException {
    if (experiment.getEvalConf() != null) {
      String evalConfJson = objectMapper.writeValueAsString(experiment.getEvalConf());
      builder.evalConf(evalConfJson);
    }
  }

  /**
   * 将PO转换为DO
   * 迁移对应关系: Go语言PO2DO
   *
   * @param exptEntity 持久化对象
   * @param refs 评估器引用列表
   * @return 领域对象
   * @throws BssException 转换失败时抛出
   */
  public static Experiment convertToDO(ExperimentEntity exptEntity, List<ExptEvaluatorRefEntity> refs) {
    if (exptEntity == null) {
      return null;
    }

    try {
      EvaluationConfiguration evalConf = deserializeEvalConf(exptEntity);
      List<ExptEvaluatorVersionRef> evaluatorVersionRefs = convertEvaluatorRefs(refs);

      return buildExperimentFromEntity(exptEntity, evaluatorVersionRefs, evalConf);
    }
    catch (JsonProcessingException e) {
      throw new BssException("EvaluationConfiguration JSON反序列化失败, expt_id: " + exptEntity.getId() + ", raw: " +
        (exptEntity.getEvalConf() != null ? exptEntity.getEvalConf() : "null"), e);
    }
  }

  private static EvaluationConfiguration deserializeEvalConf(ExperimentEntity exptEntity) throws JsonProcessingException {
    EvaluationConfiguration evalConf = new EvaluationConfiguration();
    if (exptEntity.getEvalConf() != null && !exptEntity.getEvalConf().isEmpty()) {
      evalConf = objectMapper.readValue(exptEntity.getEvalConf(), EvaluationConfiguration.class);
    }
    return evalConf;
  }

  private static List<ExptEvaluatorVersionRef> convertEvaluatorRefs(List<ExptEvaluatorRefEntity> refs) {
    List<ExptEvaluatorVersionRef> evaluatorVersionRefs = new ArrayList<>();
    if (refs != null) {
      for (ExptEvaluatorRefEntity ref : refs) {
        evaluatorVersionRefs.add(ExptEvaluatorVersionRef.builder()
          .evaluatorVersionId(ref.getEvaluatorVersionId())
          .evaluatorId(ref.getEvaluatorId())
          .build());
      }
    }
    return evaluatorVersionRefs;
  }

  private static Experiment buildExperimentFromEntity(ExperimentEntity exptEntity,
                                                      List<ExptEvaluatorVersionRef> evaluatorVersionRefs, EvaluationConfiguration evalConf) {
    return Experiment.builder()
      .id(exptEntity.getId())
      .spaceId(exptEntity.getSpaceId())
      .createdBy(exptEntity.getCreatedBy())
      .name(exptEntity.getName())
      .description(exptEntity.getDescription())
      .evalSetVersionId(exptEntity.getEvalSetVersionId())
      .evalSetId(exptEntity.getEvalSetId())
      .targetVersionId(exptEntity.getTargetVersionId())
      .targetType(convertTargetTypeFromEntity(exptEntity.getTargetType()))
      .targetId(exptEntity.getTargetId())
      .evaluatorVersionRef(evaluatorVersionRefs)
      .evalConf(evalConf)
      .status(convertStatusFromEntity(exptEntity.getStatus()))
      .statusMessage(exptEntity.getStatusMessage())
      .latestRunId(exptEntity.getLatestRunId())
      .creditCost(convertCreditCostFromEntity(exptEntity.getCreditCost()))
      .startAt(exptEntity.getStartAt())
      .endAt(exptEntity.getEndAt())
      .sourceType(convertSourceTypeFromEntity(exptEntity.getSourceType()))
      .sourceId(exptEntity.getSourceId())
      .exptType(convertExptTypeFromEntity(exptEntity.getExptType()))
      .maxAliveTime(exptEntity.getMaxAliveTime())
      .catalogItemId(exptEntity.getCatalogItemId())
      .build();
  }

  private static EvalTargetType convertTargetTypeFromEntity(Long targetType) {
    return targetType != null ? EvalTargetType.fromValue(targetType.intValue()) : null;
  }

  private static ExptStatus convertStatusFromEntity(Integer status) {
    return status != null ? ExptStatus.fromValue((long) status) : null;
  }

  private static CreditCost convertCreditCostFromEntity(Integer creditCost) {
    return creditCost != null ? CreditCost.fromValue(creditCost) : null;
  }

  private static SourceType convertSourceTypeFromEntity(Integer sourceType) {
    return sourceType != null ? SourceType.fromValue(sourceType) : null;
  }

  private static ExptType convertExptTypeFromEntity(Integer exptType) {
    return exptType != null ? ExptType.fromValue(exptType) : null;
  }
}
