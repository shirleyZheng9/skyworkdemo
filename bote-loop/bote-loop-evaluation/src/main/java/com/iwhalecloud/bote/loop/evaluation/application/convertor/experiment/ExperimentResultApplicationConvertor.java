package com.iwhalecloud.bote.loop.evaluation.application.convertor.experiment;

import com.iwhalecloud.bote.loop.client.evaluation.domain.common.ContentTypeDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorRecordDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorTypeDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ColumnEvalSetFieldDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ColumnEvaluatorDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ExperimentResultDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ExperimentTurnPayloadDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ItemResultDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ItemRunStateDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ItemSystemInfoDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.RunErrorDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.TurnEvalSetDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.TurnEvaluatorOutputDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.TurnResultDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.TurnRunStateDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.TurnSystemInfoDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.TurnTargetOutputDTO;
import com.iwhalecloud.bote.loop.evaluation.application.convertor.common.CommonConvertor;
import com.iwhalecloud.bote.loop.evaluation.application.convertor.evaltarget.EvalTargetRecordConvertor;
import com.iwhalecloud.bote.loop.evaluation.application.convertor.evaluationset.EvaluationSetItemApplicationConvertor;
import com.iwhalecloud.bote.loop.evaluation.application.convertor.evaluator.EvaluatorRecordApplicationConvertor;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ColumnEvalSetField;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ColumnEvaluator;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorRecord;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExperimentResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExperimentTurnPayload;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ItemResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ItemSystemInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.RunError;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.TurnEvalSet;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.TurnEvaluatorOutput;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.TurnResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.TurnSystemInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.TurnTargetOutput;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 实验结果转换器
 * 对应Go: experiment/expt_result.go
 */
public final class ExperimentResultApplicationConvertor {

  private ExperimentResultApplicationConvertor() {
  }

  /**
   * 列评估集字段DO列表转DTO列表
   * 对应Go: ColumnEvalSetFieldsDO2DTOs
   */
  public static List<ColumnEvalSetFieldDTO> convertColumnEvalSetFieldsDO2DTOs(List<ColumnEvalSetField> from) {
    if (from == null || from.isEmpty()) {
      return List.of();
    }

    return from.stream()
      .map(ExperimentResultApplicationConvertor::convertColumnEvalSetFieldDO2DTO)
      .collect(Collectors.toList());
  }

  /**
   * 列评估集字段DO转DTO
   * 对应Go: ColumnEvalSetFieldsDO2DTO
   */
  public static ColumnEvalSetFieldDTO convertColumnEvalSetFieldDO2DTO(ColumnEvalSetField from) {
    if (from == null) {
      return null;
    }

    String contentType = CommonConvertor.convertContentTypeDO2DTO(from.getContentType());

    return ColumnEvalSetFieldDTO.builder()
      .key(from.getKey())
      .name(from.getName())
      .description(from.getDescription())
      .contentType(ContentTypeDTO.fromValue(contentType))
      .textSchema(from.getTextSchema())
      .build();
  }

  /**
   * 列评估器DO列表转DTO列表
   * 对应Go: ColumnEvaluatorsDO2DTOs
   */
  public static List<ColumnEvaluatorDTO> convertColumnEvaluatorsDO2DTOs(List<ColumnEvaluator> from) {
    if (from == null || from.isEmpty()) {
      return List.of();
    }

    return from.stream()
      .map(ExperimentResultApplicationConvertor::convertColumnEvaluatorDO2DTO)
      .collect(Collectors.toList());
  }

  /**
   * 列评估器DO转DTO
   * 对应Go: ColumnEvaluatorsDO2DTO
   */
  public static ColumnEvaluatorDTO convertColumnEvaluatorDO2DTO(ColumnEvaluator from) {
    if (from == null) {
      return null;
    }

    return ColumnEvaluatorDTO.builder()
      .evaluatorVersionId(from.getEvaluatorVersionId())
      .evaluatorId(from.getEvaluatorId())
      .evaluatorType(EvaluatorTypeDTO.fromValue(from.getEvaluatorType().getValue()))
      .name(from.getName())
      .version(from.getVersion())
      .description(from.getDescription())
      .build();
  }

  /**
   * 数据项结果DO列表转DTO列表
   * 对应Go: ItemResultsDO2DTOs
   */
  public static List<ItemResultDTO> convertItemResultsDO2DTOs(List<ItemResult> from) {
    if (from == null || from.isEmpty()) {
      return List.of();
    }

    return from.stream()
      .map(ExperimentResultApplicationConvertor::convertItemResultDO2DTO)
      .collect(Collectors.toList());
  }

  /**
   * 数据项结果DO转DTO
   * 对应Go: ItemResultsDO2DTO
   */
  public static ItemResultDTO convertItemResultDO2DTO(ItemResult from) {
    if (from == null) {
      return null;
    }

    return ItemResultDTO.builder()
      .itemId(from.getItemId())
      .turnResults(convertTurnResultsDO2DTOs(from.getTurnResults()))
      .systemInfo(convertItemSystemInfoDO2DTO(from.getSystemInfo()))
      .itemIndex(from.getItemIndex())
      .build();
  }

  /**
   * 轮次结果DO列表转DTO列表
   * 对应Go: TurnResultsDO2DTOs
   */
  public static List<TurnResultDTO> convertTurnResultsDO2DTOs(List<TurnResult> from) {
    if (from == null || from.isEmpty()) {
      return List.of();
    }

    return from.stream()
      .map(ExperimentResultApplicationConvertor::convertTurnResultDO2DTO)
      .collect(Collectors.toList());
  }

  /**
   * 轮次结果DO转DTO
   * 对应Go: TurnResultsDO2DTO
   */
  public static TurnResultDTO convertTurnResultDO2DTO(TurnResult from) {
    if (from == null) {
      return null;
    }

    return TurnResultDTO.builder()
      .turnId(from.getTurnId())
      .experimentResults(convertExperimentResultsDO2DTOs(from.getExperimentResults()))
      .experimentRunId(from.getExperimentRunId())
      .turnIndex(from.getTurnIndex())
      .build();
  }

  /**
   * 实验结果DO列表转DTO列表
   * 对应Go: ExperimentResultsDO2DTOs
   */
  public static List<ExperimentResultDTO> convertExperimentResultsDO2DTOs(List<ExperimentResult> from) {
    if (from == null || from.isEmpty()) {
      return List.of();
    }

    return from.stream()
      .map(ExperimentResultApplicationConvertor::convertExperimentResultDO2DTO)
      .collect(Collectors.toList());
  }

  /**
   * 实验结果DO转DTO
   * 对应Go: ExperimentResultsDO2DTO
   */
  public static ExperimentResultDTO convertExperimentResultDO2DTO(ExperimentResult from) {
    if (from == null) {
      return null;
    }

    return ExperimentResultDTO.builder()
      .experimentId(from.getExperimentId())
      .payload(convertExperimentTurnPayloadDO2DTO(from.getPayload()))
      .build();
  }

  /**
   * 实验轮次载荷DO转DTO
   * 对应Go: ExperimentTurnPayloadDO2DTO
   */
  public static ExperimentTurnPayloadDTO convertExperimentTurnPayloadDO2DTO(ExperimentTurnPayload from) {
    if (from == null) {
      return null;
    }

    return ExperimentTurnPayloadDTO.builder()
      .turnId(from.getTurnId())
      .evalSet(convertTurnEvalSetDO2DTO(from.getEvalSet()))
      .targetOutput(convertTurnTargetOutputDO2DTO(from.getTargetOutput()))
      .evaluatorOutput(convertTurnEvaluatorOutputDO2DTO(from.getEvaluatorOutput()))
      .systemInfo(convertTurnSystemInfoDO2DTO(from.getSystemInfo()))
      .build();
  }

  /**
   * 轮次评估器输出DO转DTO
   * 对应Go: TurnEvaluatorOutputDO2DTO
   */
  public static TurnEvaluatorOutputDTO convertTurnEvaluatorOutputDO2DTO(TurnEvaluatorOutput from) {
    if (from == null) {
      return TurnEvaluatorOutputDTO.builder().build();
    }

    Map<Long, EvaluatorRecordDTO> evaluatorRecords = new HashMap<>();
    if (from.getEvaluatorRecords() != null) {
      for (Map.Entry<Long, EvaluatorRecord> entry : from.getEvaluatorRecords().entrySet()) {
        evaluatorRecords.put(entry.getKey(),
          EvaluatorRecordApplicationConvertor.convertDO2DTO(entry.getValue()));
      }
    }

    return TurnEvaluatorOutputDTO.builder()
      .evaluatorRecords(evaluatorRecords)
      .build();
  }

  /**
   * 轮次目标输出DO转DTO
   * 对应Go: TurnTargetOutputDO2DTO
   */
  public static TurnTargetOutputDTO convertTurnTargetOutputDO2DTO(TurnTargetOutput from) {
    if (from == null) {
      return TurnTargetOutputDTO.builder().build();
    }

    return TurnTargetOutputDTO.builder()
      .evalTargetRecord(EvalTargetRecordConvertor.convertDO2DTO(from.getEvalTargetRecord()))
      .build();
  }

  /**
   * 轮次评估集DO转DTO
   * 对应Go: TurnEvalSetDO2DTO
   */
  public static TurnEvalSetDTO convertTurnEvalSetDO2DTO(TurnEvalSet from) {
    if (from == null) {
      return TurnEvalSetDTO.builder().build();
    }

    return TurnEvalSetDTO.builder()
      .turn(EvaluationSetItemApplicationConvertor.convertTurnDO2DTO(from.getTurn()))
      .build();
  }

  /**
   * 轮次系统信息DO转DTO
   * 对应Go: TurnSystemInfoDO2DTO
   */
  public static TurnSystemInfoDTO convertTurnSystemInfoDO2DTO(TurnSystemInfo from) {
    if (from == null) {
      return TurnSystemInfoDTO.builder().build();
    }

    return TurnSystemInfoDTO.builder()
      .turnRunState(TurnRunStateDTO.fromValue(from.getTurnRunState().getValue()))
      .logId(from.getLogId())
      .error(convertRunErrorDO2DTO(from.getError()))
      .build();
  }

  /**
   * 运行错误DO转DTO
   * 对应Go: RunErrorDO2DTO
   */
  public static RunErrorDTO convertRunErrorDO2DTO(RunError from) {
    if (from == null) {
      return null;
    }

    return RunErrorDTO.builder()
      .code(from.getCode())
      .message(from.getMessage())
      .detail(from.getDetail())
      .build();
  }

  /**
   * 数据项系统信息DO转DTO
   * 对应Go: ItemSystemInfoDO2DTO
   */
  public static ItemSystemInfoDTO convertItemSystemInfoDO2DTO(ItemSystemInfo from) {
    if (from == null) {
      return ItemSystemInfoDTO.builder().build();
    }

    return ItemSystemInfoDTO.builder()
      .runState(ItemRunStateDTO.fromValue(from.getRunState().getValue()))
      .logId(from.getLogId())
      .error(convertRunErrorDO2DTO(from.getError()))
      .build();
  }
}
