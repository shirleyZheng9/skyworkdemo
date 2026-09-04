package com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.OrderByDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询评估集数据项列表请求
 * 对应Go: ListEvaluationSetItemsRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "列表评测集数据项请求")
public class ListEvaluationSetItemsRequest {

  @Schema(description = "工作空间ID")
  private Long workspaceId;

  @Schema(description = "评估集ID")
  private Long evaluationSetId;

  @Schema(description = "版本ID")
  private Long versionId;

  @Schema(description = "页码")
  private Integer pageNumber;

  @Schema(description = "分页大小")
  private Integer pageSize;

  @Schema(description = "分页令牌")
  private String pageToken;

  @Schema(description = "排序规则")
  private List<OrderByDTO> orderBys;

  @Schema(description = "排除的数据项ID列表")
  private List<Long> itemIdNotIn;

  @Schema(description = "基础信息")
  private Base base;
}
