package com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set.EvaluationSetItemDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量创建评测集数据请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "批量创建评测集数据项请求")
public class BatchCreateEvaluationSetItemsRequest {

  @Schema(description = "工作空间ID")
  private Long workspaceId;

  @Schema(description = "评测集ID")
  private Long evaluationSetId;

  @Schema(description = "数据项列表")
  private List<EvaluationSetItemDTO> items;

  @Schema(description = "跳过无效数据项")
  private Boolean skipInvalidItems;

  @Schema(description = "允许部分添加")
  private Boolean allowPartialAdd;

  @Schema(description = "基础信息")
  private Base base;
}
