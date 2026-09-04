package com.iwhalecloud.bote.loop.client.prompt.openapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Prompt结果DTO
 * 对应Thrift: PromptResult
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Prompt结果DTO")
public class PromptResult {

  /**
   * 查询条件
   * 对应Thrift字段: query
   */
  @Schema(description = "查询条件")
  private PromptQuery query;

  /**
   * Prompt信息
   * 对应Thrift字段: prompt
   */
  @Schema(description = "Prompt信息")
  private PromptDTO prompt;
}
