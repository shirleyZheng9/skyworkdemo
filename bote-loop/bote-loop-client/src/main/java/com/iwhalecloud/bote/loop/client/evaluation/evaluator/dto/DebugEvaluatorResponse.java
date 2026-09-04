package com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto;

import com.iwhalecloud.bote.loop.client.base.BaseResp;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorOutputDataDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 调试评估器响应
 * 对应Go: evaluator.DebugEvaluatorResponse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "调试评估器响应")
public class DebugEvaluatorResponse {

  /**
   * 输出数据
   * 对应Go: EvaluatorOutputData *evaluator.EvaluatorOutputData
   */
  @Schema(description = "输出数据")
  private EvaluatorOutputDataDTO evaluatorOutputData;

  /**
   * 基础响应信息
   * 对应Go: BaseResp *base.BaseResp
   */
  @Schema(description = "基础响应信息")
  private BaseResp baseResp;
}
