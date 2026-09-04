package com.iwhalecloud.bote.loop.evaluation.application.convertor.evaluator;

import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorRecordDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorRunStatusDTO;
import com.iwhalecloud.bote.loop.evaluation.application.convertor.common.CommonConvertor;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorRecord;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorRunStatus;

/**
 * 评估器记录转换器
 * 对应Go: evaluator/evaluator_record.go
 */
public final class EvaluatorRecordApplicationConvertor {
  private EvaluatorRecordApplicationConvertor() {
  }

  /**
   * DTO转DO
   * 对应Go: ConvertEvaluatorRecordDTO2DO
   */
  public static EvaluatorRecord convertDTO2DO(EvaluatorRecordDTO dto) {
    if (dto == null) {
      return null;
    }
    return EvaluatorRecord.builder()
      .id(dto.getId())
      .experimentId(dto.getExperimentId())
      .experimentRunId(dto.getExperimentRunId())
      .itemId(dto.getItemId())
      .turnId(dto.getTurnId())
      .evaluatorVersionId(dto.getEvaluatorVersionId())
      .traceId(dto.getTraceId())
      .logId(dto.getLogId())
      .evaluatorInputData(EvaluatorInputDataApplicationConvertor.convertDTO2DO(dto.getEvaluatorInputData()))
      .evaluatorOutputData(EvaluatorOutputDataApplicationConvertor.convertDTO2DO(dto.getEvaluatorOutputData()))
      .status(EvaluatorRunStatus.fromValue(dto.getStatus().getValue()))
      .baseInfo(CommonConvertor.convertBaseInfoDTO2DO(dto.getBaseInfo()))
      .build();
  }

  /**
   * DO转DTO
   * 对应Go: ConvertEvaluatorRecordDO2DTO
   */
  public static EvaluatorRecordDTO convertDO2DTO(EvaluatorRecord doEntity) {
    if (doEntity == null) {
      return null;
    }
    return EvaluatorRecordDTO.builder()
      .id(doEntity.getId())
      .experimentId(doEntity.getExperimentId())
      .experimentRunId(doEntity.getExperimentRunId())
      .itemId(doEntity.getItemId())
      .turnId(doEntity.getTurnId())
      .evaluatorVersionId(doEntity.getEvaluatorVersionId())
      .traceId(doEntity.getTraceId())
      .logId(doEntity.getLogId())
      .evaluatorInputData(EvaluatorInputDataApplicationConvertor.convertDO2DTO(doEntity.getEvaluatorInputData()))
      .evaluatorOutputData(EvaluatorOutputDataApplicationConvertor.convertDO2DTO(doEntity.getEvaluatorOutputData()))
      .status(EvaluatorRunStatusDTO.fromValue(doEntity.getStatus().getValue()))
      .baseInfo(CommonConvertor.convertBaseInfoDO2DTO(doEntity.getBaseInfo()))
      .build();
  }
}
