package com.iwhalecloud.bote.loop.client.prompt.openapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Prompt查询DTO
 * 对应Thrift: PromptQuery
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Prompt查询DTO")
public class PromptQuery {

  /**
   * Prompt键
   * 对应Thrift字段: prompt_key
   */
  @Schema(description = "Prompt键")
  private String promptKey;

  /**
   * 版本
   * 对应Thrift字段: version
   */
  @Schema(description = "版本")
  private String version;
}
