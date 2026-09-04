package com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto;

import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set.EvaluationSetDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set.EvaluationSetVersionDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 获取评估集版本响应
 * 对应Go: GetEvaluationSetVersionResponse
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "获取评测集版本响应")
public class GetEvaluationSetVersionResponse extends BaseResponse {

  @Schema(description = "评估集版本信息")
  private EvaluationSetVersionDTO version;

  @Schema(description = "评估集信息")
  private EvaluationSetDTO evaluationSet;
}
