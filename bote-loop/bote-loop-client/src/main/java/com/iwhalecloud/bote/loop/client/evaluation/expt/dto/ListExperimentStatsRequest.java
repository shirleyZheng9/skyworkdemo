package com.iwhalecloud.bote.loop.client.evaluation.expt.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.SessionDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ExptFilterOptionDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 列出实验统计请求
 * 对应Go: expt.ListExperimentStatsRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "列出实验统计请求")
public class ListExperimentStatsRequest {

  /**
   * 工作空间ID
   * 对应Go: WorkspaceID int64
   */
  @Schema(description = "工作空间ID")
  private Long workspaceId;

  /**
   * 页码
   * 对应Go: PageNumber *int32
   */
  @Schema(description = "页码")
  private Integer pageNumber;

  /**
   * 页面大小
   * 对应Go: PageSize *int32
   */
  @Schema(description = "页面大小")
  private Integer pageSize;

  /**
   * 过滤选项
   * 对应Go: FilterOption *expt.ExptFilterOption
   */
  @Schema(description = "过滤选项")
  private ExptFilterOptionDTO filterOption;

  /**
   * 会话信息
   * 对应Go: Session *common.Session
   */
  @Schema(description = "会话信息")
  private SessionDTO session;

  /**
   * 基础信息
   * 对应Go: Base *base.Base
   */
  @Schema(description = "基础信息")
  private Base base;
}
