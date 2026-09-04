package com.iwhalecloud.bote.loop.evaluation.application.convertor.evaluator;

import com.iwhalecloud.bote.loop.client.evaluation.domain.common.ContentDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.MessageDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorInputDataDTO;
import com.iwhalecloud.bote.loop.evaluation.application.convertor.common.CommonConvertor;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Content;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorInputData;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Message;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评估器输入数据转换器
 * 对应Go: evaluator/evaluator_input_data.go
 */
public final class EvaluatorInputDataApplicationConvertor {

  private EvaluatorInputDataApplicationConvertor() {

  }

  /**
   * DTO转DO
   * 对应Go: ConvertEvaluatorInputDataDTO2DO
   */
  public static EvaluatorInputData convertDTO2DO(EvaluatorInputDataDTO dto) {
    if (dto == null) {
      return null;
    }

    // 转换 HistoryMessages
    List<Message> historyMessages = null;
    if (dto.getHistoryMessages() != null) {
      historyMessages = dto.getHistoryMessages().stream()
        .map(CommonConvertor::convertMessageDTO2DO)
        .collect(Collectors.toList());
    }

    // 转换 InputFields
    Map<String, Content> inputFields = new HashMap<>();
    if (dto.getInputFields() != null) {
      for (Map.Entry<String, ContentDTO> entry : dto.getInputFields().entrySet()) {
        Content contentDO = CommonConvertor.convertContentDTO2DO(entry.getValue());
        inputFields.put(entry.getKey(), contentDO);
      }
    }

    return EvaluatorInputData.builder()
      .historyMessages(historyMessages)
      .inputFields(inputFields)
      .build();
  }

  /**
   * DO转DTO
   * 对应Go: ConvertEvaluatorInputDataDO2DTO
   */
  public static EvaluatorInputDataDTO convertDO2DTO(EvaluatorInputData doEntity) {
    if (doEntity == null) {
      return null;
    }

    // 转换 HistoryMessages
    List<MessageDTO> historyMessages = null;
    if (doEntity.getHistoryMessages() != null) {
      historyMessages = doEntity.getHistoryMessages().stream()
        .map(CommonConvertor::convertMessageDO2DTO)
        .collect(Collectors.toList());
    }

    // 转换 InputFields
    Map<String, ContentDTO> inputFields = new HashMap<>();
    if (doEntity.getInputFields() != null) {
      for (Map.Entry<String, Content> entry : doEntity.getInputFields().entrySet()) {
        ContentDTO contentDTO = CommonConvertor.convertContentDO2DTO(entry.getValue());
        inputFields.put(entry.getKey(), contentDTO);
      }
    }

    return EvaluatorInputDataDTO.builder()
      .historyMessages(historyMessages)
      .inputFields(inputFields)
      .build();
  }
}
