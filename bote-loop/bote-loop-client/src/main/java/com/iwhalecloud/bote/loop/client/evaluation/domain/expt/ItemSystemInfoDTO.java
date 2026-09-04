package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项目系统信息数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "项目系统信息数据传输对象")
public class ItemSystemInfoDTO {

  @Schema(description = "运行状态")
  private ItemRunStateDTO runState;

  @Schema(description = "日志ID")
  private String logId;

  @Schema(description = "错误信息")
  private RunErrorDTO error;
}
