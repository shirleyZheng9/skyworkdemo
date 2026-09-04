package com.iwhalecloud.bote.loop.evaluation.application.convertor.experiment;

import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.EvaluatorAggregateResultDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.EvaluatorFieldMappingDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ExperimentDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ExptAggregateResultDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ExptStatisticsDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ExptStatsInfoDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ExptStatusDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ExptTypeDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.FieldMappingDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.SourceTypeDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.TargetFieldMappingDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.TokenUsageDTO;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.CreateEvalTargetParamDTO;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.CreateExperimentRequest;
import com.iwhalecloud.bote.loop.evaluation.application.convertor.evaltarget.EvalTargetConvertor;
import com.iwhalecloud.bote.loop.evaluation.application.convertor.evaluationset.EvaluationSetApplicationConvertor;
import com.iwhalecloud.bote.loop.evaluation.application.convertor.evaluator.EvaluatorApplicationConvertor;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.BotInfoType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Connector;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.CreateEvalTargetParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.CreateExptParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationConfiguration;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationConfigurationResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorConf;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorIngressConf;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorsConf;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Experiment;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptAggregateResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptEvaluatorVersionRef;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptRunMode;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptStats;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.FieldAdapter;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.FieldConf;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.SourceType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.TargetConf;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.TargetIngressConf;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.util.CollectionUtils;

/**
 * 实验转换器
 * 对应Go: experiment/expt.go
 */
public final class ExperimentApplicationConvertor {
  private ExperimentApplicationConvertor() {
  }

  /**
   * 实验统计信息转DTO
   * 对应Go: ToExptStatsInfoDTO
   */
  public static ExptStatsInfoDTO toExptStatsInfoDTO(Experiment experiment, ExptStats stats) {
    if (stats == null) {
      return null;
    }
    return ExptStatsInfoDTO.builder()
      .exptId(experiment.getId())
      .sourceId(experiment.getSourceId())
      .exptStats(toExptStatsDTO(stats, null))
      .build();
  }

  /**
   * 实验列表转DTO
   * 对应Go: ToExptDTOs
   */
  public static List<ExperimentDTO> toExptDTOs(List<Experiment> experiments) {
    if (CollectionUtils.isEmpty(experiments)) {
      return new ArrayList<>();
    }
    return experiments.stream()
      .map(ExperimentApplicationConvertor::toExptDTO)
      .collect(Collectors.toList());
  }

  /**
   * 实验转DTO
   * 对应Go: ToExptDTO
   */
  public static ExperimentDTO toExptDTO(Experiment experiment) {
    if (experiment == null) {
      return null;
    }
    List<Long> evaluatorVersionIds = experiment.getEvaluatorVersionRef().stream()
      .map(ExptEvaluatorVersionRef::getEvaluatorVersionId)
      .collect(Collectors.toList());
    EvaluationConfigurationResult configResult = EvalConfConvertor.convertEntityToDTO(experiment.getEvalConf());
    ExperimentDTO.ExperimentDTOBuilder builder = ExperimentDTO.builder()
      .id(experiment.getId())
      .name(experiment.getName())
      .desc(experiment.getDescription())
      .creatorBy(experiment.getCreatedBy())
      .evalSetVersionId(experiment.getEvalSetVersionId())
      .targetVersionId(experiment.getTargetVersionId())
      .evalSetId(experiment.getEvalSetId())
      .targetId(experiment.getTargetId())
      .evaluatorVersionIds(evaluatorVersionIds)
      .status(ExptStatusDTO.fromValue(experiment.getStatus().getValue().intValue()))
      .statusMessage(experiment.getStatusMessage())
      .exptStats(toExptStatsDTO(experiment.getStats(), experiment.getAggregateResult()))
      .targetFieldMapping(configResult.getTargetMapping())
      .evaluatorFieldMapping(configResult.getEvaluatorMappings())
      .sourceType(SourceTypeDTO.fromValue(experiment.getSourceType().getValue()))
      .sourceId(experiment.getSourceId())
      .exptType(ExptTypeDTO.fromValue(experiment.getExptType().getValue()))
      .maxAliveTime(experiment.getMaxAliveTime())
      .catalogItemId(experiment.getCatalogItemId());
    if (experiment.getStartAt() != null) {
      builder.startTime(experiment.getStartAt().getTime());
    }
    if (experiment.getEndAt() != null) {
      builder.endTime(experiment.getEndAt().getTime());
    }
    ExperimentDTO res = builder.build();
    res.setEvalTarget(EvalTargetConvertor.convertDO2DTO(experiment.getTarget()));
    res.setEvalSet(EvaluationSetApplicationConvertor.convertDO2DTO(experiment.getEvalSet()));
    if (experiment.getEvaluators() != null) {
      res.setEvaluators(experiment.getEvaluators().stream()
        .map(EvaluatorApplicationConvertor::convertDO2DTO)
        .collect(Collectors.toList()));
    }
    else {
      res.setEvaluators(new ArrayList<>());
    }
    return res;
  }

  /**
   * 实验统计转DTO
   * 对应Go: ToExptStatsDTO
   */
  public static ExptStatisticsDTO toExptStatsDTO(ExptStats stats, ExptAggregateResult aggrResult) {
    if (stats == null) {
      return null;
    }
    ExptStatisticsDTO.ExptStatisticsDTOBuilder builder = ExptStatisticsDTO.builder()
      .pendingTurnCnt(stats.getPendingItemCnt() > 0 ? stats.getPendingItemCnt() : 0)
      .successTurnCnt(stats.getSuccessItemCnt() > 0 ? stats.getSuccessItemCnt() : 0)
      .failTurnCnt(stats.getFailItemCnt() > 0 ? stats.getFailItemCnt() : 0)
      .processingTurnCnt(stats.getProcessingItemCnt() > 0 ? stats.getProcessingItemCnt() : 0)
      .terminatedTurnCnt(stats.getTerminatedItemCnt() > 0 ? stats.getTerminatedItemCnt() : 0)
      .creditCost(stats.getCreditCost())
      .tokenUsage(TokenUsageDTO.builder()
        .inputTokens(stats.getInputTokenCost())
        .outputTokens(stats.getOutputTokenCost())
        .build());
    if (aggrResult != null) {
      ExptAggregateResultDTO aggrResultDTO = ExperimentAggrResultApplicationConvertor.convertDOToDTO(aggrResult);
      List<EvaluatorAggregateResultDTO> evaluatorAggregateResults = new ArrayList<>(aggrResultDTO.getEvaluatorResults().values());
      builder.evaluatorAggregateResults(evaluatorAggregateResults);
    }
    return builder.build();
  }

  /**
   * 创建评估目标参数DTO转DO
   * 对应Go: CreateEvalTargetParamDTO2DO
   */
  public static CreateEvalTargetParam convertCreateEvalTargetParamDTO2DO(CreateEvalTargetParamDTO param) {
    if (param == null) {
      return null;
    }
    CreateEvalTargetParam.CreateEvalTargetParamBuilder builder = CreateEvalTargetParam.builder()
      .sourceTargetId(param.getSourceTargetId())
      .sourceTargetVersion(param.getSourceTargetVersion())
      .botPublishVersion(param.getBotPublishVersion());
    if (param.getEvalTargetType() != null) {
      builder.evalTargetType(EvalTargetType.fromValue(param.getEvalTargetType().getValue()));
    }
    if (param.getBotInfoType() != null) {
      builder.botInfoType(BotInfoType.fromValue(param.getBotInfoType().getValue()));
    }
    return builder.build();
  }

  /**
   * 实验类型转评估模式
   * 对应Go: ExptType2EvalMode
   */
  public static ExptRunMode exptType2EvalMode(ExptTypeDTO exptType) {
    ExptRunMode exptMode = ExptRunMode.SUBMIT;
    if (exptType == ExptTypeDTO.ONLINE) {
      exptMode = ExptRunMode.APPEND;
    }
    return exptMode;
  }

  /**
   * 转换创建请求
   * 对应Go: ConvertCreateReq
   */
  public static CreateExptParam convertCreateReq(CreateExperimentRequest cer) {
    CreateExptParam param = CreateExptParam.builder()
      .workspaceId(cer.getWorkspaceId())
      .evalSetVersionId(cer.getEvalSetVersionId())
      .targetVersionId(cer.getTargetVersionId())
      .evaluatorVersionIds(cer.getEvaluatorVersionIds())
      .name(cer.getName())
      .desc(cer.getDesc())
      .evalSetId(cer.getEvalSetId())
      .targetId(cer.getTargetId())
      .createEvalTargetParam(convertCreateEvalTargetParamDTO2DO(cer.getCreateEvalTargetParam()))
      .exptType(ExptType.fromValue(cer.getExptType() == null ? ExptTypeDTO.ONLINE.getValue() : cer.getExptType().getValue()))
      .maxAliveTime(cer.getMaxAliveTime())
      .sourceType(SourceType.fromValue(cer.getSourceType() == null ? SourceTypeDTO.EVALUATION.getValue() : cer.getSourceType().getValue()))
      .sourceId(cer.getSourceId())
      .catalogItemId(cer.getCatalogItemId())
      .build();
    EvaluationConfiguration evaluationConfiguration = EvalConfConvertor.convertToEntity(cer);
    param.setExptConf(evaluationConfiguration);
    return param;
  }

  /**
   * 评估配置转换器
   * 对应Go: EvalConfConvert
   */
  public static final class EvalConfConvertor {
    private EvalConfConvertor() {
    }

    /**
     * 转换为实体
     * 对应Go: ConvertToEntity
     */
    public static EvaluationConfiguration convertToEntity(CreateExperimentRequest cer) {
      if (cer == null || cer.getTargetFieldMapping() == null || cer.getEvaluatorFieldMapping() == null) {
        throw new IllegalArgumentException("invalid EvaluationConfiguration");
      }
      return EvaluationConfiguration.builder()
        .connectorConf(Connector.builder()
          .targetConf(TargetConf.builder()
            .targetVersionId(cer.getTargetVersionId())
            .ingressConf(convertToTargetFieldMappingDO(cer.getTargetFieldMapping()))
            .build())
          .evaluatorsConf(EvaluatorsConf.builder()
            .evaluatorConcurNum(cer.getEvaluatorsConcurNum())
            .evaluatorConf(convertToEvaluatorFieldMappingDO(cer.getEvaluatorFieldMapping()))
            .build())
          .build())
        .itemConcurNum(cer.getItemConcurNum())
        .build();
    }

    /**
     * 实体转DTO
     * 对应Go: ConvertEntityToDTO
     */
    public static EvaluationConfigurationResult convertEntityToDTO(EvaluationConfiguration ec) {
      if (ec == null) {
        return EvaluationConfigurationResult.builder().build();
      }
      List<EvaluatorFieldMappingDTO> evaluatorMappings = new ArrayList<>();
      if (ec.getConnectorConf().getEvaluatorsConf() != null) {
        for (EvaluatorConf evaluatorConf : ec.getConnectorConf().getEvaluatorsConf().getEvaluatorConf()) {
          if (evaluatorConf.getIngressConf() == null) {
            continue;
          }
          EvaluatorIngressConf ingressConf = evaluatorConf.getIngressConf();
          EvaluatorFieldMappingDTO.EvaluatorFieldMappingDTOBuilder builder = EvaluatorFieldMappingDTO.builder()
            .evaluatorVersionId(evaluatorConf.getEvaluatorVersionId());
          if (evaluatorConf.getIngressConf().getEvalSetAdapter() != null) {
            List<FieldMappingDTO> fromEvalSet = evaluatorConf.getIngressConf().getEvalSetAdapter().getFieldConfs().stream()
              .map(fc -> {
                return FieldMappingDTO.builder()
                  .fieldName(fc.getFieldName())
                  .fromFieldName(fc.getFromField())
                  .constValue(fc.getValue())
                  .build();
              })
              .collect(Collectors.toList());
            builder.fromEvalSet(fromEvalSet);
          }
          if (evaluatorConf.getIngressConf().getTargetAdapter() != null) {
            List<FieldMappingDTO> fromTarget = evaluatorConf.getIngressConf().getTargetAdapter().getFieldConfs().stream()
              .map(fc -> {
                return FieldMappingDTO.builder()
                  .fieldName(fc.getFieldName())
                  .fromFieldName(fc.getFromField())
                  .constValue(fc.getValue())
                  .build();
              })
              .collect(Collectors.toList());
            builder.fromTarget(fromTarget);
          }
          Double passScore = ingressConf.getPassScore();
          if (passScore != null) {
            builder.passScore(passScore);
          }
          evaluatorMappings.add(builder.build());
        }
      }
      TargetFieldMappingDTO targetMapping = convertTargetFieldMappingDTO(ec);
      return EvaluationConfigurationResult.builder()
        .targetMapping(targetMapping)
        .evaluatorMappings(evaluatorMappings)
        .build();
    }

    private static TargetFieldMappingDTO convertTargetFieldMappingDTO(EvaluationConfiguration ec) {
      TargetFieldMappingDTO targetMapping = TargetFieldMappingDTO.builder().build();
      if (ec.getConnectorConf().getTargetConf() != null &&
        ec.getConnectorConf().getTargetConf().getIngressConf() != null &&
        ec.getConnectorConf().getTargetConf().getIngressConf().getEvalSetAdapter() != null) {
        List<FieldMappingDTO> fromEvalSet = ec.getConnectorConf().getTargetConf().getIngressConf().getEvalSetAdapter().getFieldConfs().stream()
          .map(fc -> {
            return FieldMappingDTO.builder()
              .fieldName(fc.getFieldName())
              .fromFieldName(fc.getFromField())
              .constValue(fc.getValue())
              .build();
          })
          .collect(Collectors.toList());
        targetMapping.setFromEvalSet(fromEvalSet);
      }
      return targetMapping;
    }

    // 私有辅助方法
    private static TargetIngressConf convertToTargetFieldMappingDO(TargetFieldMappingDTO mapping) {
      List<FieldMappingDTO> fromEvalSet = mapping.getFromEvalSet();
      if (fromEvalSet == null) {
        fromEvalSet = new ArrayList<>();
      }
      List<FieldConf> fc = fromEvalSet.stream()
        .map(fm -> {
          return FieldConf.builder()
            .fieldName(fm.getFieldName())
            .fromField(fm.getFromFieldName())
            .value(fm.getConstValue())
            .build();
        })
        .collect(Collectors.toList());
      return TargetIngressConf.builder()
        .evalSetAdapter(FieldAdapter.builder()
          .fieldConfs(fc)
          .build())
        .build();
    }

    private static List<EvaluatorConf> convertToEvaluatorFieldMappingDO(List<EvaluatorFieldMappingDTO> mapping) {
      return mapping.stream()
        .map(fm -> {
          List<FieldMappingDTO> fromEvalSet = fm.getFromEvalSet();
          if (fromEvalSet == null) {
            fromEvalSet = new ArrayList<>();
          }
          List<FieldConf> esf = fromEvalSet.stream()
            .map(fes -> {
              return FieldConf.builder()
                .fieldName(fes.getFieldName())
                .fromField(fes.getFromFieldName())
                .value(fes.getConstValue())
                .build();
            })
            .collect(Collectors.toList());
          List<FieldMappingDTO> fromTarget = fm.getFromTarget();
          if (fromTarget == null) {
            fromTarget = new ArrayList<>();
          }
          List<FieldConf> tf = fromTarget.stream()
            .map(ft -> {
              return FieldConf.builder()
                .fieldName(ft.getFieldName())
                .fromField(ft.getFromFieldName())
                .value(ft.getConstValue())
                .build();
            })
            .collect(Collectors.toList());
          return EvaluatorConf.builder()
            .evaluatorVersionId(fm.getEvaluatorVersionId())
            .ingressConf(EvaluatorIngressConf.builder()
              .evalSetAdapter(FieldAdapter.builder().fieldConfs(esf).build())
              .targetAdapter(FieldAdapter.builder().fieldConfs(tf).build())
              .passScore(fm.getPassScore())
              .build())
            .build();
        })
        .collect(Collectors.toList());
    }
  }
}
