package com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto;

import com.iwhalecloud.bote.loop.client.base.BaseResp;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target.EvalTargetVersionDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 列表源评测目标版本响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "列表源评测目标版本响应")
public class ListSourceEvalTargetVersionsResponse {

  @Schema(description = "版本列表")
  private List<EvalTargetVersionDTO> versions;

  @Schema(description = "下一页令牌")
  private String nextPageToken;

  @Schema(description = "是否有更多")
  private Boolean hasMore;

  @Schema(description = "基础响应信息")
  private BaseResp baseResp;
}
