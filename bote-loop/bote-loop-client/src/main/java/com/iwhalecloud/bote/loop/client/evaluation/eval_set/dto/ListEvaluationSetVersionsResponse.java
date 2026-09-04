package com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto;

import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set.EvaluationSetVersionDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 查询评估集版本列表响应
 * 对应Go: ListEvaluationSetVersionsResponse
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "列表评测集版本响应")
public class ListEvaluationSetVersionsResponse extends BaseResponse {

  @Schema(description = "评估集版本列表")
  private List<EvaluationSetVersionDTO> versions;

  @Schema(description = "总数")
  private Long total;

  @Schema(description = "下一页令牌")
  private String nextPageToken;
}
