package com.iwhalecloud.bote.loop.client.prompt.openapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LLM配置DTO
 * 对应Thrift: LLMConfig
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "LLM配置DTO")
public class LLMConfigDTO {

  /**
   * 温度
   * 对应Thrift字段: temperature
   */
  @Schema(description = "温度")
  private Double temperature;

  /**
   * 最大Token数
   * 对应Thrift字段: max_tokens
   */
  @Schema(description = "最大Token数")
  private Integer maxTokens;

  /**
   * Top K
   * 对应Thrift字段: top_k
   */
  @Schema(description = "Top K")
  private Integer topK;

  /**
   * Top P
   * 对应Thrift字段: top_p
   */
  @Schema(description = "Top P")
  private Double topP;

  /**
   * 存在惩罚
   * 对应Thrift字段: presence_penalty
   */
  @Schema(description = "存在惩罚")
  private Double presencePenalty;

  /**
   * 频率惩罚
   * 对应Thrift字段: frequency_penalty
   */
  @Schema(description = "频率惩罚")
  private Double frequencyPenalty;

  /**
   * JSON模式
   * 对应Thrift字段: json_mode
   */
  @Schema(description = "JSON模式")
  private Boolean jsonMode;
}
