package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验轮次负载数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "实验轮次负载数据传输对象")
public class ExperimentTurnPayloadDTO {

  @Schema(description = "轮次ID")
  private Long turnId;

  @Schema(description = "评测集")
  private TurnEvalSetDTO evalSet;

  @Schema(description = "目标输出")
  private TurnTargetOutputDTO targetOutput;

  @Schema(description = "评测器输出")
  private TurnEvaluatorOutputDTO evaluatorOutput;

  @Schema(description = "系统信息")
  private TurnSystemInfoDTO systemInfo;
}
