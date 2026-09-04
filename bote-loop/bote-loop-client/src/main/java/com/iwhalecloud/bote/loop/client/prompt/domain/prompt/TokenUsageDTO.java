package com.iwhalecloud.bote.loop.client.prompt.domain.prompt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token使用情况DTO
 * 迁移对应关系: Thrift struct TokenUsage
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenUsageDTO {

  @Schema(description = "输入令牌数")
  private Long inputTokens;

  @Schema(description = "输出令牌数")
  private Long outputTokens;
}
