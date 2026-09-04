package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验结果数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "实验结果数据传输对象")
public class ExperimentResultDTO {

  @Schema(description = "实验ID")
  private Long experimentId;

  @Schema(description = "载荷数据")
  private ExperimentTurnPayloadDTO payload;
}
