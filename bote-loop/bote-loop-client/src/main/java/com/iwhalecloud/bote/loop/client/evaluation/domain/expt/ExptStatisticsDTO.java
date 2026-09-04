package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验统计数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "实验统计数据传输对象")
public class ExptStatisticsDTO {

  @Schema(description = "评测器聚合结果列表")
  private List<EvaluatorAggregateResultDTO> evaluatorAggregateResults;

  @Schema(description = "令牌使用情况")
  private TokenUsageDTO tokenUsage;

  @Schema(description = "信用成本")
  private Double creditCost;

  @Schema(description = "待处理轮次数量")
  private Integer pendingTurnCnt;

  @Schema(description = "成功轮次数量")
  private Integer successTurnCnt;

  @Schema(description = "失败轮次数量")
  private Integer failTurnCnt;

  @Schema(description = "终止轮次数量")
  private Integer terminatedTurnCnt;

  @Schema(description = "处理中轮次数量")
  private Integer processingTurnCnt;
}
