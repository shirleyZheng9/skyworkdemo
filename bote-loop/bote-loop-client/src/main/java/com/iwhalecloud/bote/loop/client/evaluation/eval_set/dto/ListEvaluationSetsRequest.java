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
 * 查询评估集列表请求
 * 对应Go: ListEvaluationSetsRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "列表评测集请求")
public class ListEvaluationSetsRequest {

  @Schema(description = "工作空间ID")
  private Long workspaceId;

  @Schema(description = "评测集名称（支持模糊搜索）")
  private String name;

  @Schema(description = "创建者列表")
  private List<String> creators;

  @Schema(description = "评测集ID列表")
  private List<Long> evaluationSetIds;
  @Schema(description = "目录ID")
  private String catalogItemId;
  @Schema(description = "页码")
  private Integer pageNumber;

  @Schema(description = "分页大小")
  private Integer pageSize;

  @Schema(description = "分页令牌")
  private String pageToken;

  @Schema(description = "排序规则")
  private List<OrderByDTO> orderBys;

  @Schema(description = "基础信息")
  private Base base;
}
