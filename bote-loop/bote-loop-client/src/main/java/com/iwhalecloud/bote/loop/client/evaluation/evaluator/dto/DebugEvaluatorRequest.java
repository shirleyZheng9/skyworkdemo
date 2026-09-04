package com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorContentDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorInputDataDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorTypeDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 调试评估器请求
 * 对应Go: evaluator.DebugEvaluatorRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "调试评估器请求")
public class DebugEvaluatorRequest {

  /**
   * 工作空间ID
   * 对应Go: WorkspaceID int64
   */
  @Schema(description = "工作空间ID")
  private Long workspaceId;

  /**
   * 待调试评估器内容
   * 对应Go: EvaluatorContent *evaluator.EvaluatorContent
   */
  @Schema(description = "待调试评估器内容")
  private EvaluatorContentDTO evaluatorContent;

  /**
   * 评测数据输入: 数据集行内容 + 评测目标输出内容与历史记录 + 评测目标的 trace
   * 对应Go: InputData *evaluator.EvaluatorInputData
   */
  @Schema(description = "评测数据输入")
  private EvaluatorInputDataDTO inputData;

  /**
   * 评估器类型
   * 对应Go: EvaluatorType evaluator.EvaluatorType
   */
  @Schema(description = "评估器类型")
  private EvaluatorTypeDTO evaluatorType;

  /**
   * 基础信息
   * 对应Go: Base *base.Base
   */
  @Schema(description = "基础信息")
  private Base base;
}
