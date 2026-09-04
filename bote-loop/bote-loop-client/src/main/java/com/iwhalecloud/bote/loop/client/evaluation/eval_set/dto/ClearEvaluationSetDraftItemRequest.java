package com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 清空评测集草稿数据请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "清空评测集草稿数据项请求")
public class ClearEvaluationSetDraftItemRequest {

  @Schema(description = "工作空间ID")
  private Long workspaceId;

  @Schema(description = "评测集ID")
  private Long evaluationSetId;

  @Schema(description = "基础信息")
  private Base base;
}
