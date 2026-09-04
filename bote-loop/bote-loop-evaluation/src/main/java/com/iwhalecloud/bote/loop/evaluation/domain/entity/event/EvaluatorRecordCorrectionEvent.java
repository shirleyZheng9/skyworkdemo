package com.iwhalecloud.bote.loop.evaluation.domain.entity.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorResult;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluatorRecordCorrectionEvent {
  @JsonProperty("evaluator_result")
  private EvaluatorResult evaluatorResult;

  @JsonProperty("evaluator_record_id")
  private long evaluatorRecordId;

  @JsonProperty("evaluator_version_id")
  private long evaluatorVersionId;

  @JsonProperty("ext")
  private Map<String, String> ext;

  @JsonProperty("created_at")
  private long createdAt;

  @JsonProperty("updated_at")
  private long updatedAt;
}
