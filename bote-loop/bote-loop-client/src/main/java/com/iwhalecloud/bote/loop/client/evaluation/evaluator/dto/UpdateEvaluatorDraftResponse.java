package com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto;

import com.iwhalecloud.bote.loop.client.base.BaseResp;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新评估器草稿响应
 * 对应Go: evaluator.UpdateEvaluatorDraftResponse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "更新评估器草稿响应")
public class UpdateEvaluatorDraftResponse {

  /**
   * 评估器信息
   * 对应Go: Evaluator *evaluator.Evaluator
   */
  @Schema(description = "评估器信息")
  private EvaluatorDTO evaluator;

  /**
   * 基础响应信息
   * 对应Go: BaseResp *base.BaseResp
   */
  @Schema(description = "基础响应信息")
  private BaseResp baseResp;
}
