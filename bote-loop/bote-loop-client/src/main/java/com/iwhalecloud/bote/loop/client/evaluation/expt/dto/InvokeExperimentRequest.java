package com.iwhalecloud.bote.loop.client.evaluation.expt.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.SessionDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set.EvaluationSetItemDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 调用实验请求
 * 对应Go: expt.InvokeExperimentRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "调用实验请求")
public class InvokeExperimentRequest {

  /**
   * 工作空间ID
   * 对应Go: WorkspaceID int64
   */
  @Schema(description = "工作空间ID")
  private Long workspaceId;

  /**
   * 评估集ID
   * 对应Go: EvaluationSetID int64
   */
  @Schema(description = "评估集ID")
  private Long evaluationSetId;

  /**
   * 评估集项目列表
   * 对应Go: Items []*eval_set.EvaluationSetItem
   */
  @Schema(description = "评估集项目列表")
  private List<EvaluationSetItemDTO> items;

  /**
   * items 中存在无效数据时，默认不会写入任何数据；设置 skipInvalidItems=true 会跳过无效数据，写入有效数据
   * 对应Go: SkipInvalidItems *bool
   */
  @Schema(description = "跳过无效项目")
  private Boolean skipInvalidItems;

  /**
   * 批量写入 items 如果超出数据集容量限制，默认不会写入任何数据；设置 partialAdd=true 会写入不超出容量限制的前 N 条
   * 对应Go: AllowPartialAdd *bool
   */
  @Schema(description = "允许部分添加")
  private Boolean allowPartialAdd;

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
