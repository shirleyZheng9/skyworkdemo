package com.iwhalecloud.bote.loop.client.evaluation.expt.dto;

import com.iwhalecloud.bote.loop.client.base.BaseResp;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ExptAggregateResultDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量获取实验聚合结果响应
 * 对应Go: expt.BatchGetExperimentAggrResultResponse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "批量获取实验聚合结果响应")
public class BatchGetExperimentAggrResultResponse {

  /**
   * 实验聚合结果列表
   * 对应Go: ExptAggregateResults []*expt.ExptAggregateResult_
   */
  @Schema(description = "实验聚合结果列表")
  private List<ExptAggregateResultDTO> exptAggregateResults;

  /**
   * 基础响应信息
   * 对应Go: BaseResp *base.BaseResp
   */
  @Schema(description = "基础响应信息")
  private BaseResp baseResp;
}
