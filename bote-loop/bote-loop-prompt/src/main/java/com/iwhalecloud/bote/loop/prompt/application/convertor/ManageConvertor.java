package com.iwhalecloud.bote.loop.prompt.application.convertor;


import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.CommitInfoDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.ContentPartDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.ContentTypeDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.DebugToolCallDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.DraftInfoDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.FunctionCallDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.FunctionDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.ImageURLDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.MessageDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.ModelConfigDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.PromptBasicDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.PromptCommitDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.PromptDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.PromptDetailDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.PromptDraftDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.PromptTemplateDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.RoleDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.ScenarioDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.TemplateTypeDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.TokenUsageDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.ToolCallConfigDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.ToolCallDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.ToolChoiceTypeDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.ToolDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.ToolTypeDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.VariableDefDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.VariableTypeDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.VariableValDTO;
import com.iwhalecloud.bote.loop.prompt.domain.entity.CommitInfo;
import com.iwhalecloud.bote.loop.prompt.domain.entity.ContentPart;
import com.iwhalecloud.bote.loop.prompt.domain.entity.ContentType;
import com.iwhalecloud.bote.loop.prompt.domain.entity.DebugToolCall;
import com.iwhalecloud.bote.loop.prompt.domain.entity.DraftInfo;
import com.iwhalecloud.bote.loop.prompt.domain.entity.Function;
import com.iwhalecloud.bote.loop.prompt.domain.entity.FunctionCall;
import com.iwhalecloud.bote.loop.prompt.domain.entity.ImageURL;
import com.iwhalecloud.bote.loop.prompt.domain.entity.Message;
import com.iwhalecloud.bote.loop.prompt.domain.entity.ModelConfig;
import com.iwhalecloud.bote.loop.prompt.domain.entity.Prompt;
import com.iwhalecloud.bote.loop.prompt.domain.entity.PromptBasic;
import com.iwhalecloud.bote.loop.prompt.domain.entity.PromptCommit;
import com.iwhalecloud.bote.loop.prompt.domain.entity.PromptDetail;
import com.iwhalecloud.bote.loop.prompt.domain.entity.PromptDraft;
import com.iwhalecloud.bote.loop.prompt.domain.entity.PromptTemplate;
import com.iwhalecloud.bote.loop.prompt.domain.entity.Role;
import com.iwhalecloud.bote.loop.prompt.domain.entity.Scenario;
import com.iwhalecloud.bote.loop.prompt.domain.entity.TemplateType;
import com.iwhalecloud.bote.loop.prompt.domain.entity.TokenUsage;
import com.iwhalecloud.bote.loop.prompt.domain.entity.Tool;
import com.iwhalecloud.bote.loop.prompt.domain.entity.ToolCall;
import com.iwhalecloud.bote.loop.prompt.domain.entity.ToolCallConfig;
import com.iwhalecloud.bote.loop.prompt.domain.entity.ToolChoiceType;
import com.iwhalecloud.bote.loop.prompt.domain.entity.ToolType;
import com.iwhalecloud.bote.loop.prompt.domain.entity.VariableDef;
import com.iwhalecloud.bote.loop.prompt.domain.entity.VariableType;
import com.iwhalecloud.bote.loop.prompt.domain.entity.VariableVal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Prompt管理转换器
 * 迁移对应关系: Go语言modules/prompt/application/convertor.ManageConvertor
 * - 功能: 将领域对象(DO)与数据传输对象(DTO)之间进行转换
 * - 主要方法:
 * * promptDTO2DO - Prompt DTO转DO
 * * promptDO2DTO - Prompt DO转DTO
 * * promptDraftDTO2DO - PromptDraft DTO转DO
 * * draftInfoDTO2DO - DraftInfo DTO转DO
 * * promptCommitDTO2DO - PromptCommit DTO转DO
 * * promptCommitInfoDTO2DO - CommitInfo DTO转DO
 * * promptBasicDTO2DO - PromptBasic DTO转DO
 * * promptDetailDTO2DO - PromptDetail DTO转DO
 * * promptTemplateDTO2DO - PromptTemplate DTO转DO
 * * messageDTO2DO - Message DTO转DO
 * * contentPartDTO2DO - ContentPart DTO转DO
 * * variableDefDTO2DO - VariableDef DTO转DO
 * * toolDTO2DO - Tool DTO转DO
 * * functionDTO2DO - Function DTO转DO
 * * toolCallDTO2DO - ToolCall DTO转DO
 * * toolCallConfigDTO2DO - ToolCallConfig DTO转DO
 * * modelConfigDTO2DO - ModelConfig DTO转DO
 * * variableValDTO2DO - VariableVal DTO转DO
 * * 以及对应的DO转DTO方法
 * <p>
 * Java实现说明:
 * - 对应Go的convertor包中的转换函数
 * - 使用Java静态方法实现转换逻辑
 * - 处理空值检查和类型转换
 * - 支持批量转换操作
 * - 处理时间戳转换
 * <p>
 * 技术栈迁移:
 * - Go函数 -> Java静态方法
 * - Go指针类型 -> Java对象引用
 * - Go切片类型 -> Java List
 * - Go nil检查 -> Java null检查
 * - Go time.UnixMilli -> Java LocalDateTime转换
 * - Go ptr.Of -> Java直接赋值
 */
public final class ManageConvertor {

  private ManageConvertor() {
    // 工具类，禁止实例化
  }

  // ==================== DTO转DO方法 ====================

  /**
   * Prompt DTO转DO
   * 迁移对应关系: Go语言convertor.PromptDTO2DO
   */
  public static Prompt promptDTO2DO(PromptDTO dto) {
    if (dto == null) {
      return null;
    }

    return Prompt.builder()
      .id(dto.getId())
      .spaceId(dto.getWorkspaceId())
      .promptKey(dto.getPromptKey())
      .promptBasic(promptBasicDTO2DO(dto.getPromptBasic()))
      .promptDraft(promptDraftDTO2DO(dto.getPromptDraft()))
      .promptCommit(promptCommitDTO2DO(dto.getPromptCommit()))
      .catalogItemId(dto.getCatalogItemId())
      .build();
  }

  /**
   * PromptDraft DTO转DO
   * 迁移对应关系: Go语言convertor.PromptDraftDTO2DO
   */
  public static PromptDraft promptDraftDTO2DO(PromptDraftDTO dto) {
    if (dto == null) {
      return null;
    }

    return PromptDraft.builder()
      .promptDetail(promptDetailDTO2DO(dto.getDetail()))
      .draftInfo(draftInfoDTO2DO(dto.getDraftInfo()))
      .build();
  }

  /**
   * DraftInfo DTO转DO
   * 迁移对应关系: Go语言convertor.DraftInfoDTO2DO
   */
  public static DraftInfo draftInfoDTO2DO(DraftInfoDTO dto) {
    if (dto == null) {
      return null;
    }

    return DraftInfo.builder()
      .userId(dto.getUserId())
      .baseVersion(dto.getBaseVersion())
      .isModified(dto.getIsModified())
      .createdAt(convertTimestampToDate(dto.getCreatedAt()))
      .updatedAt(convertTimestampToDate(dto.getUpdatedAt()))
      .build();
  }

  /**
   * PromptCommit DTO转DO
   * 迁移对应关系: Go语言convertor.PromptCommitDTO2DO
   */
  public static PromptCommit promptCommitDTO2DO(PromptCommitDTO dto) {
    if (dto == null) {
      return null;
    }

    return PromptCommit.builder()
      .commitInfo(promptCommitInfoDTO2DO(dto.getCommitInfo()))
      .promptDetail(promptDetailDTO2DO(dto.getDetail()))
      .build();
  }

  /**
   * CommitInfo DTO转DO
   * 迁移对应关系: Go语言convertor.PromptCommitInfoDTO2DO
   */
  public static CommitInfo promptCommitInfoDTO2DO(CommitInfoDTO dto) {
    if (dto == null) {
      return null;
    }

    return CommitInfo.builder()
      .version(dto.getVersion())
      .baseVersion(dto.getBaseVersion())
      .description(dto.getDescription())
      .committedBy(dto.getCommittedBy())
      .committedAt(convertTimestampToDate(dto.getCommittedAt()))
      .build();
  }

  /**
   * PromptBasic DTO转DO
   * 迁移对应关系: Go语言convertor.PromptBasicDTO2DO
   */
  public static PromptBasic promptBasicDTO2DO(PromptBasicDTO dto) {
    if (dto == null) {
      return null;
    }

    return PromptBasic.builder()
      .displayName(dto.getDisplayName())
      .description(dto.getDescription())
      // 默认值为空字符串
      .latestVersion(dto.getLatestVersion() == null ? "" : dto.getLatestVersion())
      .createdBy(dto.getCreatedBy())
      .updatedBy(dto.getUpdatedBy())
      .createdAt(convertTimestampToDate(dto.getCreatedAt()))
      .updatedAt(convertTimestampToDate(dto.getUpdatedAt()))
      .promptType(dto.getPromptType())
      .build();
  }

  /**
   * PromptDetail DTO转DO
   * 迁移对应关系: Go语言convertor.PromptDetailDTO2DO
   */
  public static PromptDetail promptDetailDTO2DO(PromptDetailDTO dto) {
    if (dto == null) {
      return null;
    }

    return PromptDetail.builder()
      .promptTemplate(promptTemplateDTO2DO(dto.getPromptTemplate()))
      .tools(batchToolDTO2DO(dto.getTools()))
      .toolCallConfig(toolCallConfigDTO2DO(dto.getToolCallConfig()))
      .modelConfig(modelConfigDTO2DO(dto.getModelConfig()))
      .build();
  }

  /**
   * PromptTemplate DTO转DO
   * 迁移对应关系: Go语言convertor.PromptTemplateDTO2DO
   */
  public static PromptTemplate promptTemplateDTO2DO(PromptTemplateDTO dto) {
    if (dto == null) {
      return null;
    }

    return PromptTemplate.builder()
      .templateType(templateTypeDTO2DO(dto.getTemplateType()))
      .messages(batchMessageDTO2DO(dto.getMessages()))
      .variableDefs(batchVariableDefDTO2DO(dto.getVariableDefs()))
      .build();
  }

  /**
   * TemplateType DTO转DO
   * 迁移对应关系: Go语言convertor.TemplateTypeDTO2DO
   */
  public static TemplateType templateTypeDTO2DO(TemplateTypeDTO dto) {
    if (dto == null) {
      return TemplateType.NORMAL;
    }
    return switch (dto) {
      default -> TemplateType.NORMAL;
    };
  }

  /**
   * 批量Message DTO转DO
   * 迁移对应关系: Go语言convertor.BatchMessageDTO2DO
   */
  public static List<Message> batchMessageDTO2DO(List<MessageDTO> dtos) {
    List<Message> messages = new ArrayList<>();
    if (dtos == null || dtos.isEmpty()) {
      return messages;
    }
    for (MessageDTO dto : dtos) {
      if (dto == null) {
        continue;
      }
      messages.add(messageDTO2DO(dto));
    }
    return messages;
  }

  /**
   * Message DTO转DO
   * 迁移对应关系: Go语言convertor.MessageDTO2DO
   */
  public static Message messageDTO2DO(MessageDTO dto) {
    if (dto == null) {
      return null;
    }

    return Message.builder()
      .role(roleDTO2DO(dto.getRole()))
      .reasoningContent(dto.getReasoningContent())
      .content(dto.getContent())
      .parts(batchContentPartDTO2DO(dto.getParts()))
      .toolCallId(dto.getToolCallId())
      .toolCalls(batchToolCallDTO2DO(dto.getToolCalls()))
      .build();
  }

  /**
   * Role DTO转DO
   * 迁移对应关系: Go语言convertor.RoleDTO2DO
   */
  public static Role roleDTO2DO(RoleDTO role) {
    if (role == null) {
      return Role.USER;
    }
    return switch (role) {
      case SYSTEM -> Role.SYSTEM;
      case USER -> Role.USER;
      case ASSISTANT -> Role.ASSISTANT;
      case TOOL -> Role.TOOL;
      case PLACEHOLDER -> Role.PLACEHOLDER;
      default -> Role.USER;
    };
  }

  /**
   * 批量ContentPart DTO转DO
   * 迁移对应关系: Go语言convertor.BatchContentPartDTO2DO
   */
  public static List<ContentPart> batchContentPartDTO2DO(List<ContentPartDTO> dtos) {
    if (dtos == null || dtos.isEmpty()) {
      return null;
    }

    List<ContentPart> parts = new ArrayList<>();
    for (ContentPartDTO dto : dtos) {
      if (dto == null) {
        continue;
      }
      parts.add(contentPartDTO2DO(dto));
    }
    return parts;
  }

  /**
   * ContentPart DTO转DO
   * 迁移对应关系: Go语言convertor.ContentPartDTO2DO
   */
  public static ContentPart contentPartDTO2DO(ContentPartDTO dto) {
    if (dto == null) {
      return null;
    }

    return ContentPart.builder()
      .type(contentTypeDTO2DO(dto.getType()))
      .text(dto.getText())
      .imageUrl(imageUrlDTO2DO(dto.getImageUrl()))
      .build();
  }

  /**
   * ContentType DTO转DO
   * 迁移对应关系: Go语言convertor.ContentTypeDTO2DO
   */
  public static ContentType contentTypeDTO2DO(ContentTypeDTO dto) {
    if (dto == null) {
      return ContentType.TEXT;
    }
    return switch (dto) {
      case TEXT -> ContentType.TEXT;
      case IMAGE_URL -> ContentType.IMAGE_URL;
      default -> ContentType.TEXT;
    };
  }

  /**
   * ImageURL DTO转DO
   * 迁移对应关系: Go语言convertor.ImageURLDTO2DO
   */
  public static ImageURL imageUrlDTO2DO(ImageURLDTO dto) {
    if (dto == null) {
      return null;
    }

    return ImageURL.builder()
      .uri(dto.getUri())
      .url(dto.getUrl())
      .build();
  }

  /**
   * 批量VariableDef DTO转DO
   * 迁移对应关系: Go语言convertor.BatchVariableDefDTO2DO
   */
  public static List<VariableDef> batchVariableDefDTO2DO(List<VariableDefDTO> dtos) {
    if (dtos == null || dtos.isEmpty()) {
      return null;
    }

    List<VariableDef> variableDefs = new ArrayList<>();
    for (VariableDefDTO dto : dtos) {
      if (dto == null) {
        continue;
      }
      variableDefs.add(variableDefDTO2DO(dto));
    }
    return variableDefs;
  }

  /**
   * VariableDef DTO转DO
   * 迁移对应关系: Go语言convertor.VariableDefDTO2DO
   */
  public static VariableDef variableDefDTO2DO(VariableDefDTO dto) {
    if (dto == null) {
      return null;
    }

    return VariableDef.builder()
      .key(dto.getKey())
      .desc(dto.getDesc())
      .type(variableTypeDTO2DO(dto.getType()))
      .build();
  }

  /**
   * VariableType DTO转DO
   * 迁移对应关系: Go语言convertor.VariableTypeDTO2DO
   */
  public static VariableType variableTypeDTO2DO(VariableTypeDTO dto) {
    if (dto == null) {
      return VariableType.STRING;
    }
    return switch (dto) {
      case STRING -> VariableType.STRING;
      case PLACEHOLDER -> VariableType.PLACEHOLDER;
      default -> VariableType.STRING;
    };
  }

  /**
   * 批量Tool DTO转DO
   * 迁移对应关系: Go语言convertor.BatchToolDTO2DO
   */
  public static List<Tool> batchToolDTO2DO(List<ToolDTO> dtos) {
    if (dtos == null || dtos.isEmpty()) {
      return null;
    }

    List<Tool> tools = new ArrayList<>();
    for (ToolDTO dto : dtos) {
      if (dto == null) {
        continue;
      }
      tools.add(toolDTO2DO(dto));
    }
    return tools;
  }

  /**
   * Tool DTO转DO
   * 迁移对应关系: Go语言convertor.ToolDTO2DO
   */
  public static Tool toolDTO2DO(ToolDTO dto) {
    if (dto == null) {
      return null;
    }

    return Tool.builder()
      .type(toolTypeDTO2DO(dto.getType()))
      .function(functionDTO2DO(dto.getFunction()))
      .build();
  }

  /**
   * Function DTO转DO
   * 迁移对应关系: Go语言convertor.FunctionDTO2DO
   */
  public static Function functionDTO2DO(FunctionDTO dto) {
    if (dto == null) {
      return null;
    }

    return Function.builder()
      .name(dto.getName())
      .description(dto.getDescription())
      .parameters(dto.getParameters())
      .build();
  }

  /**
   * 批量ToolCall DTO转DO
   * 迁移对应关系: Go语言convertor.BatchToolCallDTO2DO
   */
  public static List<ToolCall> batchToolCallDTO2DO(List<ToolCallDTO> dtos) {
    if (dtos == null || dtos.isEmpty()) {
      return null;
    }

    List<ToolCall> toolCalls = new ArrayList<>();
    for (ToolCallDTO dto : dtos) {
      if (dto == null) {
        continue;
      }
      toolCalls.add(toolCallDTO2DO(dto));
    }
    return toolCalls;
  }

  /**
   * ToolCall DTO转DO
   * 迁移对应关系: Go语言convertor.ToolCallDTO2DO
   */
  public static ToolCall toolCallDTO2DO(ToolCallDTO dto) {
    if (dto == null) {
      return null;
    }

    return ToolCall.builder()
      .index(dto.getIndex())
      .id(dto.getId())
      .type(toolTypeDTO2DO(dto.getType()))
      .functionCall(functionCallDTO2DO(dto.getFunctionCall()))
      .build();
  }

  /**
   * ToolType DTO转DO
   * 迁移对应关系: Go语言convertor.ToolTypeDTO2DO
   */
  public static ToolType toolTypeDTO2DO(ToolTypeDTO dto) {
    if (dto == null) {
      return ToolType.FUNCTION;
    }
    switch (dto) {
      default:
        return ToolType.FUNCTION;
    }
  }

  /**
   * FunctionCall DTO转DO
   * 迁移对应关系: Go语言convertor.FunctionCallDTO2DO
   */
  public static FunctionCall functionCallDTO2DO(FunctionCallDTO dto) {
    if (dto == null) {
      return null;
    }

    return FunctionCall.builder()
      .name(dto.getName())
      .arguments(dto.getArguments())
      .build();
  }

  /**
   * ToolCallConfig DTO转DO
   * 迁移对应关系: Go语言convertor.ToolCallConfigDTO2DO
   */
  public static ToolCallConfig toolCallConfigDTO2DO(ToolCallConfigDTO dto) {
    if (dto == null) {
      return null;
    }

    return ToolCallConfig.builder()
      .toolChoice(toolChoiceTypeDTO2DO(dto.getToolChoice()))
      .build();
  }

  /**
   * ToolChoiceType DTO转DO
   * 迁移对应关系: Go语言convertor.ToolChoiceTypeDTO2DO
   */
  public static ToolChoiceType toolChoiceTypeDTO2DO(ToolChoiceTypeDTO dto) {
    if (dto == null) {
      return ToolChoiceType.AUTO;
    }
    switch (dto) {
      case NONE:
        return ToolChoiceType.NONE;
      case AUTO:
        return ToolChoiceType.AUTO;
      default:
        return ToolChoiceType.AUTO;
    }
  }

  /**
   * ModelConfig DTO转DO
   * 迁移对应关系: Go语言convertor.ModelConfigDTO2DO
   */
  public static ModelConfig modelConfigDTO2DO(ModelConfigDTO dto) {
    if (dto == null) {
      return null;
    }

    return ModelConfig.builder()
      .modelId(dto.getModelId())
      .maxTokens(dto.getMaxTokens())
      .temperature(dto.getTemperature())
      .topK(dto.getTopK())
      .topP(dto.getTopP())
      .presencePenalty(dto.getPresencePenalty())
      .frequencyPenalty(dto.getFrequencyPenalty())
      .jsonMode(dto.getJsonMode())
      .build();
  }

  /**
   * 批量VariableVal DTO转DO
   * 迁移对应关系: Go语言convertor.BatchVariableValDTO2DO
   */
  public static List<VariableVal> batchVariableValDTO2DO(List<VariableValDTO> dtos) {
    if (dtos == null || dtos.isEmpty()) {
      return null;
    }

    List<VariableVal> variableVals = new ArrayList<>();
    for (VariableValDTO dto : dtos) {
      if (dto == null) {
        continue;
      }
      variableVals.add(variableValDTO2DO(dto));
    }
    return variableVals;
  }

  /**
   * VariableVal DTO转DO
   * 迁移对应关系: Go语言convertor.VariableValDTO2DO
   */
  public static VariableVal variableValDTO2DO(VariableValDTO dto) {
    if (dto == null) {
      return null;
    }

    return VariableVal.builder()
      .key(dto.getKey())
      .value(dto.getValue())
      .placeholderMessages(batchMessageDTO2DO(dto.getPlaceholderMessages()))
      .build();
  }

  /**
   * Scenario DTO转DO
   * 迁移对应关系: Go语言convertor.ScenarioDTO2DO
   */
  public static Scenario scenarioDTO2DO(ScenarioDTO dto) {
    if (dto == null) {
      return Scenario.DEFAULT;
    }
    switch (dto) {
      case EVAL_TARGET:
        return Scenario.EVAL_TARGET;
      default:
        return Scenario.DEFAULT;
    }
  }

  // ==================== DO转DTO方法 ====================

  /**
   * Role DO转DTO
   * 迁移对应关系: Go语言convertor.RoleDO2DTO
   */
  public static RoleDTO roleDO2DTO(Role role) {
    if (role == null) {
      return RoleDTO.USER;
    }
    switch (role) {
      case SYSTEM:
        return RoleDTO.SYSTEM;
      case USER:
        return RoleDTO.USER;
      case ASSISTANT:
        return RoleDTO.ASSISTANT;
      case TOOL:
        return RoleDTO.TOOL;
      case PLACEHOLDER:
        return RoleDTO.PLACEHOLDER;
      default:
        return RoleDTO.USER;
    }
  }

  /**
   * 批量ToolCall DO转DTO
   * 迁移对应关系: Go语言convertor.BatchToolCallDO2DTO
   */
  public static List<ToolCallDTO> batchToolCallDO2DTO(List<ToolCall> toolCalls) {
    if (toolCalls == null || toolCalls.isEmpty()) {
      return null;
    }

    List<ToolCallDTO> dtos = new ArrayList<>();
    for (ToolCall toolCall : toolCalls) {
      if (toolCall == null) {
        continue;
      }
      dtos.add(toolCallDO2DTO(toolCall));
    }
    return dtos;
  }

  /**
   * ToolCall DO转DTO
   * 迁移对应关系: Go语言convertor.ToolCallDO2DTO
   */
  public static ToolCallDTO toolCallDO2DTO(ToolCall toolCall) {
    if (toolCall == null) {
      return null;
    }

    return ToolCallDTO.builder()
      .index(toolCall.getIndex())
      .id(toolCall.getId())
      .type(toolTypeDO2DTO(toolCall.getType()))
      .functionCall(functionCallDO2DTO(toolCall.getFunctionCall()))
      .build();
  }

  /**
   * ToolType DO转DTO
   * 迁移对应关系: Go语言convertor.ToolTypeDO2DTO
   */
  public static ToolTypeDTO toolTypeDO2DTO(ToolType toolType) {
    if (toolType == null) {
      return ToolTypeDTO.FUNCTION;
    }
    switch (toolType) {
      default:
        return ToolTypeDTO.FUNCTION;
    }
  }

  /**
   * FunctionCall DO转DTO
   * 迁移对应关系: Go语言convertor.FunctionCallDO2DTO
   */
  public static FunctionCallDTO functionCallDO2DTO(FunctionCall functionCall) {
    if (functionCall == null) {
      return null;
    }

    return FunctionCallDTO.builder()
      .name(functionCall.getName())
      .arguments(functionCall.getArguments())
      .build();
  }

  /**
   * TokenUsage DO转DTO
   * 迁移对应关系: Go语言convertor.TokenUsageDO2DTO
   */
  public static TokenUsageDTO tokenUsageDO2DTO(TokenUsage tokenUsage) {
    if (tokenUsage == null) {
      return null;
    }

    return TokenUsageDTO.builder()
      .inputTokens(tokenUsage.getInputTokens())
      .outputTokens(tokenUsage.getOutputTokens())
      .build();
  }

  /**
   * 批量ContentPart DO转DTO
   * 迁移对应关系: Go语言convertor.BatchContentPartDO2DTO
   */
  public static List<ContentPartDTO> batchContentPartDO2DTO(List<ContentPart> parts) {
    if (parts == null || parts.isEmpty()) {
      return null;
    }

    List<ContentPartDTO> dtos = new ArrayList<>();
    for (ContentPart part : parts) {
      if (part == null) {
        continue;
      }
      dtos.add(contentPartDO2DTO(part));
    }
    return dtos;
  }

  /**
   * ContentPart DO转DTO
   * 迁移对应关系: Go语言convertor.ContentPartDO2DTO
   */
  public static ContentPartDTO contentPartDO2DTO(ContentPart part) {
    if (part == null) {
      return null;
    }

    return ContentPartDTO.builder()
      .type(contentTypeDO2DTO(part.getType()))
      .text(part.getText())
      .imageUrl(imageUrlDO2DTO(part.getImageUrl()))
      .build();
  }

  /**
   * ContentType DO转DTO
   * 迁移对应关系: Go语言convertor.ContentTypeDO2DTO
   */
  public static ContentTypeDTO contentTypeDO2DTO(ContentType contentType) {
    if (contentType == null) {
      return ContentTypeDTO.TEXT;
    }
    return switch (contentType) {
      case TEXT -> ContentTypeDTO.TEXT;
      case IMAGE_URL -> ContentTypeDTO.IMAGE_URL;
      default -> ContentTypeDTO.TEXT;
    };
  }

  /**
   * ImageURL DO转DTO
   * 迁移对应关系: Go语言convertor.ImageURLDO2DTO
   */
  public static ImageURLDTO imageUrlDO2DTO(ImageURL imageUrl) {
    if (imageUrl == null) {
      return null;
    }

    return ImageURLDTO.builder()
      .uri(imageUrl.getUri())
      .url(imageUrl.getUrl())
      .build();
  }

  /**
   * 批量DebugToolCall DO转DTO
   * 迁移对应关系: Go语言convertor.BatchDebugToolCallDO2DTO
   */
  public static List<DebugToolCallDTO> batchDebugToolCallDO2DTO(List<DebugToolCall> toolCalls) {
    if (toolCalls == null || toolCalls.isEmpty()) {
      return null;
    }

    List<DebugToolCallDTO> dtos = new ArrayList<>();
    for (DebugToolCall toolCall : toolCalls) {
      if (toolCall == null) {
        continue;
      }
      dtos.add(debugToolCallDO2DTO(toolCall));
    }
    return dtos;
  }

  /**
   * DebugToolCall DO转DTO
   * 迁移对应关系: Go语言convertor.DebugToolCallDO2DTO
   */
  public static DebugToolCallDTO debugToolCallDO2DTO(DebugToolCall toolCall) {
    if (toolCall == null) {
      return null;
    }

    return DebugToolCallDTO.builder()
      .toolCall(toolCallDO2DTO(toolCall.getToolCall()))
      .mockResponse(toolCall.getMockResponse())
      .debugTraceKey(toolCall.getDebugTraceKey())
      .build();
  }

  /**
   * 批量VariableVal DO转DTO
   * 迁移对应关系: Go语言convertor.BatchVariableValDO2DTO
   */
  public static List<VariableValDTO> batchVariableValDO2DTO(List<VariableVal> variableVals) {
    if (variableVals == null || variableVals.isEmpty()) {
      return null;
    }

    List<VariableValDTO> dtos = new ArrayList<>();
    for (VariableVal variableVal : variableVals) {
      if (variableVal == null) {
        continue;
      }
      dtos.add(variableValDO2DTO(variableVal));
    }
    return dtos;
  }

  /**
   * VariableVal DO转DTO
   * 迁移对应关系: Go语言convertor.VariableValDO2DTO
   */
  public static VariableValDTO variableValDO2DTO(VariableVal variableVal) {
    if (variableVal == null) {
      return null;
    }

    return VariableValDTO.builder()
      .key(variableVal.getKey())
      .value(variableVal.getValue())
      .placeholderMessages(batchMessageDO2DTO(variableVal.getPlaceholderMessages()))
      .build();
  }

  /**
   * 批量Message DO转DTO
   * 迁移对应关系: Go语言convertor.BatchMessageDO2DTO
   */
  public static List<MessageDTO> batchMessageDO2DTO(List<Message> messages) {
    if (messages == null || messages.isEmpty()) {
      return null;
    }

    List<MessageDTO> dtos = new ArrayList<>();
    for (Message message : messages) {
      if (message == null) {
        continue;
      }
      dtos.add(messageDO2DTO(message));
    }
    return dtos;
  }

  /**
   * Message DO转DTO
   * 迁移对应关系: Go语言convertor.MessageDO2DTO
   */
  public static MessageDTO messageDO2DTO(Message message) {
    if (message == null) {
      return null;
    }

    return MessageDTO.builder()
      .role(roleDO2DTO(message.getRole()))
      .reasoningContent(message.getReasoningContent())
      .content(message.getContent())
      .parts(batchContentPartDO2DTO(message.getParts()))
      .toolCallId(message.getToolCallId())
      .toolCalls(batchToolCallDO2DTO(message.getToolCalls()))
      .build();
  }

  /**
   * 批量Prompt DO转DTO
   * 迁移对应关系: Go语言convertor.BatchPromptDO2DTO
   */
  public static List<PromptDTO> batchPromptDO2DTO(List<Prompt> prompts) {
    if (prompts == null || prompts.isEmpty()) {
      return Collections.emptyList();
    }

    List<PromptDTO> dtos = new ArrayList<>();
    for (Prompt prompt : prompts) {
      if (prompt == null) {
        continue;
      }
      dtos.add(promptDO2DTO(prompt));
    }
    return dtos;
  }

  /**
   * Prompt DO转DTO
   * 迁移对应关系: Go语言convertor.PromptDO2DTO
   */
  public static PromptDTO promptDO2DTO(Prompt prompt) {
    if (prompt == null) {
      return null;
    }

    return PromptDTO.builder()
      .id(prompt.getId())
      .workspaceId(prompt.getSpaceId())
      .promptKey(prompt.getPromptKey())
      .promptBasic(promptBasicDO2DTO(prompt.getPromptBasic()))
      .promptCommit(promptCommitDO2DTO(prompt.getPromptCommit()))
      .promptDraft(promptDraftDO2DTO(prompt.getPromptDraft()))
      .catalogItemId(prompt.getCatalogItemId())
      .build();
  }

  /**
   * PromptDraft DO转DTO
   * 迁移对应关系: Go语言convertor.PromptDraftDO2DTO
   */
  public static PromptDraftDTO promptDraftDO2DTO(PromptDraft promptDraft) {
    if (promptDraft == null) {
      return null;
    }

    return PromptDraftDTO.builder()
      .draftInfo(draftInfoDO2DTO(promptDraft.getDraftInfo()))
      .detail(promptDetailDO2DTO(promptDraft.getPromptDetail()))
      .build();
  }

  /**
   * DraftInfo DO转DTO
   * 迁移对应关系: Go语言convertor.DraftInfoDO2DTO
   */
  public static DraftInfoDTO draftInfoDO2DTO(DraftInfo draftInfo) {
    if (draftInfo == null) {
      return null;
    }

    return DraftInfoDTO.builder()
      .userId(draftInfo.getUserId())
      .baseVersion(draftInfo.getBaseVersion())
      .isModified(draftInfo.getIsModified())
      .createdAt(convertDateToTimestamp(draftInfo.getCreatedAt()))
      .updatedAt(convertDateToTimestamp(draftInfo.getUpdatedAt()))
      .build();
  }

  /**
   * PromptBasic DO转DTO
   * 迁移对应关系: Go语言convertor.PromptBasicDO2DTO
   */
  public static PromptBasicDTO promptBasicDO2DTO(PromptBasic promptBasic) {
    if (promptBasic == null) {
      return null;
    }

    return PromptBasicDTO.builder()
      .displayName(promptBasic.getDisplayName())
      .description(promptBasic.getDescription())
      .latestVersion(promptBasic.getLatestVersion())
      .createdBy(promptBasic.getCreatedBy())
      .updatedBy(promptBasic.getUpdatedBy())
      .createdAt(convertDateToTimestamp(promptBasic.getCreatedAt()))
      .updatedAt(convertDateToTimestamp(promptBasic.getUpdatedAt()))
      .latestCommittedAt(convertDateToTimestamp(promptBasic.getLatestCommittedAt()))
      .promptType(promptBasic.getPromptType())
      .build();
  }

  /**
   * PromptCommit DO转DTO
   * 迁移对应关系: Go语言convertor.PromptCommitDO2DTO
   */
  public static PromptCommitDTO promptCommitDO2DTO(PromptCommit promptCommit) {
    if (promptCommit == null) {
      return null;
    }

    return PromptCommitDTO.builder()
      .commitInfo(commitInfoDO2DTO(promptCommit.getCommitInfo()))
      .detail(promptDetailDO2DTO(promptCommit.getPromptDetail()))
      .build();
  }

  /**
   * 批量CommitInfo DO转DTO
   * 迁移对应关系: Go语言convertor.BatchCommitInfoDO2DTO
   */
  public static List<CommitInfoDTO> batchCommitInfoDO2DTO(List<CommitInfo> commitInfos) {
    if (commitInfos == null || commitInfos.isEmpty()) {
      return null;
    }

    List<CommitInfoDTO> dtos = new ArrayList<>();
    for (CommitInfo commitInfo : commitInfos) {
      if (commitInfo == null) {
        continue;
      }
      dtos.add(commitInfoDO2DTO(commitInfo));
    }
    return dtos;
  }

  /**
   * CommitInfo DO转DTO
   * 迁移对应关系: Go语言convertor.CommitInfoDO2DTO
   */
  public static CommitInfoDTO commitInfoDO2DTO(CommitInfo commitInfo) {
    if (commitInfo == null) {
      return null;
    }

    return CommitInfoDTO.builder()
      .version(commitInfo.getVersion())
      .baseVersion(commitInfo.getBaseVersion())
      .description(commitInfo.getDescription())
      .committedBy(commitInfo.getCommittedBy())
      .committedAt(convertDateToTimestamp(commitInfo.getCommittedAt()))
      .build();
  }

  /**
   * PromptDetail DO转DTO
   * 迁移对应关系: Go语言convertor.PromptDetailDO2DTO
   */
  public static PromptDetailDTO promptDetailDO2DTO(PromptDetail promptDetail) {
    if (promptDetail == null) {
      return null;
    }

    return PromptDetailDTO.builder()
      .promptTemplate(promptTemplateDO2DTO(promptDetail.getPromptTemplate()))
      .tools(batchToolDO2DTO(promptDetail.getTools()))
      .toolCallConfig(toolCallConfigDO2DTO(promptDetail.getToolCallConfig()))
      .modelConfig(modelConfigDO2DTO(promptDetail.getModelConfig()))
      .build();
  }

  /**
   * ModelConfig DO转DTO
   * 迁移对应关系: Go语言convertor.ModelConfigDO2DTO
   */
  public static ModelConfigDTO modelConfigDO2DTO(ModelConfig modelConfig) {
    if (modelConfig == null) {
      return null;
    }

    return ModelConfigDTO.builder()
      .modelId(modelConfig.getModelId())
      .maxTokens(modelConfig.getMaxTokens())
      .temperature(modelConfig.getTemperature())
      .topK(modelConfig.getTopK())
      .topP(modelConfig.getTopP())
      .presencePenalty(modelConfig.getPresencePenalty())
      .frequencyPenalty(modelConfig.getFrequencyPenalty())
      .jsonMode(modelConfig.getJsonMode())
      .build();
  }

  /**
   * ToolCallConfig DO转DTO
   * 迁移对应关系: Go语言convertor.ToolCallConfigDO2DTO
   */
  public static ToolCallConfigDTO toolCallConfigDO2DTO(ToolCallConfig toolCallConfig) {
    if (toolCallConfig == null) {
      return null;
    }

    return ToolCallConfigDTO.builder()
      .toolChoice(ToolChoiceTypeDTO.fromValue(toolCallConfig.getToolChoice().getValue()))
      .build();
  }

  /**
   * 批量Tool DO转DTO
   * 迁移对应关系: Go语言convertor.BatchToolDO2DTO
   */
  public static List<ToolDTO> batchToolDO2DTO(List<Tool> tools) {
    if (tools == null || tools.isEmpty()) {
      return null;
    }

    List<ToolDTO> dtos = new ArrayList<>();
    for (Tool tool : tools) {
      if (tool == null) {
        continue;
      }
      dtos.add(toolDO2DTO(tool));
    }
    return dtos;
  }

  /**
   * Tool DO转DTO
   * 迁移对应关系: Go语言convertor.ToolDO2DTO
   */
  public static ToolDTO toolDO2DTO(Tool tool) {
    if (tool == null) {
      return null;
    }

    return ToolDTO.builder()
      .type(ToolTypeDTO.fromValue(tool.getType().getValue()))
      .function(functionDO2DTO(tool.getFunction()))
      .build();
  }

  /**
   * Function DO转DTO
   * 迁移对应关系: Go语言convertor.FunctionDO2DTO
   */
  public static FunctionDTO functionDO2DTO(Function function) {
    if (function == null) {
      return null;
    }

    return FunctionDTO.builder()
      .name(function.getName())
      .description(function.getDescription())
      .parameters(function.getParameters())
      .build();
  }

  /**
   * PromptTemplate DO转DTO
   * 迁移对应关系: Go语言convertor.PromptTemplateDO2DTO
   */
  public static PromptTemplateDTO promptTemplateDO2DTO(PromptTemplate promptTemplate) {
    if (promptTemplate == null) {
      return null;
    }

    return PromptTemplateDTO.builder()
      .templateType(TemplateTypeDTO.fromValue(promptTemplate.getTemplateType().getValue()))
      .messages(batchMessageDO2DTO(promptTemplate.getMessages()))
      .variableDefs(batchVariableDefDO2DTO(promptTemplate.getVariableDefs()))
      .build();
  }

  /**
   * 批量VariableDef DO转DTO
   * 迁移对应关系: Go语言convertor.BatchVariableDefDO2DTO
   */
  public static List<VariableDefDTO> batchVariableDefDO2DTO(List<VariableDef> variableDefs) {
    if (variableDefs == null || variableDefs.isEmpty()) {
      return null;
    }

    List<VariableDefDTO> dtos = new ArrayList<>();
    for (VariableDef variableDef : variableDefs) {
      if (variableDef == null) {
        continue;
      }
      dtos.add(variableDefDO2DTO(variableDef));
    }
    return dtos;
  }

  /**
   * VariableDef DO转DTO
   * 迁移对应关系: Go语言convertor.VariableDefDO2DTO
   */
  public static VariableDefDTO variableDefDO2DTO(VariableDef variableDef) {
    if (variableDef == null) {
      return null;
    }

    return VariableDefDTO.builder()
      .key(variableDef.getKey())
      .desc(variableDef.getDesc())
      .type(VariableTypeDTO.fromValue(variableDef.getType().getValue()))
      .build();
  }

  // ==================== 辅助方法 ====================

  /**
   * 时间戳转Date
   */
  private static Date convertTimestampToDate(Long timestamp) {
    if (timestamp == null) {
      return null;
    }
    return new Date(timestamp);
  }

  /**
   * Date转时间
   */
  private static Long convertDateToTimestamp(Date dateTime) {
    if (dateTime == null) {
      return null;
    }
    return dateTime.getTime();
  }
}
