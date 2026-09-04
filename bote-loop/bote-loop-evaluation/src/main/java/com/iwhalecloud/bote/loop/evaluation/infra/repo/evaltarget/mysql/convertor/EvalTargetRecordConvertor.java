package com.iwhalecloud.bote.loop.evaluation.infra.repo.evaltarget.mysql.convertor;

import com.iwhalecloud.bote.entity.loop.evaluation.TargetRecordEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.BaseInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetInputData;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetOutputData;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetRecord;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetRunStatus;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.Date;

/**
 * 评估目标记录转换器
 * 对应Go: convertor/eval_target_record.go
 */
public final class EvalTargetRecordConvertor {
  private EvalTargetRecordConvertor() {
  }

  /**
   * DO转PO
   * 对应Go: EvalTargetRecordDO2PO(e *entity.EvalTargetRecord) (*model.TargetRecord, error)
   */
  public static TargetRecordEntity convertDO2PO(EvalTargetRecord evalTargetRecord) {
    if (evalTargetRecord == null) {
      return null;
    }

    // 处理输入数据序列化
    String inputData = null;
    if (evalTargetRecord.getEvalTargetInputData() != null) {
      inputData = JsonUtil.toJsonString(evalTargetRecord.getEvalTargetInputData());
    }

    // 处理输出数据序列化
    String outputData = null;
    if (evalTargetRecord.getEvalTargetOutputData() != null) {
      outputData = JsonUtil.toJsonString(evalTargetRecord.getEvalTargetOutputData());
    }

    // 处理状态转换（包含nil指针处理）
    int status;
    if (evalTargetRecord.getStatus() != null) {
      status = evalTargetRecord.getStatus().getValue();
    }
    else {
      status = EvalTargetRunStatus.UNKNOWN.getValue();
    }

    TargetRecordEntity.TargetRecordEntityBuilder builder = TargetRecordEntity.builder()
      .id(evalTargetRecord.getId())
      .spaceId(evalTargetRecord.getSpaceId())
      .targetId(evalTargetRecord.getTargetId())
      .targetVersionId(evalTargetRecord.getTargetVersionId())
      .experimentRunId(evalTargetRecord.getExperimentRunId())
      .itemId(evalTargetRecord.getItemId())
      .turnId(evalTargetRecord.getTurnId())
      .logId(evalTargetRecord.getLogId())
      .traceId(evalTargetRecord.getTraceId())
      .inputData(inputData)
      .outputData(outputData)
      .status(status);

    if (evalTargetRecord.getBaseInfo() != null) {
      if (evalTargetRecord.getBaseInfo().getCreatedAt() != null) {
        builder.createdAt(new Date(evalTargetRecord.getBaseInfo().getCreatedAt()));
      }
      if (evalTargetRecord.getBaseInfo().getUpdatedAt() != null) {
        builder.updatedAt(new Date(evalTargetRecord.getBaseInfo().getUpdatedAt()));
      }
    }

    return builder.build();
  }

  /**
   * PO转DO
   * 对应Go: EvalTargetRecordPO2DO(m *model.TargetRecord) (*entity.EvalTargetRecord, error)
   */
  public static EvalTargetRecord convertPO2DO(TargetRecordEntity targetRecordEntity) {
    if (targetRecordEntity == null) {
      return null;
    }

    // 处理输入数据反序列化
    EvalTargetInputData evalTargetInputData = null;
    if (targetRecordEntity.getInputData() != null && !targetRecordEntity.getInputData().isEmpty()) {
      evalTargetInputData = JsonUtil.parseJson(targetRecordEntity.getInputData(), EvalTargetInputData.class);
    }

    // 处理输出数据反序列化
    EvalTargetOutputData evalTargetOutputData = null;
    if (targetRecordEntity.getOutputData() != null && !targetRecordEntity.getOutputData().isEmpty()) {
      evalTargetOutputData = JsonUtil.parseJson(targetRecordEntity.getOutputData(), EvalTargetOutputData.class);
    }

    // 状态类型转换
    EvalTargetRunStatus status = EvalTargetRunStatus.fromValue(targetRecordEntity.getStatus());

    EvalTargetRecord.EvalTargetRecordBuilder builder = EvalTargetRecord.builder()
      .id(targetRecordEntity.getId())
      .spaceId(targetRecordEntity.getSpaceId())
      .targetId(targetRecordEntity.getTargetId())
      .targetVersionId(targetRecordEntity.getTargetVersionId())
      .experimentRunId(targetRecordEntity.getExperimentRunId())
      .itemId(targetRecordEntity.getItemId())
      .turnId(targetRecordEntity.getTurnId())
      .logId(targetRecordEntity.getLogId())
      .traceId(targetRecordEntity.getTraceId())
      .evalTargetInputData(evalTargetInputData)
      .evalTargetOutputData(evalTargetOutputData)
      .status(status);

    BaseInfo.BaseInfoBuilder baseInfoBuilder = BaseInfo.builder();
    if (targetRecordEntity.getCreatedAt() != null) {
      baseInfoBuilder.createdAt(targetRecordEntity.getCreatedAt().getTime());
    }
    if (targetRecordEntity.getUpdatedAt() != null) {
      baseInfoBuilder.updatedAt(targetRecordEntity.getUpdatedAt().getTime());
    }

    return builder.baseInfo(baseInfoBuilder.build()).build();
  }
}
