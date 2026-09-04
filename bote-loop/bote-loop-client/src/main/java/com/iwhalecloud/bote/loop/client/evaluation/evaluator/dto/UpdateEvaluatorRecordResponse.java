package com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto;

import com.iwhalecloud.bote.loop.client.base.BaseResp;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorRecordDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新评估器记录响应
 * 对应Go: evaluator.UpdateEvaluatorRecordResponse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "更新评估器记录响应")
public class UpdateEvaluatorRecordResponse {

  /**
   * 评估器记录
   * 对应Go: Record *evaluator.EvaluatorRecord
   */
  @Schema(description = "评估器记录")
  private EvaluatorRecordDTO record;

  /**
   * 基础响应信息
   * 对应Go: BaseResp *base.BaseResp
   */
  @Schema(description = "基础响应信息")
  private BaseResp baseResp;
}
