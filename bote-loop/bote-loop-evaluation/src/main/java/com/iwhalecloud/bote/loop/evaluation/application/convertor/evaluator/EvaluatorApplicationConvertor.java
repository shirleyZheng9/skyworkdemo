package com.iwhalecloud.bote.loop.evaluation.application.convertor.evaluator;

import com.iwhalecloud.bote.loop.client.evaluation.domain.common.ArgsSchemaDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.MessageDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorContentDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorTypeDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorVersionDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.PromptEvaluatorDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.PromptSourceTypeDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.ToolDTO;
import com.iwhalecloud.bote.loop.evaluation.application.convertor.common.CommonConvertor;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ArgsSchema;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Evaluator;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Message;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.PromptEvaluatorVersion;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.PromptSourceType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Tool;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 评估器应用转换器
 * 对应Go: evaluator/evaluator.go
 */
public final class EvaluatorApplicationConvertor {

  private EvaluatorApplicationConvertor() {
  }

  /**
   * DTO转DO
   * 对应Go: ConvertEvaluatorDTO2DO
   */
  public static Evaluator convertDTO2DO(EvaluatorDTO evaluatorDTO) {
    if (evaluatorDTO == null) {
      return null;
    }

    // 从DTO转换为DO
    Evaluator evaluatorDO = Evaluator.builder()
      .id(evaluatorDTO.getEvaluatorId())
      .spaceId(evaluatorDTO.getWorkspaceId())
      .name(evaluatorDTO.getName())
      .description(evaluatorDTO.getDescription())
      .draftSubmitted(evaluatorDTO.getDraftSubmitted())
      .evaluatorType(EvaluatorType.fromValue(evaluatorDTO.getEvaluatorType().getValue()))
      .latestVersion(evaluatorDTO.getLatestVersion())
      .catalogItemId(evaluatorDTO.getCatalogItemId())
      .baseInfo(CommonConvertor.convertBaseInfoDTO2DO(evaluatorDTO.getBaseInfo()))
      .build();

    if (evaluatorDTO.getCurrentVersion() != null) {
      if (Objects.requireNonNull(evaluatorDTO.getEvaluatorType()) == EvaluatorTypeDTO.PROMPT) {
        evaluatorDO.setPromptEvaluatorVersion(
          convertPromptEvaluatorVersionDTO2DO(evaluatorDO.getId(), evaluatorDO.getSpaceId(), evaluatorDTO.getCurrentVersion())
        );
      }
    }
    return evaluatorDO;
  }

  /**
   * DO列表转DTO列表
   * 对应Go: ConvertEvaluatorDOList2DTO
   */
  public static List<EvaluatorDTO> convertDOList2DTO(List<Evaluator> doList) {
    if (doList == null || doList.isEmpty()) {
      return List.of();
    }
    return doList.stream()
      .map(EvaluatorApplicationConvertor::convertDO2DTO)
      .collect(Collectors.toList());
  }

  /**
   * DO转DTO
   * 对应Go: ConvertEvaluatorDO2DTO
   */
  public static EvaluatorDTO convertDO2DTO(Evaluator doEntity) {
    if (doEntity == null) {
      return null;
    }

    EvaluatorDTO dto = EvaluatorDTO.builder()
      .evaluatorId(doEntity.getId())
      .workspaceId(doEntity.getSpaceId())
      .name(doEntity.getName())
      .description(doEntity.getDescription())
      .draftSubmitted(doEntity.getDraftSubmitted())
      .evaluatorType(EvaluatorTypeDTO.fromValue(doEntity.getEvaluatorType().getValue()))
      .latestVersion(doEntity.getLatestVersion())
      .catalogItemId(doEntity.getCatalogItemId())
      .baseInfo(CommonConvertor.convertBaseInfoDO2DTO(doEntity.getBaseInfo()))
      .build();

    if (Objects.requireNonNull(doEntity.getEvaluatorType()) == EvaluatorType.PROMPT) {
      if (doEntity.getPromptEvaluatorVersion() != null) {
        EvaluatorVersionDTO versionDTO = convertPromptEvaluatorVersionDO2DTO(doEntity.getPromptEvaluatorVersion());
        dto.setCurrentVersion(versionDTO);
      }
    }
    return dto;
  }

  /**
   * Prompt评估器版本DTO转DO
   * 对应Go: ConvertPromptEvaluatorVersionDTO2DO
   */
  public static PromptEvaluatorVersion convertPromptEvaluatorVersionDTO2DO(Long evaluatorId, Long spaceId, EvaluatorVersionDTO dto) {
    if (dto == null) {
      return null;
    }

    PromptEvaluatorVersion promptEvaluatorVersion = PromptEvaluatorVersion.builder()
      .id(dto.getId())
      .spaceId(spaceId)
      .evaluatorType(EvaluatorType.PROMPT)
      .evaluatorId(evaluatorId)
      .description(dto.getDescription())
      .version(dto.getVersion())
      .baseInfo(CommonConvertor.convertBaseInfoDTO2DO(dto.getBaseInfo()))
      .build();

    if (dto.getEvaluatorContent() != null) {
      applyEvaluatorContent(promptEvaluatorVersion, dto.getEvaluatorContent());
    }
    return promptEvaluatorVersion;
  }

  /**
   * 应用评估器内容到版本对象
   */
  private static void applyEvaluatorContent(PromptEvaluatorVersion version, EvaluatorContentDTO content) {
    version.setReceiveChatHistory(content.getReceiveChatHistory());
    applyInputSchemas(version, content);
    applyPromptEvaluator(version, content.getPromptEvaluator());
  }

  /**
   * 应用输入模式
   */
  private static void applyInputSchemas(PromptEvaluatorVersion version, EvaluatorContentDTO content) {
    if (content.getInputSchemas() != null && !content.getInputSchemas().isEmpty()) {
      List<ArgsSchema> inputSchemas = content.getInputSchemas().stream()
        .map(CommonConvertor::convertArgsSchemaDTO2DO)
        .collect(Collectors.toList());
      version.setInputSchemas(inputSchemas);
    }
  }

  /**
   * 应用Prompt评估器配置
   */
  private static void applyPromptEvaluator(PromptEvaluatorVersion version, PromptEvaluatorDTO promptEvaluator) {
    if (promptEvaluator == null) {
      return;
    }

    if (promptEvaluator.getPromptSourceType() != null) {
      version.setPromptSourceType(PromptSourceType.fromValue(promptEvaluator.getPromptSourceType().getValue()));
    }
    version.setPromptTemplateKey(promptEvaluator.getPromptTemplateKey());
    version.setModelConfig(CommonConvertor.convertModelConfigDTO2DO(promptEvaluator.getModelConfig()));

    applyMessageList(version, promptEvaluator);
    applyTools(version, promptEvaluator);
    version.setPassScore(promptEvaluator.getPassScore());
  }

  /**
   * 应用消息列表
   */
  private static void applyMessageList(PromptEvaluatorVersion version, PromptEvaluatorDTO promptEvaluator) {
    if (promptEvaluator.getMessageList() != null && !promptEvaluator.getMessageList().isEmpty()) {
      List<Message> messageList = promptEvaluator.getMessageList().stream()
        .map(CommonConvertor::convertMessageDTO2DO)
        .collect(Collectors.toList());
      version.setMessageList(messageList);
    }
  }

  /**
   * 应用工具列表
   */
  private static void applyTools(PromptEvaluatorVersion version, PromptEvaluatorDTO promptEvaluator) {
    if (promptEvaluator.getTools() != null && !promptEvaluator.getTools().isEmpty()) {
      List<Tool> tools = promptEvaluator.getTools().stream()
        .map(EvaluatorContentApplicationConvertor::convertToolDTO2DO)
        .collect(Collectors.toList());
      version.setTools(tools);
    }
  }

  /**
   * Prompt评估器版本DO转DTO
   * 对应Go: ConvertPromptEvaluatorVersionDO2DTO
   */
  public static EvaluatorVersionDTO convertPromptEvaluatorVersionDO2DTO(PromptEvaluatorVersion doEntity) {
    if (doEntity == null) {
      return null;
    }

    EvaluatorVersionDTO dto = EvaluatorVersionDTO.builder()
      .id(doEntity.getId())
      .version(doEntity.getVersion())
      .description(doEntity.getDescription())
      .baseInfo(CommonConvertor.convertBaseInfoDO2DTO(doEntity.getBaseInfo()))
      .evaluatorContent(EvaluatorContentDTO.builder()
        .receiveChatHistory(doEntity.getReceiveChatHistory())
        .promptEvaluator(PromptEvaluatorDTO.builder()
          .modelConfig(CommonConvertor.convertModelConfigDO2DTO(doEntity.getModelConfig()))
          .promptSourceType(doEntity.getPromptSourceType() != null ? PromptSourceTypeDTO.fromValue(doEntity.getPromptSourceType().getValue()) : null)
          .promptTemplateKey(doEntity.getPromptTemplateKey())
          .passScore(doEntity.getPassScore())
          .build())
        .build())
      .build();

    if (doEntity.getInputSchemas() != null && !doEntity.getInputSchemas().isEmpty()) {
      List<ArgsSchemaDTO> inputSchemas = doEntity.getInputSchemas().stream()
        .map(CommonConvertor::convertArgsSchemaDO2DTO)
        .collect(Collectors.toList());
      dto.getEvaluatorContent().setInputSchemas(inputSchemas);
    }

    if (doEntity.getMessageList() != null && !doEntity.getMessageList().isEmpty()) {
      List<MessageDTO> messageList = doEntity.getMessageList().stream()
        .map(CommonConvertor::convertMessageDO2DTO)
        .collect(Collectors.toList());
      dto.getEvaluatorContent().getPromptEvaluator().setMessageList(messageList);
    }

    if (doEntity.getTools() != null && !doEntity.getTools().isEmpty()) {
      List<ToolDTO> tools = doEntity.getTools().stream()
        .map(EvaluatorContentApplicationConvertor::convertToolDO2DTO)
        .collect(Collectors.toList());
      dto.getEvaluatorContent().getPromptEvaluator().setTools(tools);
    }

    return dto;
  }
}
