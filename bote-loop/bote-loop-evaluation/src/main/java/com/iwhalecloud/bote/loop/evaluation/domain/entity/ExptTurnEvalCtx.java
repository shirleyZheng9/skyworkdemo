package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.event.ExptItemEvalEvent;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExptTurnEvalCtx {

  private ExptItemEvalEvent event;
  private Experiment expt;
  private EvaluationSetItem evalSetItem;
  private ExptItemEvalResult existItemEvalResult;

  private Turn turn;
  private ExptTurnRunResult exptTurnRunResult;
  private List<Message> history;
  private Map<String, String> ext;

}
