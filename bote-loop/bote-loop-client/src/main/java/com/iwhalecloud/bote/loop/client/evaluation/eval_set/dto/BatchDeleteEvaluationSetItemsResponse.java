package com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 批量删除评测集数据响应DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "批量删除评测集数据项响应")
public class BatchDeleteEvaluationSetItemsResponse extends BaseResponse {

  @Schema(description = "删除数量")
  private Integer deletedCount;
}
