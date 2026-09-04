package com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto;

import com.iwhalecloud.bote.loop.client.base.BaseResp;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 提交评估器版本响应
 * 对应Go: evaluator.SubmitEvaluatorVersionResponse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "提交评估器版本响应")
public class SubmitEvaluatorVersionResponse {

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
