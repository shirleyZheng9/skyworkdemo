package com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评测目标运行错误数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "评测目标运行错误数据传输对象")
public class EvalTargetRunErrorDTO {

  @Schema(description = "错误码")
  private Integer code;

  @Schema(description = "错误消息")
  private String message;
}
