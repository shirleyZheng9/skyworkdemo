package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token使用情况数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Token使用情况数据传输对象")
public class TokenUsageDTO {

  @Schema(description = "输入令牌数")
  private Long inputTokens;

  @Schema(description = "输出令牌数")
  private Long outputTokens;
}
