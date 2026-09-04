package com.iwhalecloud.bote.loop.evaluation.infra.repo.evaluator.mysql.convertor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.entity.loop.evaluation.EvaluatorEntity;
import com.iwhalecloud.bote.entity.loop.evaluation.EvaluatorVersionEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ArgsSchema;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.BaseInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Evaluator;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.PromptEvaluatorVersion;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.UserInfo;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 评估器转换器
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/evaluator/mysql/convertor/evaluator.go
 *
 * @author Generated
 * @since 2025-01-27
 */
public final class EvaluatorConvertor {

  private EvaluatorConvertor() {
    // 工具类，禁止实例化
  }

  /**
   * 将DO对象转换为PO对象
   * 迁移对应关系: Go语言ConvertEvaluatorDO2PO
   */
  public static EvaluatorEntity convertToPO(Evaluator doEntity) {
    if (doEntity == null) {
      return null;
    }

    EvaluatorEntity po = EvaluatorEntity.builder()
      .id(doEntity.getId())
      .spaceId(doEntity.getSpaceId())
      .name(doEntity.getName())
      .description(doEntity.getDescription())
      .draftSubmitted(doEntity.getDraftSubmitted())
      .evaluatorType(doEntity.getEvaluatorType().getValue())
      .latestVersion(doEntity.getLatestVersion())
      .catalogItemId(doEntity.getCatalogItemId())
      .build();

    if (doEntity.getBaseInfo() != null) {
      if (doEntity.getBaseInfo().getCreatedBy() != null) {
        po.setCreatedBy(doEntity.getBaseInfo().getCreatedBy().getUserId());
      }
      if (doEntity.getBaseInfo().getUpdatedBy() != null) {
        po.setUpdatedBy(doEntity.getBaseInfo().getUpdatedBy().getUserId());
      }
      if (doEntity.getBaseInfo().getCreatedAt() != null) {
        po.setCreatedAt(Date.from(Instant.ofEpochSecond(doEntity.getBaseInfo().getCreatedAt() / 1000)));
      }
      if (doEntity.getBaseInfo().getUpdatedAt() != null) {
        po.setUpdatedAt(Date.from(Instant.ofEpochSecond(doEntity.getBaseInfo().getUpdatedAt() / 1000)));
      }
    }

    return po;
  }

  /**
   * 将PO对象转换为DO对象
   * 迁移对应关系: Go语言ConvertEvaluatorPO2DO
   */
  public static Evaluator convertToDO(EvaluatorEntity po) {
    if (po == null) {
      return null;
    }

    Evaluator doEntity = Evaluator.builder()
      .id(po.getId())
      .spaceId(po.getSpaceId())
      .name(po.getName())
      .description(po.getDescription())
      .draftSubmitted(po.getDraftSubmitted())
      .evaluatorType(EvaluatorType.fromValue(po.getEvaluatorType()))
      .latestVersion(po.getLatestVersion())
      .catalogItemId(po.getCatalogItemId())
      .build();

    BaseInfo baseInfo = BaseInfo.builder()
      .createdBy(UserInfo.builder().userId(po.getCreatedBy()).build())
      .updatedBy(UserInfo.builder().userId(po.getUpdatedBy()).build())
      .createdAt(po.getCreatedAt() != null ? po.getCreatedAt().toInstant().toEpochMilli() : null)
      .updatedAt(po.getUpdatedAt() != null ? po.getUpdatedAt().toInstant().toEpochMilli() : null)
      .build();

    if (po.getDeletedAt() != null) {
      baseInfo.setDeletedAt(po.getDeletedAt().getTime());
    }

    doEntity.setBaseInfo(baseInfo);
    return doEntity;
  }

  /**
   * 将评估器版本DO对象转换为PO对象
   * 迁移对应关系: Go语言ConvertEvaluatorVersionDO2PO
   */
  public static EvaluatorVersionEntity convertVersionToPO(Evaluator doEntity) {
    if (doEntity == null || doEntity.getEvaluatorVersion() == null) {
      return null;
    }

    EvaluatorVersionEntity po = buildBasicEvaluatorVersionEntity(doEntity);
    setBaseInfoToPO(doEntity, po);
    processEvaluatorTypeSpecificInfo(doEntity, po);

    return po;
  }

  private static EvaluatorVersionEntity buildBasicEvaluatorVersionEntity(Evaluator doEntity) {
    return EvaluatorVersionEntity.builder()
      .id(doEntity.getEvaluatorVersion().getId())
      .spaceId(doEntity.getSpaceId())
      .version(doEntity.getEvaluatorVersion().getVersion())
      .evaluatorType(doEntity.getEvaluatorType().getValue())
      .evaluatorId(doEntity.getId())
      .description(doEntity.getEvaluatorVersion().getDescription())
      .build();
  }

  private static void setBaseInfoToPO(Evaluator doEntity, EvaluatorVersionEntity po) {
    if (doEntity.getEvaluatorVersion().getBaseInfo() != null) {
      setUserInfoToPO(doEntity, po);
      setTimestampInfoToPO(doEntity, po);
      setDeletedAtToPO(doEntity, po);
    }
  }

  private static void setUserInfoToPO(Evaluator doEntity, EvaluatorVersionEntity po) {
    if (doEntity.getEvaluatorVersion().getBaseInfo().getCreatedBy() != null) {
      po.setCreatedBy(doEntity.getEvaluatorVersion().getBaseInfo().getCreatedBy().getUserId());
    }
    if (doEntity.getEvaluatorVersion().getBaseInfo().getUpdatedBy() != null) {
      po.setUpdatedBy(doEntity.getEvaluatorVersion().getBaseInfo().getUpdatedBy().getUserId());
    }
  }

  private static void setTimestampInfoToPO(Evaluator doEntity, EvaluatorVersionEntity po) {
    if (doEntity.getEvaluatorVersion().getBaseInfo().getCreatedAt() != null) {
      po.setCreatedAt(Date.from(Instant.ofEpochSecond(doEntity.getEvaluatorVersion().getBaseInfo().getCreatedAt() / 1000)));
    }
    if (doEntity.getEvaluatorVersion().getBaseInfo().getUpdatedAt() != null) {
      po.setUpdatedAt(Date.from(Instant.ofEpochSecond(doEntity.getEvaluatorVersion().getBaseInfo().getUpdatedAt() / 1000)));
    }
  }

  private static void setDeletedAtToPO(Evaluator doEntity, EvaluatorVersionEntity po) {
    if (doEntity.getEvaluatorVersion().getBaseInfo().getDeletedAt() != null) {
      po.setDeletedAt(new Date(doEntity.getEvaluatorVersion().getBaseInfo().getDeletedAt()));
    }
  }

  private static void processEvaluatorTypeSpecificInfo(Evaluator doEntity, EvaluatorVersionEntity po) {
    if (doEntity.getEvaluatorType() == EvaluatorType.PROMPT) {
      processPromptEvaluatorInfo(doEntity, po);
    }
  }

  private static void processPromptEvaluatorInfo(Evaluator doEntity, EvaluatorVersionEntity po) {
    try {
      serializePromptEvaluatorMetaInfo(doEntity, po);
      serializePromptEvaluatorInputSchema(doEntity, po);
      setPromptEvaluatorBasicInfo(doEntity, po);
    }
    catch (JsonProcessingException e) {
      throw new BssException("序列化评估器版本信息失败: " + e.getMessage(), e);
    }
  }

  private static void serializePromptEvaluatorMetaInfo(Evaluator doEntity, EvaluatorVersionEntity po) throws JsonProcessingException {
    po.setMetainfo(JsonUtil.toJsonString(doEntity.getPromptEvaluatorVersion()));
  }

  private static void serializePromptEvaluatorInputSchema(Evaluator doEntity, EvaluatorVersionEntity po) throws JsonProcessingException {
    List<ArgsSchema> inputSchemas = doEntity.getPromptEvaluatorVersion().getInputSchemas();
    if (inputSchemas == null) {
      po.setInputSchema(JsonUtil.toJsonString(Collections.emptySet()));
      return;
    }
    po.setInputSchema(JsonUtil.toJsonString(inputSchemas));
  }

  private static void setPromptEvaluatorBasicInfo(Evaluator doEntity, EvaluatorVersionEntity po) {
    po.setReceiveChatHistory(doEntity.getPromptEvaluatorVersion().getReceiveChatHistory());
    po.setId(doEntity.getPromptEvaluatorVersion().getId());
  }

  /**
   * 将评估器版本PO对象转换为DO对象
   * 迁移对应关系: Go语言ConvertEvaluatorVersionPO2DO
   */
  public static Evaluator convertVersionToDO(EvaluatorVersionEntity po) {
    if (po == null) {
      return null;
    }

    Evaluator doEntity = Evaluator.builder()
      .evaluatorType(EvaluatorType.fromValue(po.getEvaluatorType()))
      .build();

    if (doEntity.getEvaluatorType() == EvaluatorType.PROMPT) {
      processPromptEvaluatorVersion(doEntity, po);
    }

    setVersionBasicInfo(doEntity, po);
    setVersionBaseInfo(doEntity, po);
    return doEntity;
  }

  /**
   * 处理Prompt评估器版本
   */
  private static void processPromptEvaluatorVersion(Evaluator evaluator, EvaluatorVersionEntity po) {
    evaluator.setPromptEvaluatorVersion(new PromptEvaluatorVersion());
    deserializeMetainfo(evaluator, po);
    deserializeInputSchema(evaluator, po);
  }

  /**
   * 反序列化元信息
   */
  private static void deserializeMetainfo(Evaluator evaluator, EvaluatorVersionEntity po) {
    if (po.getMetainfo() != null) {
      PromptEvaluatorVersion promptVersion = JsonUtil.parseJson(po.getMetainfo(), PromptEvaluatorVersion.class);
      evaluator.setPromptEvaluatorVersion(promptVersion);
    }
  }

  /**
   * 反序列化输入模式
   */
  private static void deserializeInputSchema(Evaluator evaluator, EvaluatorVersionEntity po) {
    if (po.getInputSchema() != null) {
      List<ArgsSchema> schemas = JsonUtil.parseJson(po.getInputSchema(), new TypeReference<>() {
      });
      evaluator.getPromptEvaluatorVersion().setInputSchemas(schemas);
    }
  }

  /**
   * 设置版本基本信息
   */
  private static void setVersionBasicInfo(Evaluator evaluator, EvaluatorVersionEntity po) {
    evaluator.getEvaluatorVersion().setId(po.getId());
    evaluator.getEvaluatorVersion().setVersion(po.getVersion());
    evaluator.getEvaluatorVersion().setSpaceId(po.getSpaceId());
    evaluator.getEvaluatorVersion().setEvaluatorId(po.getEvaluatorId());
    if (po.getDescription() != null) {
      evaluator.getEvaluatorVersion().setDescription(po.getDescription());
    }
  }

  /**
   * 设置版本基础信息
   */
  private static void setVersionBaseInfo(Evaluator evaluator, EvaluatorVersionEntity po) {
    BaseInfo baseInfo = BaseInfo.builder()
      .createdBy(UserInfo.builder().userId(po.getCreatedBy()).build())
      .updatedBy(UserInfo.builder().userId(po.getUpdatedBy()).build())
      .createdAt(po.getCreatedAt() != null ? po.getCreatedAt().toInstant().toEpochMilli() : null)
      .updatedAt(po.getUpdatedAt() != null ? po.getUpdatedAt().toInstant().toEpochMilli() : null)
      .build();

    if (po.getDeletedAt() != null) {
      baseInfo.setDeletedAt(po.getDeletedAt().getTime());
    }

    evaluator.getEvaluatorVersion().setBaseInfo(baseInfo);
  }
}
