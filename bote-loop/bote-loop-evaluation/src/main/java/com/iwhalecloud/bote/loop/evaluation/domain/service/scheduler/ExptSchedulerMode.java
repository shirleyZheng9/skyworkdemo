package com.iwhalecloud.bote.loop.evaluation.domain.service.scheduler;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.Experiment;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptEvalItem;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptRunMode;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnEvaluatorResultRef;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.event.ExptScheduleEvent;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public interface ExptSchedulerMode {

  ExptRunMode mode();

  void exptStart(ExptScheduleEvent event, Experiment expt);

  ScanEvalItemResult scanEvalItems(ExptScheduleEvent event, Experiment expt);

  boolean exptEnd(ExptScheduleEvent event, Experiment expt, int toSubmit, int incomplete);

  void scheduleStart(ExptScheduleEvent event, Experiment expt);

  void scheduleEnd(ExptScheduleEvent event, Experiment expt, int toSubmit, int incomplete);

  void nextTick(ExptScheduleEvent event, Experiment expt, boolean nextTick);

  void publishResult(List<ExptTurnEvaluatorResultRef> turnEvaluatorRefs, ExptScheduleEvent event);

  @Getter
  @Setter
  class ScanEvalItemResult {
    private List<ExptEvalItem> toSubmit;
    private List<ExptEvalItem> incomplete;
    private List<ExptEvalItem> complete;
  }

}
