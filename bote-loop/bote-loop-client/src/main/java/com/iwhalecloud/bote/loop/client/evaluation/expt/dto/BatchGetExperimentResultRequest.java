package com.iwhalecloud.bote.loop.client.evaluation.expt.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ExperimentFilterDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量获取实验结果请求
 * 对应Go: expt.BatchGetExperimentResultRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "批量获取实验结果请求")
public class BatchGetExperimentResultRequest {
  @Schema(description = "工作空间ID")
  private Long workspaceId;
  @Schema(description = "实验ID列表")
  private List<Long> experimentIds;
  @Schema(description = "数据项id列表")
  private List<Long> itemIds;
  @Schema(description = "基线实验ID")
  private Long baselineExperimentId;
  @Schema(description = "过滤器映射")
  private Map<Long, ExperimentFilterDTO> filters;
  @Schema(description = "页码")
  private Integer pageNumber;
  @Schema(description = "页面大小")
  private Integer pageSize;
  @Schema(description = "是否使用加速器")
  private Boolean useAccelerator;
  @Schema(description = "基础信息")
  private Base base;
}
