package com.iwhalecloud.bote.loop.client.evaluation.expt.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.SessionDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 完成实验请求
 * 对应Go: expt.FinishExperimentRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "完成实验请求")
public class FinishExperimentRequest {

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
   * 实验运行ID
   * 对应Go: ExperimentRunID *int64
   */
  @Schema(description = "实验运行ID")
  private Long experimentRunId;

  /**
   * 客户端ID
   * 对应Go: Cid *string
   */
  @Schema(description = "客户端ID")
  private String cid;

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
