package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set.TurnDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 轮次评估集数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "轮次评估集数据传输对象")
public class TurnEvalSetDTO {

  @Schema(description = "轮次")
  private TurnDTO turn;
}
