package com.iwhalecloud.bote.loop.evaluation.infra.repo.evaluator.mysql.convertor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwhalecloud.bote.entity.loop.evaluation.EvaluatorRecordEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.BaseInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorInputData;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorOutputData;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorRecord;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorRunStatus;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.UserInfo;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * 评估器执行结果转换器
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/evaluator/mysql/convertor/evaluator_record.go
 *
 * @author Generated
 * @since 2025-01-27
 */
@Component
public final class EvaluatorRecordConvertor {

  private EvaluatorRecordConvertor() {
    // 工具类，禁止实例化
  }

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * 将DO对象转换为PO对象
   * 迁移对应关系: Go语言ConvertEvaluatorRecordDO2PO
   */
  public static EvaluatorRecordEntity convertToPO(EvaluatorRecord doEntity) {
    if (doEntity == null) {
      return null;
    }

    EvaluatorRecordEntity po = buildBasicPO(doEntity);
    serializeInputData(doEntity, po);
    serializeOutputData(doEntity, po);
    setBaseInfo(doEntity, po);
    serializeExtData(doEntity, po);

    return po;
  }

  private static EvaluatorRecordEntity buildBasicPO(EvaluatorRecord doEntity) {
    return EvaluatorRecordEntity.builder()
      .id(doEntity.getId())
      .spaceId(doEntity.getSpaceId())
      .experimentId(doEntity.getExperimentId())
      .experimentRunId(doEntity.getExperimentRunId())
      .itemId(doEntity.getItemId())
      .evaluatorVersionId(doEntity.getEvaluatorVersionId())
      .turnId(doEntity.getTurnId())
      .logId(doEntity.getLogId())
      .traceId(doEntity.getTraceId())
      .status(doEntity.getStatus().getValue())
      .build();
  }

  private static void serializeInputData(EvaluatorRecord doEntity, EvaluatorRecordEntity po) {
    if (doEntity.getEvaluatorInputData() != null) {
      String inputDataBytes = JsonUtil.toJsonString(doEntity.getEvaluatorInputData());
      po.setInputData(inputDataBytes);
    }
  }

  private static void serializeOutputData(EvaluatorRecord doEntity, EvaluatorRecordEntity po) {
    if (doEntity.getEvaluatorOutputData() != null) {
      String outputDataBytes = JsonUtil.toJsonString(doEntity.getEvaluatorOutputData());
      po.setOutputData(outputDataBytes);
      setScoreInfo(doEntity, po);
    }
  }

  private static void setScoreInfo(EvaluatorRecord doEntity, EvaluatorRecordEntity po) {
    if (doEntity.getEvaluatorOutputData().getEvaluatorResult() != null) {
      EvaluatorResult result = doEntity.getEvaluatorOutputData().getEvaluatorResult();
      if (result.getCorrection() != null) {
        po.setUpdatedBy(result.getCorrection().getUpdatedBy());
        po.setScore(result.getCorrection().getScore());
      }
      else {
        po.setScore(result.getScore());
      }
    }
  }

  private static void setBaseInfo(EvaluatorRecord doEntity, EvaluatorRecordEntity po) {
    if (doEntity.getBaseInfo() != null) {
      setUserInfo(doEntity, po);
      setTimestampInfo(doEntity, po);
    }
  }

  private static void setUserInfo(EvaluatorRecord doEntity, EvaluatorRecordEntity po) {
    if (doEntity.getBaseInfo().getCreatedBy() != null) {
      po.setCreatedBy(doEntity.getBaseInfo().getCreatedBy().getUserId());
    }
    if (doEntity.getBaseInfo().getUpdatedBy() != null) {
      po.setUpdatedBy(doEntity.getBaseInfo().getUpdatedBy().getUserId());
    }
  }

  private static void setTimestampInfo(EvaluatorRecord doEntity, EvaluatorRecordEntity po) {
    if (doEntity.getBaseInfo().getCreatedAt() != null) {
      po.setCreatedAt(Date.from(Instant.ofEpochSecond(doEntity.getBaseInfo().getCreatedAt() / 1000)));
    }
    if (doEntity.getBaseInfo().getUpdatedAt() != null) {
      po.setUpdatedAt(Date.from(Instant.ofEpochSecond(doEntity.getBaseInfo().getUpdatedAt() / 1000)));
    }
  }

  private static void serializeExtData(EvaluatorRecord doEntity, EvaluatorRecordEntity po) {
    if (doEntity.getExt() != null && !doEntity.getExt().isEmpty()) {
      String extBytes = JsonUtil.toJsonString(doEntity.getExt());
      po.setExt(extBytes);
    }
  }

  /**
   * 将PO对象转换为DO对象
   * 迁移对应关系: Go语言ConvertEvaluatorRecordPO2DO
   */
  public static EvaluatorRecord convertToDO(EvaluatorRecordEntity po) {
    if (po == null) {
      return null;
    }

    EvaluatorRecord doEntity = buildBasicEvaluatorRecord(po);
    deserializeInputOutputData(doEntity, po);
    setBaseInfoFromPO(doEntity, po);
    deserializeExt(doEntity, po);
    return doEntity;
  }

  /**
   * 构建基本评估器记录
   */
  private static EvaluatorRecord buildBasicEvaluatorRecord(EvaluatorRecordEntity po) {
    return EvaluatorRecord.builder()
      .id(po.getId())
      .spaceId(po.getSpaceId())
      .experimentId(po.getExperimentId())
      .experimentRunId(po.getExperimentRunId())
      .itemId(po.getItemId())
      .evaluatorVersionId(po.getEvaluatorVersionId())
      .traceId(po.getTraceId())
      .logId(po.getLogId())
      .turnId(po.getTurnId())
      .status(EvaluatorRunStatus.fromValue(po.getStatus()))
      .build();
  }

  /**
   * 反序列化输入输出数据
   */
  private static void deserializeInputOutputData(EvaluatorRecord record, EvaluatorRecordEntity po) {
    deserializeInputData(record, po);
    deserializeOutputData(record, po);
  }

  /**
   * 反序列化输入数据
   */
  private static void deserializeInputData(EvaluatorRecord record, EvaluatorRecordEntity po) {
    if (po.getInputData() != null) {
      try {
        EvaluatorInputData inputData = objectMapper.readValue(po.getInputData(), EvaluatorInputData.class);
        record.setEvaluatorInputData(inputData);
      }
      catch (IOException e) {
        throw new BssException("反序列化输入数据失败: " + e.getMessage(), e);
      }
    }
  }

  /**
   * 反序列化输出数据
   */
  private static void deserializeOutputData(EvaluatorRecord record, EvaluatorRecordEntity po) {
    if (po.getOutputData() != null) {
      try {
        EvaluatorOutputData outputData = objectMapper.readValue(po.getOutputData(), EvaluatorOutputData.class);
        record.setEvaluatorOutputData(outputData);
      }
      catch (IOException e) {
        throw new BssException("反序列化输出数据失败: " + e.getMessage(), e);
      }
    }
  }

  /**
   * 设置基础信息（PO转DO）
   */
  private static void setBaseInfoFromPO(EvaluatorRecord record, EvaluatorRecordEntity po) {
    BaseInfo baseInfo = BaseInfo.builder()
      .createdAt(po.getCreatedAt() != null ? po.getCreatedAt().toInstant().toEpochMilli() : null)
      .updatedAt(po.getUpdatedAt() != null ? po.getUpdatedAt().toInstant().toEpochMilli() : null)
      .createdBy(UserInfo.builder().userId(po.getCreatedBy()).build())
      .updatedBy(UserInfo.builder().userId(po.getUpdatedBy()).build())
      .build();

    if (po.getDeletedAt() != null) {
      baseInfo.setDeletedAt(po.getDeletedAt());
    }

    record.setBaseInfo(baseInfo);
  }

  /**
   * 反序列化扩展信息
   */
  private static void deserializeExt(EvaluatorRecord record, EvaluatorRecordEntity po) {
    if (po.getExt() != null) {
      try {
        Map<String, String> ext = objectMapper.readValue(po.getExt(), new TypeReference<>() {
        });
        record.setExt(ext);
      }
      catch (IOException e) {
        throw new BssException("反序列化扩展信息失败: " + e.getMessage(), e);
      }
    }
  }
}
