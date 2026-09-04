package com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto;

import com.iwhalecloud.bote.loop.client.base.BaseResp;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取评测器响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "获取评测器响应")
public class GetEvaluatorResponse {

  @Schema(description = "评测器信息")
  private EvaluatorDTO evaluator;

  @Schema(description = "基础响应信息")
  private BaseResp baseResp;
}
