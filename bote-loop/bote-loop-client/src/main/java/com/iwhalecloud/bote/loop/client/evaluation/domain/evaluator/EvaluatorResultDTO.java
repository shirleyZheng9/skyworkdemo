package com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评测器结果数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluatorResultDTO {

  @JsonProperty("score")
  private Double score;

  @JsonProperty("correction")
  private CorrectionDTO correction;

  @JsonProperty("reasoning")
  private String reasoning;
}
