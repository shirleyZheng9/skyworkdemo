package com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto;

import com.iwhalecloud.bote.loop.client.data.domain.dataset.ItemErrorGroupDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 批量创建评估集数据项响应
 * 对应Go: BatchCreateEvaluationSetItemsResponse
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "批量创建评测集数据项响应")
public class BatchCreateEvaluationSetItemsResponse extends BaseResponse {

  @Schema(description = "已添加的数据项映射，key为item在items中的索引，value为itemId")
  private Map<Long, Long> addedItems;

  @Schema(description = "错误信息列表")
  private List<ItemErrorGroupDTO> errors;
}
