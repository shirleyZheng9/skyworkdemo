package com.iwhalecloud.bote.loop.client.prompt.openapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Prompt DTO
 * 对应Thrift: Prompt
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Prompt DTO")
public class PromptDTO {

  @Schema(description = "工作空间ID")
  private Long workspaceId;

  @Schema(description = "Prompt键")
  private String promptKey;

  @Schema(description = "版本")
  private String version;

  @Schema(description = "Prompt模板")
  private PromptTemplateDTO promptTemplate;

  @Schema(description = "工具列表")
  private List<ToolDTO> tools;

  @Schema(description = "工具调用配置")
  private ToolCallConfigDTO toolCallConfig;

  @Schema(description = "LLM配置")
  private LLMConfigDTO llmConfig;
}
