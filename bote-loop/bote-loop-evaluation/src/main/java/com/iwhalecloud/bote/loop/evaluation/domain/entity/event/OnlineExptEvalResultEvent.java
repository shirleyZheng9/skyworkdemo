package com.iwhalecloud.bote.loop.evaluation.domain.entity.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnlineExptEvalResultEvent {
  @JsonProperty("expt_id")
  private Long exptId;

  @JsonProperty("turn_eval_results")
  private List<OnlineExptTurnEvalResult> turnEvalResults;
}
