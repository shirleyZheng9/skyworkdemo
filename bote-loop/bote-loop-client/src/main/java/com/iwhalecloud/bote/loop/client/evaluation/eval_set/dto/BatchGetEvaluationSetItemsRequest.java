package com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量获取评估集数据项请求
 * 对应Go: BatchGetEvaluationSetItemsRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "批量获取评测集数据项请求")
public class BatchGetEvaluationSetItemsRequest {

  @Schema(description = "工作空间ID")
  private Long workspaceId;

  @Schema(description = "评估集ID")
  private Long evaluationSetId;

  @Schema(description = "版本ID")
  private Long versionId;

  @Schema(description = "数据项ID列表")
  private List<Long> itemIds;

  @Schema(description = "基础信息")
  private Base base;
}
