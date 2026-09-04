package com.iwhalecloud.bote.loop.evaluation.application.convertor.evaltarget;

import com.iwhalecloud.bote.loop.client.evaluation.domain.common.ContentDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.MessageDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target.EvalTargetInputDataDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target.EvalTargetOutputDataDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target.EvalTargetRecordDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target.EvalTargetRunErrorDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target.EvalTargetRunStatusDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target.EvalTargetUsageDTO;
import com.iwhalecloud.bote.loop.evaluation.application.convertor.common.CommonConvertor;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Content;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetInputData;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetOutputData;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetRecord;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetRunError;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetRunStatus;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetUsage;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Message;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评估目标记录转换器
 * 对应Go: target/eval_target_record.go
 */
public final class EvalTargetRecordConvertor {

  private EvalTargetRecordConvertor() {
  }

  /**
   * DO转DTO
   * 对应Go: EvalTargetRecordDO2DTO
   */
  public static EvalTargetRecordDTO convertDO2DTO(EvalTargetRecord src) {
    if (src == null) {
      return null;
    }

    return EvalTargetRecordDTO.builder()
      .id(src.getId())
      .workspaceId(src.getSpaceId())
      .targetId(src.getTargetId())
      .targetVersionId(src.getTargetVersionId())
      .experimentRunId(src.getExperimentRunId())
      .itemId(src.getItemId())
      .turnId(src.getTurnId())
      .traceId(src.getTraceId())
      .logId(src.getLogId())
      .evalTargetInputData(convertInputDO2DTO(src.getEvalTargetInputData()))
      .evalTargetOutputData(convertOutputDO2DTO(src.getEvalTargetOutputData()))
      .status(convertStatusDO2DTO(src.getStatus()))
      .baseInfo(CommonConvertor.convertBaseInfoDO2DTO(src.getBaseInfo()))
      .build();
  }

  /**
   * DTO转DO
   * 对应Go: RecordDTO2DO
   */
  public static EvalTargetRecord convertDTO2DO(EvalTargetRecordDTO src) {
    if (src == null) {
      return null;
    }

    return EvalTargetRecord.builder()
      .id(src.getId())
      .spaceId(src.getWorkspaceId())
      .targetId(src.getTargetId())
      .targetVersionId(src.getTargetVersionId())
      .experimentRunId(src.getExperimentRunId())
      .itemId(src.getItemId())
      .turnId(src.getTurnId())
      .traceId(src.getTraceId())
      .logId(src.getLogId())
      .evalTargetInputData(convertInputDTO2DO(src.getEvalTargetInputData()))
      .evalTargetOutputData(convertOutputDTO2DO(src.getEvalTargetOutputData()))
      .status(convertStatusDTO2DO(src.getStatus()))
      .baseInfo(CommonConvertor.convertBaseInfoDTO2DO(src.getBaseInfo()))
      .build();
  }

  /**
   * 输入数据DO转DTO
   * 对应Go: InputDO2DTO
   */
  public static EvalTargetInputDataDTO convertInputDO2DTO(EvalTargetInputData src) {
    if (src == null) {
      return null;
    }
    return EvalTargetInputDataDTO.builder()
      .historyMessages(convertMessagesDO2DTO(src.getHistoryMessages()))
      .inputFields(convertContentDO2DTO(src.getInputFields()))
      .ext(src.getExt())
      .build();
  }

  /**
   * 输出数据DO转DTO
   * 对应Go: OutputDO2DTO
   */
  public static EvalTargetOutputDataDTO convertOutputDO2DTO(EvalTargetOutputData src) {
    if (src == null) {
      return null;
    }
    return EvalTargetOutputDataDTO.builder()
      .outputFields(convertContentDO2DTO(src.getOutputFields()))
      .evalTargetUsage(convertUsageDO2DTO(src.getEvalTargetUsage()))
      .evalTargetRunError(convertRunErrorDO2DTO(src.getEvalTargetRunError()))
      .timeConsumingMs(src.getTimeConsumingMs())
      .build();
  }

  /**
   * 输入数据DTO转DO
   * 对应Go: InputDTO2ToDO
   */
  public static EvalTargetInputData convertInputDTO2DO(EvalTargetInputDataDTO src) {
    if (src == null) {
      return null;
    }
    return EvalTargetInputData.builder()
      .historyMessages(convertMessagesDTO2DO(src.getHistoryMessages()))
      .inputFields(convertContentDTO2DO(src.getInputFields()))
      .ext(src.getExt())
      .build();
  }

  /**
   * 输出数据DTO转DO
   * 对应Go: OutputDTO2ToDO
   */
  public static EvalTargetOutputData convertOutputDTO2DO(EvalTargetOutputDataDTO src) {
    if (src == null) {
      return null;
    }
    return EvalTargetOutputData.builder()
      .outputFields(convertContentDTO2DO(src.getOutputFields()))
      .evalTargetUsage(convertUsageDTO2DO(src.getEvalTargetUsage()))
      .evalTargetRunError(convertRunErrorDTO2DO(src.getEvalTargetRunError()))
      .timeConsumingMs(src.getTimeConsumingMs())
      .build();
  }

  /**
   * 状态DO转DTO
   * 对应Go: StatusDO2DTO
   */
  public static EvalTargetRunStatusDTO convertStatusDO2DTO(EvalTargetRunStatus src) {
    if (src == null) {
      return null;
    }
    return EvalTargetRunStatusDTO.fromValue(src.getValue());
  }

  /**
   * 状态DTO转DO
   * 对应Go: StatusDTO2DO
   */
  public static EvalTargetRunStatus convertStatusDTO2DO(EvalTargetRunStatusDTO src) {
    if (src == null) {
      return null;
    }
    return EvalTargetRunStatus.fromValue(src.getValue());
  }

  // 辅助转换方法
  private static List<MessageDTO> convertMessagesDO2DTO(List<Message> src) {
    if (src == null || src.isEmpty()) {
      return List.of();
    }
    return src.stream()
      .map(CommonConvertor::convertMessageDO2DTO)
      .collect(Collectors.toList());
  }

  private static Map<String, ContentDTO> convertContentDO2DTO(Map<String, Content> src) {
    if (src == null || src.isEmpty()) {
      return new HashMap<>();
    }

    Map<String, ContentDTO> result = new HashMap<>();
    for (Map.Entry<String, Content> entry : src.entrySet()) {
      result.put(entry.getKey(), CommonConvertor.convertContentDO2DTO(entry.getValue()));
    }
    return result;
  }

  private static List<Message> convertMessagesDTO2DO(List<MessageDTO> src) {
    if (src == null || src.isEmpty()) {
      return List.of();
    }
    return src.stream()
      .filter(message -> message != null)
      .map(CommonConvertor::convertMessageDTO2DO)
      .collect(Collectors.toList());
  }

  private static Map<String, Content> convertContentDTO2DO(Map<String, ContentDTO> src) {
    if (src == null || src.isEmpty()) {
      return new HashMap<>();
    }

    Map<String, Content> result = new HashMap<>();
    for (Map.Entry<String, ContentDTO> entry : src.entrySet()) {
      if (entry.getValue() == null) {
        result.put(entry.getKey(), null);
        continue;
      }
      result.put(entry.getKey(), CommonConvertor.convertContentDTO2DO(entry.getValue()));
    }
    return result;
  }

  private static EvalTargetUsageDTO convertUsageDO2DTO(EvalTargetUsage src) {
    if (src == null) {
      return null;
    }
    return EvalTargetUsageDTO.builder()
      .inputTokens(src.getInputTokens())
      .outputTokens(src.getOutputTokens())
      .build();
  }

  private static EvalTargetRunErrorDTO convertRunErrorDO2DTO(EvalTargetRunError src) {
    if (src == null) {
      return null;
    }
    return EvalTargetRunErrorDTO.builder()
      .code(src.getCode())
      .message(src.getMessage())
      .build();
  }

  private static EvalTargetUsage convertUsageDTO2DO(EvalTargetUsageDTO src) {
    if (src == null) {
      return null;
    }
    return EvalTargetUsage.builder()
      .inputTokens(src.getInputTokens())
      .outputTokens(src.getOutputTokens())
      .build();
  }

  private static EvalTargetRunError convertRunErrorDTO2DO(EvalTargetRunErrorDTO src) {
    if (src == null) {
      return null;
    }
    return EvalTargetRunError.builder()
      .code(src.getCode())
      .message(src.getMessage())
      .build();
  }
}
