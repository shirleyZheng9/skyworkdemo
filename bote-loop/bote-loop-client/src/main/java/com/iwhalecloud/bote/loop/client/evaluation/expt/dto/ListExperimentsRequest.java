package com.iwhalecloud.bote.loop.client.evaluation.expt.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.OrderByDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ExptFilterOptionDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 列出实验请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "列出实验请求")
public class ListExperimentsRequest {
  @Schema(description = "工作空间ID")
  private Long workspaceId;
  @Schema(description = "工作空间ID")
  private String catalogItemId;
  @Schema(description = "页码")
  private Integer pageNumber;
  @Schema(description = "页面大小")
  private Integer pageSize;
  @Schema(description = "过滤选项")
  private ExptFilterOptionDTO filterOption;
  @Schema(description = "排序条件列表")
  private List<OrderByDTO> orderBys;
  @Schema(description = "基础信息")
  private Base base;
}
