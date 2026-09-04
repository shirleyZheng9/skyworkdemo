package com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto;

import com.iwhalecloud.bote.loop.client.base.BaseResp;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量获取评估器版本响应
 * 对应Go: evaluator.BatchGetEvaluatorVersionsResponse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "批量获取评估器版本响应")
public class BatchGetEvaluatorVersionsResponse {

  /**
   * 评估器列表
   * 对应Go: Evaluators []*evaluator.Evaluator
   */
  @Schema(description = "评估器列表")
  private List<EvaluatorDTO> evaluators;

  /**
   * 基础响应信息
   * 对应Go: BaseResp *base.BaseResp
   */
  @Schema(description = "基础响应信息")
  private BaseResp baseResp;
}
