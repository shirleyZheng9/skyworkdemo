package com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorInputDataDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 运行评估器请求
 * 对应Go: evaluator.RunEvaluatorRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "运行评估器请求")
public class RunEvaluatorRequest {

  /**
   * 工作空间ID
   * 对应Go: WorkspaceID int64
   */
  @Schema(description = "工作空间ID")
  private Long workspaceId;

  /**
   * 评测规则ID
   * 对应Go: EvaluatorVersionID int64
   */
  @Schema(description = "评测规则ID")
  private Long evaluatorVersionId;

  /**
   * 评测数据输入: 数据集行内容 + 评测目标输出内容与历史记录 + 评测目标的 trace
   * 对应Go: InputData *evaluator.EvaluatorInputData
   */
  @Schema(description = "评测数据输入")
  private EvaluatorInputDataDTO inputData;

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
   * 项目ID
   * 对应Go: ItemID *int64
   */
  @Schema(description = "项目ID")
  private Long itemId;

  /**
   * 轮次ID
   * 对应Go: TurnID *int64
   */
  @Schema(description = "轮次ID")
  private Long turnId;

  /**
   * 扩展信息
   * 对应Go: Ext map[string]string
   */
  @Schema(description = "扩展信息")
  private Map<String, String> ext;

  /**
   * 基础信息
   * 对应Go: Base *base.Base
   */
  @Schema(description = "基础信息")
  private Base base;
}
