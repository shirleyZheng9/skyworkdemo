package com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评测器使用情况数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "评测器使用情况数据传输对象")
public class EvaluatorUsageDTO {

  @Schema(description = "输入Token数")
  private Long inputTokens;

  @Schema(description = "输出Token数")
  private Long outputTokens;
}
