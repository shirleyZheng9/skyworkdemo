package com.iwhalecloud.bote.loop.prompt.application.convertor;

import com.iwhalecloud.bote.loop.client.prompt.openapi.dto.FunctionDTO;
import com.iwhalecloud.bote.loop.client.prompt.openapi.dto.LLMConfigDTO;
import com.iwhalecloud.bote.loop.client.prompt.openapi.dto.MessageDTO;
import com.iwhalecloud.bote.loop.client.prompt.openapi.dto.PromptDTO;
import com.iwhalecloud.bote.loop.client.prompt.openapi.dto.PromptTemplateDTO;
import com.iwhalecloud.bote.loop.client.prompt.openapi.dto.RoleDTO;
import com.iwhalecloud.bote.loop.client.prompt.openapi.dto.TemplateTypeDTO;
import com.iwhalecloud.bote.loop.client.prompt.openapi.dto.ToolCallConfigDTO;
import com.iwhalecloud.bote.loop.client.prompt.openapi.dto.ToolChoiceTypeDTO;
import com.iwhalecloud.bote.loop.client.prompt.openapi.dto.ToolDTO;
import com.iwhalecloud.bote.loop.client.prompt.openapi.dto.ToolTypeDTO;
import com.iwhalecloud.bote.loop.client.prompt.openapi.dto.VariableDefDTO;
import com.iwhalecloud.bote.loop.client.prompt.openapi.dto.VariableTypeDTO;
import com.iwhalecloud.bote.loop.prompt.domain.entity.Function;
import com.iwhalecloud.bote.loop.prompt.domain.entity.Message;
import com.iwhalecloud.bote.loop.prompt.domain.entity.ModelConfig;
import com.iwhalecloud.bote.loop.prompt.domain.entity.Prompt;
import com.iwhalecloud.bote.loop.prompt.domain.entity.PromptDetail;
import com.iwhalecloud.bote.loop.prompt.domain.entity.PromptTemplate;
import com.iwhalecloud.bote.loop.prompt.domain.entity.Tool;
import com.iwhalecloud.bote.loop.prompt.domain.entity.ToolCallConfig;
import com.iwhalecloud.bote.loop.prompt.domain.entity.VariableDef;
import java.util.ArrayList;
import java.util.List;

/**
 * OpenAPI转换器
 * 迁移对应关系: Go语言modules/prompt/application/convertor.OpenApiConvertor
 * - 功能: 将领域对象(DO)转换为数据传输对象(DTO)
 * - 主要方法:
 * * openApiPromptDO2DTO - Prompt DO转DTO
 * * openApiPromptTemplateDO2DTO - PromptTemplate DO转DTO
 * * openApiBatchMessageDO2DTO - 批量Message DO转DTO
 * * openApiMessageDO2DTO - Message DO转DTO
 * * openApiBatchVariableDefDO2DTO - 批量VariableDef DO转DTO
 * * openApiVariableDefDO2DTO - VariableDef DO转DTO
 * * openApiBatchToolDO2DTO - 批量Tool DO转DTO
 * * openApiToolDO2DTO - Tool DO转DTO
 * * openApiFunctionDO2DTO - Function DO转DTO
 * * openApiToolCallConfigDO2DTO - ToolCallConfig DO转DTO
 * * openApiModelConfigDO2DTO - ModelConfig DO转DTO
 * <p>
 * Java实现说明:
 * - 对应Go的convertor包中的转换函数
 * - 使用Java静态方法实现转换逻辑
 * - 处理空值检查和类型转换
 * - 支持批量转换操作
 * <p>
 * 技术栈迁移:
 * - Go函数 -> Java静态方法
 * - Go指针类型 -> Java对象引用
 * - Go切片类型 -> Java List
 * - Go nil检查 -> Java null检查
 * - Go ptr.Of -> Java直接赋值
 */
public final class OpenApiConvertor {

  private OpenApiConvertor() {
    // 工具类，禁止实例化
  }

  /**
   * Prompt DO转DTO
   * 迁移对应关系: Go语言convertor.OpenAPIPromptDO2DTO
   * - 功能: 将Prompt领域对象转换为OpenAPI DTO
   * - 参数: Prompt领域对象
   * - 返回: Prompt DTO
   */
  public static PromptDTO openApiPromptDO2DTO(Prompt prompt) {
    if (prompt == null) {
      return null;
    }

    PromptTemplate promptTemplate = null;
    List<Tool> tools = null;
    ToolCallConfig toolCallConfig = null;
    ModelConfig modelConfig = null;

    PromptDetail promptDetail = prompt.getPromptDetail();
    if (promptDetail != null) {
      promptTemplate = promptDetail.getPromptTemplate();
      tools = promptDetail.getTools();
      toolCallConfig = promptDetail.getToolCallConfig();
      modelConfig = promptDetail.getModelConfig();
    }

    PromptDTO dto = new PromptDTO();
    dto.setWorkspaceId(prompt.getSpaceId());
    dto.setPromptKey(prompt.getPromptKey());
    dto.setVersion(prompt.getVersion());
    dto.setPromptTemplate(openApiPromptTemplateDO2DTO(promptTemplate));
    dto.setTools(openApiBatchToolDO2DTO(tools));
    dto.setToolCallConfig(openApiToolCallConfigDO2DTO(toolCallConfig));
    dto.setLlmConfig(openApiModelConfigDO2DTO(modelConfig));
    return dto;
  }

  /**
   * PromptTemplate DO转DTO
   * 迁移对应关系: Go语言convertor.OpenAPIPromptTemplateDO2DTO
   * - 功能: 将PromptTemplate领域对象转换为OpenAPI DTO
   * - 参数: PromptTemplate领域对象
   * - 返回: PromptTemplate DTO
   */
  public static PromptTemplateDTO openApiPromptTemplateDO2DTO(PromptTemplate promptTemplate) {
    if (promptTemplate == null) {
      return null;
    }

    return PromptTemplateDTO.builder()
      .templateType(promptTemplate.getTemplateType() != null ? TemplateTypeDTO.fromValue(promptTemplate.getTemplateType().getValue()) : null)
      .messages(openApiBatchMessageDO2DTO(promptTemplate.getMessages()))
      .variableDefs(openApiBatchVariableDefDO2DTO(promptTemplate.getVariableDefs()))
      .build();
  }

  /**
   * 批量Message DO转DTO
   * 迁移对应关系: Go语言convertor.OpenAPIBatchMessageDO2DTO
   * - 功能: 批量将Message领域对象转换为OpenAPI DTO
   * - 参数: Message领域对象列表
   * - 返回: Message DTO列表
   */
  public static List<MessageDTO> openApiBatchMessageDO2DTO(List<Message> messages) {
    if (messages == null || messages.isEmpty()) {
      return null;
    }

    List<MessageDTO> messageDTOs = new ArrayList<>();
    for (Message message : messages) {
      if (message == null) {
        continue;
      }
      messageDTOs.add(openApiMessageDO2DTO(message));
    }
    return messageDTOs;
  }

  /**
   * Message DO转DTO
   * 迁移对应关系: Go语言convertor.OpenAPIMessageDO2DTO
   * - 功能: 将Message领域对象转换为OpenAPI DTO
   * - 参数: Message领域对象
   * - 返回: Message DTO
   */
  public static MessageDTO openApiMessageDO2DTO(Message message) {
    if (message == null) {
      return null;
    }

    return MessageDTO.builder()
      .role(message.getRole() != null ? RoleDTO.fromValue(message.getRole().getValue()) : null)
      .content(message.getContent())
      .build();
  }

  /**
   * 批量VariableDef DO转DTO
   * 迁移对应关系: Go语言convertor.OpenAPIBatchVariableDefDO2DTO
   * - 功能: 批量将VariableDef领域对象转换为OpenAPI DTO
   * - 参数: VariableDef领域对象列表
   * - 返回: VariableDef DTO列表
   */
  public static List<VariableDefDTO> openApiBatchVariableDefDO2DTO(List<VariableDef> variableDefs) {
    if (variableDefs == null || variableDefs.isEmpty()) {
      return null;
    }

    List<VariableDefDTO> variableDefDTOs = new ArrayList<>();
    for (VariableDef variableDef : variableDefs) {
      if (variableDef == null) {
        continue;
      }
      variableDefDTOs.add(openApiVariableDefDO2DTO(variableDef));
    }
    return variableDefDTOs;
  }

  /**
   * VariableDef DO转DTO
   * 迁移对应关系: Go语言convertor.OpenAPIVariableDefDO2DTO
   * - 功能: 将VariableDef领域对象转换为OpenAPI DTO
   * - 参数: VariableDef领域对象
   * - 返回: VariableDef DTO
   */
  public static VariableDefDTO openApiVariableDefDO2DTO(VariableDef variableDef) {
    if (variableDef == null) {
      return null;
    }

    return VariableDefDTO.builder()
      .key(variableDef.getKey())
      .desc(variableDef.getDesc())
      .type(variableDef.getType() != null ? VariableTypeDTO.fromValue(variableDef.getType().getValue()) : null)
      .build();
  }

  /**
   * 批量Tool DO转DTO
   * 迁移对应关系: Go语言convertor.OpenAPIBatchToolDO2DTO
   * - 功能: 批量将Tool领域对象转换为OpenAPI DTO
   * - 参数: Tool领域对象列表
   * - 返回: Tool DTO列表
   */
  public static List<ToolDTO> openApiBatchToolDO2DTO(List<Tool> tools) {
    if (tools == null || tools.isEmpty()) {
      return null;
    }

    List<ToolDTO> toolDTOs = new ArrayList<>();
    for (Tool tool : tools) {
      if (tool == null) {
        continue;
      }
      toolDTOs.add(openApiToolDO2DTO(tool));
    }
    return toolDTOs;
  }

  /**
   * Tool DO转DTO
   * 迁移对应关系: Go语言convertor.OpenAPIToolDO2DTO
   * - 功能: 将Tool领域对象转换为OpenAPI DTO
   * - 参数: Tool领域对象
   * - 返回: Tool DTO
   */
  public static ToolDTO openApiToolDO2DTO(Tool tool) {
    if (tool == null) {
      return null;
    }

    return ToolDTO.builder()
      .type(tool.getType() != null ? ToolTypeDTO.fromValue(tool.getType().getValue()) : null)
      .function(openApiFunctionDO2DTO(tool.getFunction()))
      .build();
  }

  /**
   * Function DO转DTO
   * 迁移对应关系: Go语言convertor.OpenAPIFunctionDO2DTO
   * - 功能: 将Function领域对象转换为OpenAPI DTO
   * - 参数: Function领域对象
   * - 返回: Function DTO
   */
  public static FunctionDTO openApiFunctionDO2DTO(Function function) {
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
   * ToolCallConfig DO转DTO
   * 迁移对应关系: Go语言convertor.OpenAPIToolCallConfigDO2DTO
   * - 功能: 将ToolCallConfig领域对象转换为OpenAPI DTO
   * - 参数: ToolCallConfig领域对象
   * - 返回: ToolCallConfig DTO
   */
  public static ToolCallConfigDTO openApiToolCallConfigDO2DTO(ToolCallConfig toolCallConfig) {
    if (toolCallConfig == null) {
      return null;
    }

    return ToolCallConfigDTO.builder()
      .toolChoice(toolCallConfig.getToolChoice() != null ? ToolChoiceTypeDTO.fromValue(toolCallConfig.getToolChoice().getValue()) : null)
      .build();
  }

  /**
   * ModelConfig DO转DTO
   * 迁移对应关系: Go语言convertor.OpenAPIModelConfigDO2DTO
   * - 功能: 将ModelConfig领域对象转换为OpenAPI DTO
   * - 参数: ModelConfig领域对象
   * - 返回: LLMConfig DTO
   */
  public static LLMConfigDTO openApiModelConfigDO2DTO(ModelConfig modelConfig) {
    if (modelConfig == null) {
      return null;
    }

    return LLMConfigDTO.builder()
      .maxTokens(modelConfig.getMaxTokens())
      .temperature(modelConfig.getTemperature())
      .topK(modelConfig.getTopK())
      .topP(modelConfig.getTopP())
      .presencePenalty(modelConfig.getPresencePenalty())
      .frequencyPenalty(modelConfig.getFrequencyPenalty())
      .jsonMode(modelConfig.getJsonMode())
      .build();
  }
}
