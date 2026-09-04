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
 * 批量获取评测器响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "批量获取评测器响应")
public class BatchGetEvaluatorsResponse {

  @Schema(description = "评测器列表")
  private List<EvaluatorDTO> evaluators;

  @Schema(description = "基础响应信息")
  private BaseResp baseResp;
}
