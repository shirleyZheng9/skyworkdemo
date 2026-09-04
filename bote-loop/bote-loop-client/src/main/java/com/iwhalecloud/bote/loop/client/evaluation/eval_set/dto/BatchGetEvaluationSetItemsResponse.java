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
 * 批量获取评估集数据项响应
 * 对应Go: BatchGetEvaluationSetItemsResponse
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "批量获取评测集数据项响应")
public class BatchGetEvaluationSetItemsResponse extends BaseResponse {

  @Schema(description = "评估集数据项列表")
  private List<EvaluationSetItemDTO> items;
}
