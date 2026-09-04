package com.iwhalecloud.bote.loop.client.evaluation.expt.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.SessionDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新实验轮次结果过滤器请求
 * 对应Go: expt.UpsertExptTurnResultFilterRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "更新实验轮次结果过滤器请求")
public class UpsertExptTurnResultFilterRequest {

  /**
   * 工作空间ID
   * 对应Go: WorkspaceID *int64
   */
  @Schema(description = "工作空间ID")
  private Long workspaceId;

  /**
   * 实验ID
   * 对应Go: ExperimentID *int64
   */
  @Schema(description = "实验ID")
  private Long experimentId;

  /**
   * 项目ID列表
   * 对应Go: ItemIds []int64
   */
  @Schema(description = "项目ID列表")
  private List<Long> itemIds;

  /**
   * 过滤器类型
   * 对应Go: FilterType *UpsertExptTurnResultFilterType
   */
  @Schema(description = "过滤器类型")
  private String filterType;

  /**
   * 重试次数
   * 对应Go: RetryTimes *int32
   */
  @Schema(description = "重试次数")
  private Integer retryTimes;

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
