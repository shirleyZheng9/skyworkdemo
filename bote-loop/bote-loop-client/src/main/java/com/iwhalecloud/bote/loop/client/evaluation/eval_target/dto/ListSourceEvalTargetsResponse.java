package com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto;

import com.iwhalecloud.bote.loop.client.base.BaseResp;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target.EvalTargetDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 列表源评测目标响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "列表源评测目标响应")
public class ListSourceEvalTargetsResponse {

  @Schema(description = "评测目标列表")
  private List<EvalTargetDTO> evalTargets;

  @Schema(description = "下一页令牌")
  private String nextPageToken;

  @Schema(description = "是否还有更多")
  private Boolean hasMore;

  @Schema(description = "基础响应")
  private BaseResp baseResp;
}
