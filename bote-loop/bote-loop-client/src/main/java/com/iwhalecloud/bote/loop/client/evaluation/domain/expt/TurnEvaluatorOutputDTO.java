package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorRecordDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 轮次评估器输出数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "轮次评估器输出数据传输对象")
public class TurnEvaluatorOutputDTO {

  @Schema(description = "评测器记录映射")
  private Map<Long, EvaluatorRecordDTO> evaluatorRecords;
}
