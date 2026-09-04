package com.iwhalecloud.bote.loop.evaluation.application.convertor.evaltarget;

import com.iwhalecloud.bote.loop.client.evaluation.domain.common.ArgsSchemaDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target.BotDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target.BotInfoTypeDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target.WorkflowDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target.EvalPromptDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target.EvalTargetContentDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target.EvalTargetDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target.EvalTargetTypeDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target.EvalTargetVersionDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target.SubmitStatusDTO;
import com.iwhalecloud.bote.loop.evaluation.application.convertor.common.CommonConvertor;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ArgsSchema;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Bot;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.BotInfoType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTarget;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetVersion;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.LoopPromptDO;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.SubmitStatus;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评估目标转换器
 * 对应Go: target/eval_target.go
 */
public final class EvalTargetConvertor {

  private EvalTargetConvertor() {
  }

  /**
   * DTO转DO
   * 对应Go: EvalTargetDTO2DO
   */
  public static EvalTarget convertDTO2DO(EvalTargetDTO targetDTO) {
    if (targetDTO == null) {
      return null;
    }

    EvalTarget targetDO = EvalTarget.builder()
      .id(targetDTO.getId())
      .spaceId(targetDTO.getWorkspaceId())
      .sourceTargetId(targetDTO.getSourceTargetId())
      .evalTargetType(EvalTargetType.fromValue(targetDTO.getEvalTargetType().getValue()))
      .baseInfo(CommonConvertor.convertBaseInfoDTO2DO(targetDTO.getBaseInfo()))
      .build();

    targetDO.setEvalTargetVersion(convertVersionDTO2DO(targetDTO.getEvalTargetVersion()));

    return targetDO;
  }

  /**
   * 版本DTO转DO
   * 对应Go: EvalTargetVersionDTO2DO
   */
  public static EvalTargetVersion convertVersionDTO2DO(EvalTargetVersionDTO targetVersionDTO) {
    if (targetVersionDTO == null) {
      return null;
    }

    EvalTargetVersion targetVersionDO = EvalTargetVersion.builder()
      .id(targetVersionDTO.getId())
      .spaceId(targetVersionDTO.getWorkspaceId())
      .targetId(targetVersionDTO.getTargetId())
      .sourceTargetVersion(targetVersionDTO.getSourceTargetVersion())
      .build();

    if (targetVersionDTO.getEvalTargetContent() != null) {
      // 转换输入模式
      List<ArgsSchema> inputSchemas = targetVersionDTO.getEvalTargetContent().getInputSchemas().stream()
        .map(CommonConvertor::convertArgsSchemaDTO2DO)
        .collect(Collectors.toList());
      targetVersionDO.setInputSchema(inputSchemas);

      // 转换输出模式
      List<ArgsSchema> outputSchemas = targetVersionDTO.getEvalTargetContent().getOutputSchemas().stream()
        .map(CommonConvertor::convertArgsSchemaDTO2DO)
        .collect(Collectors.toList());
      targetVersionDO.setOutputSchema(outputSchemas);

      // 转换Bot
      if (targetVersionDTO.getEvalTargetContent().getBot() != null) {
        BotDTO botDTO = targetVersionDTO.getEvalTargetContent().getBot();
        targetVersionDO.setBot(Bot.builder()
          .botId(botDTO.getBotId())
          .botVersion(botDTO.getBotVersion())
          .botInfoType(BotInfoType.fromValue(botDTO.getBotInfoType().getValue()))
          .botName(botDTO.getBotName())
          .avatarUrl(botDTO.getAvatarUrl())
          .description(botDTO.getDescription())
          .baseInfo(CommonConvertor.convertBaseInfoDTO2DO(botDTO.getBaseInfo()))
          .build());
      }

      // 转换Prompt
      if (targetVersionDTO.getEvalTargetContent().getPrompt() != null) {
        EvalPromptDTO promptDTO = targetVersionDTO.getEvalTargetContent().getPrompt();
        LoopPromptDO.LoopPromptDOBuilder builder = LoopPromptDO.builder()
          .promptId(promptDTO.getPromptId())
          .version(promptDTO.getVersion())
          .promptKey(promptDTO.getPromptKey())
          .name(promptDTO.getName())
          .description(promptDTO.getDescription());
        if (promptDTO.getSubmitStatus() != null) {
          builder.submitStatus(SubmitStatus.fromValue(promptDTO.getSubmitStatus().getValue()));
        }
        targetVersionDO.setPrompt(builder.build());
      }
    }

    return targetVersionDO;
  }

  /**
   * DO列表转DTO列表
   * 对应Go: EvalTargetListDO2DTO
   */
  public static List<EvalTargetDTO> convertListDO2DTO(List<EvalTarget> targetDOList) {
    if (targetDOList == null || targetDOList.isEmpty()) {
      return List.of();
    }
    return targetDOList.stream()
      .map(EvalTargetConvertor::convertDO2DTO)
      .collect(Collectors.toList());
  }

  /**
   * DO转DTO
   * 对应Go: EvalTargetDO2DTO
   */
  public static EvalTargetDTO convertDO2DTO(EvalTarget targetDO) {
    if (targetDO == null) {
      return null;
    }

    EvalTargetDTO targetDTO = EvalTargetDTO.builder()
      .id(targetDO.getId())
      .workspaceId(targetDO.getSpaceId())
      .sourceTargetId(targetDO.getSourceTargetId())
      .evalTargetType(EvalTargetTypeDTO.fromValue(targetDO.getEvalTargetType().getValue()))
      .build();

    if (targetDO.getEvalTargetVersion() != null) {
      // 填充version上的类型
      if (targetDO.getEvalTargetVersion().getEvalTargetType() == null) {
        targetDO.getEvalTargetVersion().setEvalTargetType(targetDO.getEvalTargetType());
      }
      targetDTO.setEvalTargetVersion(convertVersionDO2DTO(targetDO.getEvalTargetVersion()));
    }

    // 处理BaseInfo
    targetDTO.setBaseInfo(CommonConvertor.convertBaseInfoDO2DTO(targetDO.getBaseInfo()));
    return targetDTO;
  }

  /**
   * 版本DO转DTO
   * 对应Go: EvalTargetVersionDO2DTO
   */
  public static EvalTargetVersionDTO convertVersionDO2DTO(EvalTargetVersion targetVersionDO) {
    if (targetVersionDO == null) {
      return null;
    }

    EvalTargetVersionDTO targetVersionDTO = EvalTargetVersionDTO.builder()
      .id(targetVersionDO.getId())
      .workspaceId(targetVersionDO.getSpaceId())
      .targetId(targetVersionDO.getTargetId())
      .sourceTargetVersion(targetVersionDO.getSourceTargetVersion())
      .build();

    EvalTargetContentDTO contentDTO = convertEvalTargetContent(targetVersionDO);
    targetVersionDTO.setEvalTargetContent(contentDTO);

    // 填充输入输出模式
    if (targetVersionDO.getInputSchema() != null) {
      List<ArgsSchemaDTO> inputSchemas = targetVersionDO.getInputSchema().stream()
        .map(CommonConvertor::convertArgsSchemaDO2DTO)
        .collect(Collectors.toList());
      targetVersionDTO.getEvalTargetContent().setInputSchemas(inputSchemas);
    }

    if (targetVersionDO.getOutputSchema() != null) {
      List<ArgsSchemaDTO> outputSchemas = targetVersionDO.getOutputSchema().stream()
        .map(CommonConvertor::convertArgsSchemaDO2DTO)
        .collect(Collectors.toList());
      targetVersionDTO.getEvalTargetContent().setOutputSchemas(outputSchemas);
    }

    targetVersionDTO.setBaseInfo(CommonConvertor.convertBaseInfoDO2DTO(targetVersionDO.getBaseInfo()));

    return targetVersionDTO;
  }

  /**
   * 转换评估目标内容
   */
  private static EvalTargetContentDTO convertEvalTargetContent(EvalTargetVersion targetVersionDO) {
    EvalTargetContentDTO contentDTO = EvalTargetContentDTO.builder()
      .inputSchemas(List.of())
      .outputSchemas(List.of())
      .build();

    switch (targetVersionDO.getEvalTargetType()) {
      case BOT:
        convertBotContent(targetVersionDO, contentDTO);
        break;
      case LOOP_PROMPT:
        convertPromptContent(targetVersionDO, contentDTO);
        break;
      case WORKFLOW:
        convertWorkflowContent(targetVersionDO, contentDTO);
        break;
      default:
        // 保持默认的空内容
        break;
    }

    return contentDTO;
  }

  /**
   * 转换Bot内容
   */
  private static void convertBotContent(EvalTargetVersion targetVersionDO, EvalTargetContentDTO contentDTO) {
    if (targetVersionDO.getBot() != null) {
      BotDTO.BotDTOBuilder builder = BotDTO.builder()
        .botId(targetVersionDO.getBot().getBotId())
        .botVersion(targetVersionDO.getBot().getBotVersion())
        .botName(targetVersionDO.getBot().getBotName())
        .avatarUrl(targetVersionDO.getBot().getAvatarUrl())
        .description(targetVersionDO.getBot().getDescription())
        .baseInfo(CommonConvertor.convertBaseInfoDO2DTO(targetVersionDO.getBot().getBaseInfo()));
      if (targetVersionDO.getBot().getBotInfoType() != null) {
        builder.botInfoType(BotInfoTypeDTO.fromValue(targetVersionDO.getBot().getBotInfoType().getValue()));
      }
      contentDTO.setBot(builder.build());
    }
  }

  /**
   * 转换Prompt内容
   */
  private static void convertPromptContent(EvalTargetVersion targetVersionDO, EvalTargetContentDTO contentDTO) {
    if (targetVersionDO.getPrompt() != null) {
      EvalPromptDTO.EvalPromptDTOBuilder builder = EvalPromptDTO.builder()
        .promptId(targetVersionDO.getPrompt().getPromptId())
        .version(targetVersionDO.getPrompt().getVersion())
        .promptKey(targetVersionDO.getPrompt().getPromptKey())
        .name(targetVersionDO.getPrompt().getName())
        .description(targetVersionDO.getPrompt().getDescription());
      if (targetVersionDO.getPrompt().getSubmitStatus() != null) {
        builder.submitStatus(SubmitStatusDTO.fromValue(targetVersionDO.getPrompt().getSubmitStatus().getValue()));
      }
      contentDTO.setPrompt(builder.build());
    }
  }

  /**
   * 转换Workflow内容
   */
  private static void convertWorkflowContent(EvalTargetVersion targetVersionDO, EvalTargetContentDTO contentDTO) {
    if (targetVersionDO.getWorkflow() != null) {
      contentDTO.setWorkflow(WorkflowDTO.builder()
        .id(targetVersionDO.getWorkflow().getId())
        .version(targetVersionDO.getWorkflow().getVersion())
        .name(targetVersionDO.getWorkflow().getName())
        .avatarUrl(targetVersionDO.getWorkflow().getAvatarUrl())
        .description(targetVersionDO.getWorkflow().getDescription())
        .baseInfo(CommonConvertor.convertBaseInfoDO2DTO(targetVersionDO.getWorkflow().getBaseInfo()))
        .build());
    }
  }
}
