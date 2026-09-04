package com.iwhalecloud.bote.loop.evaluation.domain.entity.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.BaseInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorRunError;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnlineExptTurnEvalResult {
  @JsonProperty("evaluator_version_id")
  private long evaluatorVersionId;

  @JsonProperty("evaluator_record_id")
  private long evaluatorRecordId;

  @JsonProperty("score")
  private double score;

  @JsonProperty("reasoning")
  private String reasoning;

  @JsonProperty("status")
  private int status;

  @JsonProperty("evaluator_run_error")
  private EvaluatorRunError evaluatorRunError;

  @JsonProperty("ext")
  private Map<String, String> ext;

  @JsonProperty("base_info")
  private BaseInfo baseInfo;
}
