package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target.EvalTargetRecordDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 轮次目标输出数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "轮次目标输出数据传输对象")
public class TurnTargetOutputDTO {

  @Schema(description = "评测目标记录")
  private EvalTargetRecordDTO evalTargetRecord;
}
