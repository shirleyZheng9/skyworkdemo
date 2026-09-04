package com.iwhalecloud.bote.loop.client.prompt.domain.prompt;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Prompt详细信息DTO
 * 迁移对应关系: Thrift struct PromptDetail
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Prompt详细信息DTO")
public class PromptDetailDTO {

  @Schema(description = "Prompt模板")
  private PromptTemplateDTO promptTemplate;

  @Schema(description = "工具列表")
  private List<ToolDTO> tools;

  @Schema(description = "工具调用配置")
  private ToolCallConfigDTO toolCallConfig;

  @Schema(description = "模型配置")
  private ModelConfigDTO modelConfig;
}
