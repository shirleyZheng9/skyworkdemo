package com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto;

import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set.EvaluationSetItemDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 查询评估集数据项列表响应
 * 对应Go: ListEvaluationSetItemsResponse
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "列表评测集数据项响应")
public class ListEvaluationSetItemsResponse extends BaseResponse {

  @Schema(description = "评估集数据项列表")
  private List<EvaluationSetItemDTO> items;

  @Schema(description = "总数")
  private Long total;

  @Schema(description = "下一页令牌")
  private String nextPageToken;
}
