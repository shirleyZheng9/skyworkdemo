package com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator;

import com.iwhalecloud.bote.loop.client.common.userinfo.UserInfoCarrier;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.BaseInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评测器记录数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "评测器记录数据传输对象")
public class EvaluatorRecordDTO implements UserInfoCarrier {

  @Schema(description = "ID")
  private Long id;

  @Schema(description = "实验ID")
  private Long experimentId;

  @Schema(description = "实验运行ID")
  private Long experimentRunId;

  @Schema(description = "数据项ID")
  private Long itemId;

  @Schema(description = "轮次ID")
  private Long turnId;

  @Schema(description = "评测器版本ID")
  private Long evaluatorVersionId;

  @Schema(description = "追踪ID")
  private String traceId;

  @Schema(description = "日志ID")
  private String logId;

  @Schema(description = "评测器输入数据")
  private EvaluatorInputDataDTO evaluatorInputData;

  @Schema(description = "评测器输出数据")
  private EvaluatorOutputDataDTO evaluatorOutputData;

  @Schema(description = "状态")
  private EvaluatorRunStatusDTO status;

  @Schema(description = "基础信息")
  private BaseInfoDTO baseInfo;

  @Schema(description = "扩展信息")
  private Map<String, String> ext;
}
