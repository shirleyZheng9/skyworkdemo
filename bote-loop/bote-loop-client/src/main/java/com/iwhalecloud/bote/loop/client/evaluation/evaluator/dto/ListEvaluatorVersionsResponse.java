package com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto;

import com.iwhalecloud.bote.loop.client.base.BaseResp;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorVersionDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 列表评测器版本响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "列表评测器版本响应")
public class ListEvaluatorVersionsResponse {

  @Schema(description = "评测器版本列表")
  private List<EvaluatorVersionDTO> evaluatorVersions;

  @Schema(description = "总数")
  private Long total;

  @Schema(description = "基础响应信息")
  private BaseResp baseResp;
}
