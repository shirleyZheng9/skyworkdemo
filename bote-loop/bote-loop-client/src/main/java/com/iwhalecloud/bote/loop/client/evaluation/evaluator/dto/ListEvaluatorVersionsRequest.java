package com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.OrderByDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 列出评估器版本请求
 * 对应Go: evaluator.ListEvaluatorVersionsRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "列出评估器版本请求")
public class ListEvaluatorVersionsRequest {

  /**
   * 工作空间ID
   * 对应Go: WorkspaceID int64
   */
  @Schema(description = "工作空间ID")
  private Long workspaceId;

  /**
   * 评估器ID
   * 对应Go: EvaluatorID *int64
   */
  @Schema(description = "评估器ID")
  private Long evaluatorId;

  /**
   * 查询版本列表
   * 对应Go: QueryVersions []string
   */
  @Schema(description = "查询版本列表")
  private List<String> queryVersions;

  /**
   * 页面大小
   * 对应Go: PageSize *int32
   */
  @Schema(description = "页面大小")
  private Integer pageSize;

  /**
   * 页码
   * 对应Go: PageNumber *int32
   */
  @Schema(description = "页码")
  private Integer pageNumber;

  /**
   * 排序条件
   * 对应Go: OrderBys []*common.OrderBy
   */
  @Schema(description = "排序条件")
  private List<OrderByDTO> orderBys;

  /**
   * 基础信息
   * 对应Go: Base *base.Base
   */
  @Schema(description = "基础信息")
  private Base base;
}
