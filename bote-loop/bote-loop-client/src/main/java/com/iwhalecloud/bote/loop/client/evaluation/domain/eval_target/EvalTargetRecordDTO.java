package com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target;

import com.iwhalecloud.bote.loop.client.evaluation.domain.common.BaseInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评测目标记录数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "评测目标记录数据传输对象")
public class EvalTargetRecordDTO {

  @Schema(description = "ID")
  private Long id;

  @Schema(description = "工作空间ID")
  private Long workspaceId;

  @Schema(description = "目标ID")
  private Long targetId;

  @Schema(description = "目标版本ID")
  private Long targetVersionId;

  @Schema(description = "实验运行ID")
  private Long experimentRunId;

  @Schema(description = "数据项ID")
  private Long itemId;

  @Schema(description = "轮次ID")
  private Long turnId;

  @Schema(description = "追踪ID")
  private String traceId;

  @Schema(description = "日志ID")
  private String logId;

  @Schema(description = "评测目标输入数据")
  private EvalTargetInputDataDTO evalTargetInputData;

  @Schema(description = "评测目标输出数据")
  private EvalTargetOutputDataDTO evalTargetOutputData;

  @Schema(description = "状态")
  private EvalTargetRunStatusDTO status;

  @Schema(description = "基础信息")
  private BaseInfoDTO baseInfo;
}
