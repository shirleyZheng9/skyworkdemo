package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExptTurnRunResult {
  private EvalTargetRecord TargetResult;
  private Map<Long, EvaluatorRecord> evaluatorResults;
  private Exception evalErr;


}
