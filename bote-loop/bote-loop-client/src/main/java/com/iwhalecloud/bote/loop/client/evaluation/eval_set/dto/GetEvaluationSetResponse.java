package com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto;

import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set.EvaluationSetDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 获取评测集响应DTO
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "获取评测集响应")
public class GetEvaluationSetResponse extends BaseResponse {

  @Schema(description = "评测集信息")
  private EvaluationSetDTO evaluationSet;
}
