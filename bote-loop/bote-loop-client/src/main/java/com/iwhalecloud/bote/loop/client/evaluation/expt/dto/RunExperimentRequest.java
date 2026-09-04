package com.iwhalecloud.bote.loop.client.evaluation.expt.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.SessionDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ExptTypeDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 运行实验请求
 * 对应Go: expt.RunExperimentRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "运行实验请求")
public class RunExperimentRequest {

  /**
   * 工作空间ID
   * 对应Go: WorkspaceID *int64
   */
  @Schema(description = "工作空间ID")
  private Long workspaceId;

  /**
   * 实验ID
   * 对应Go: ExptID *int64
   */
  @Schema(description = "实验ID")
  private Long exptId;

  /**
   * 项目ID列表
   * 对应Go: ItemIds []int64
   */
  @Schema(description = "项目ID列表")
  private List<Long> itemIds;

  /**
   * 实验类型
   * 对应Go: ExptType *expt.ExptType
   */
  @Schema(description = "实验类型")
  private ExptTypeDTO exptType;

  /**
   * 扩展信息
   * 对应Go: Ext map[string]string
   */
  @Schema(description = "扩展信息")
  private Map<String, String> ext;

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
