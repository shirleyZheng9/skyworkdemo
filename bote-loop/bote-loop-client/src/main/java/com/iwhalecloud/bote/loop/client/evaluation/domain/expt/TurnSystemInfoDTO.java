package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 轮次系统信息数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "轮次系统信息数据传输对象")
public class TurnSystemInfoDTO {

  @Schema(description = "轮次运行状态")
  private TurnRunStateDTO turnRunState;

  @Schema(description = "日志ID")
  private String logId;

  @Schema(description = "错误信息")
  private RunErrorDTO error;
}
