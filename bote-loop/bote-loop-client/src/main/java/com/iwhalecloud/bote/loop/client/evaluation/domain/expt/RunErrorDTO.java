package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 运行错误数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "运行错误数据传输对象")
public class RunErrorDTO {

  @Schema(description = "错误代码")
  private Long code;

  @Schema(description = "错误消息")
  private String message;

  @Schema(description = "错误详情")
  private String detail;
}
