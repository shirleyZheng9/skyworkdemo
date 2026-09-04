package com.iwhalecloud.bote.loop.client.prompt.domain.prompt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 覆盖Prompt参数DTO
 * 迁移对应关系: Thrift struct OverridePromptParams
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OverridePromptParamsDTO {

  @Schema(description = "模型配置")
  private ModelConfigDTO modelConfig;
}
