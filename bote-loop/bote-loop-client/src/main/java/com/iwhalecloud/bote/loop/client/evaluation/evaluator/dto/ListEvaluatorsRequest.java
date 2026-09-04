package com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.OrderByDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorTypeDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 列表评测器请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "列表评测器请求")
public class ListEvaluatorsRequest {

  @Schema(description = "工作空间ID")
  private Long workspaceId;

  @Schema(description = "搜索名称")
  private String searchName;

  @Schema(description = "创建者ID列表")
  private List<Long> creatorIds;

  @Schema(description = "评估器类型列表")
  private List<EvaluatorTypeDTO> evaluatorType;

  @Schema(description = "是否包含版本")
  private Boolean withVersion;

  @Schema(description = "目录ID")
  private String catalogItemId;

  @Schema(description = "每页大小")
  private Integer pageSize;

  @Schema(description = "页码")
  private Integer pageNumber;

  @Schema(description = "排序字段列表")
  private List<OrderByDTO> orderBys;

  @Schema(description = "基础信息")
  private Base base;
}
