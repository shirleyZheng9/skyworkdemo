package com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评测器输出数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "评测器输出数据传输对象")
public class EvaluatorOutputDataDTO {

  @Schema(description = "评测器结果")
  private EvaluatorResultDTO evaluatorResult;

  @Schema(description = "评测器使用情况")
  private EvaluatorUsageDTO evaluatorUsage;

  @Schema(description = "评测器运行错误")
  private EvaluatorRunErrorDTO evaluatorRunError;

  @Schema(description = "耗时（毫秒）")
  private Long timeConsumingMs;
}
