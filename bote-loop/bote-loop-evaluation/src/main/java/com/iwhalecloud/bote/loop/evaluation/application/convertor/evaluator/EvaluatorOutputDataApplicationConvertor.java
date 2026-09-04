package com.iwhalecloud.bote.loop.evaluation.application.convertor.evaluator;

import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.CorrectionDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorOutputDataDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorResultDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorRunErrorDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorUsageDTO;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Correction;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorOutputData;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorRunError;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorUsage;

/**
 * 评估器输出数据转换器
 * 对应Go: evaluator/evaluator_output_data.go
 */
public final class EvaluatorOutputDataApplicationConvertor {
  private EvaluatorOutputDataApplicationConvertor() {
  }

  /**
   * DTO转DO
   * 对应Go: ConvertEvaluatorOutputDataDTO2DO
   */
  public static EvaluatorOutputData convertDTO2DO(EvaluatorOutputDataDTO dto) {
    if (dto == null) {
      return null;
    }
    return EvaluatorOutputData.builder()
      .evaluatorResult(convertEvaluatorResultDTO2DO(dto.getEvaluatorResult()))
      .evaluatorUsage(convertEvaluatorUsageDTO2DO(dto.getEvaluatorUsage()))
      .evaluatorRunError(convertEvaluatorRunErrorDTO2DO(dto.getEvaluatorRunError()))
      .timeConsumingMs(dto.getTimeConsumingMs())
      .build();
  }

  /**
   * DO转DTO
   * 对应Go: ConvertEvaluatorOutputDataDO2DTO
   */
  public static EvaluatorOutputDataDTO convertDO2DTO(EvaluatorOutputData doEntity) {
    if (doEntity == null) {
      return null;
    }
    return EvaluatorOutputDataDTO.builder()
      .evaluatorResult(convertEvaluatorResultDO2DTO(doEntity.getEvaluatorResult()))
      .evaluatorUsage(convertEvaluatorUsageDO2DTO(doEntity.getEvaluatorUsage()))
      .evaluatorRunError(convertEvaluatorRunErrorDO2DTO(doEntity.getEvaluatorRunError()))
      .timeConsumingMs(doEntity.getTimeConsumingMs())
      .build();
  }

  /**
   * 修正DTO转DO
   * 对应Go: ConvertCorrectionDTO2DO
   */
  public static Correction convertCorrectionDTO2DO(CorrectionDTO dto) {
    if (dto == null) {
      return null;
    }
    return Correction.builder()
      .score(dto.getScore())
      .explain(dto.getExplain())
      .updatedBy(dto.getUpdatedBy())
      .build();
  }

  /**
   * 修正DO转DTO
   * 对应Go: ConvertCorrectionDO2DTO
   */
  public static CorrectionDTO convertCorrectionDO2DTO(Correction doEntity) {
    if (doEntity == null) {
      return null;
    }
    return CorrectionDTO.builder()
      .score(doEntity.getScore())
      .explain(doEntity.getExplain())
      .updatedBy(doEntity.getUpdatedBy())
      .build();
  }

  /**
   * 评估器结果DTO转DO
   * 对应Go: ConvertEvaluatorResultDTO2DO
   */
  public static EvaluatorResult convertEvaluatorResultDTO2DO(EvaluatorResultDTO dto) {
    if (dto == null) {
      return null;
    }
    return EvaluatorResult.builder()
      .score(dto.getScore())
      .correction(convertCorrectionDTO2DO(dto.getCorrection()))
      .reasoning(dto.getReasoning())
      .build();
  }

  /**
   * 评估器结果DO转DTO
   * 对应Go: ConvertEvaluatorResultDO2DTO
   */
  public static EvaluatorResultDTO convertEvaluatorResultDO2DTO(EvaluatorResult doEntity) {
    if (doEntity == null) {
      return null;
    }
    return EvaluatorResultDTO.builder()
      .score(doEntity.getScore())
      .correction(convertCorrectionDO2DTO(doEntity.getCorrection()))
      .reasoning(doEntity.getReasoning())
      .build();
  }

  /**
   * 评估器使用量DTO转DO
   * 对应Go: ConvertEvaluatorUsageDTO2DO
   */
  public static EvaluatorUsage convertEvaluatorUsageDTO2DO(EvaluatorUsageDTO dto) {
    if (dto == null) {
      return null;
    }
    return EvaluatorUsage.builder()
      .inputTokens(dto.getInputTokens())
      .outputTokens(dto.getOutputTokens())
      .build();
  }

  /**
   * 评估器使用量DO转DTO
   * 对应Go: ConvertEvaluatorUsageDO2DTO
   */
  public static EvaluatorUsageDTO convertEvaluatorUsageDO2DTO(EvaluatorUsage doEntity) {
    if (doEntity == null) {
      return null;
    }
    return EvaluatorUsageDTO.builder()
      .inputTokens(doEntity.getInputTokens())
      .outputTokens(doEntity.getOutputTokens())
      .build();
  }

  /**
   * 评估器运行错误DTO转DO
   * 对应Go: ConvertEvaluatorRunErrorDTO2DO
   */
  public static EvaluatorRunError convertEvaluatorRunErrorDTO2DO(EvaluatorRunErrorDTO dto) {
    if (dto == null) {
      return null;
    }
    return EvaluatorRunError.builder()
      .code(dto.getCode())
      .message(dto.getMessage())
      .build();
  }

  /**
   * 评估器运行错误DO转DTO
   * 对应Go: ConvertEvaluatorRunErrorDO2DTO
   */
  public static EvaluatorRunErrorDTO convertEvaluatorRunErrorDO2DTO(EvaluatorRunError doEntity) {
    if (doEntity == null) {
      return null;
    }
    return EvaluatorRunErrorDTO.builder()
      .code(doEntity.getCode())
      .message(doEntity.getMessage())
      .build();
  }
}
