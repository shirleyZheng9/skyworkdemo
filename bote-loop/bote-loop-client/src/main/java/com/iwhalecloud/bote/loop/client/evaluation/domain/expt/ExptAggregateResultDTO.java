package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验聚合结果数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "实验聚合结果数据传输对象")
public class ExptAggregateResultDTO {
  @Schema(description = "实验ID")
  private Long experimentId;
  @Schema(description = "实验名称")
  private String experimentName;
  @Schema(description = "评测器结果映射")
  private Map<Long, EvaluatorAggregateResultDTO> evaluatorResults;
  @Schema(description = "状态")
  private ExptAggregateCalculateStatusDTO status;
}
