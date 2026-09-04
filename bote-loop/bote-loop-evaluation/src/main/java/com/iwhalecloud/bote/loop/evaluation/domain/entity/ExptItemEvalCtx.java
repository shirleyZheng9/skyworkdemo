package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.event.ExptItemEvalEvent;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExptItemEvalCtx {

  private ExptItemEvalEvent event;
  private Experiment expt;
  private EvaluationSetItem evalSetItem;
  private ExptItemEvalResult exptItemEvalResult;

  public ExptTurnResultRunLog getExistTurnResultRunLog(Long turnId) {
    Map<Long, ExptTurnResultRunLog> existTurnResultLogs = getExistTurnResultLogs();
    return existTurnResultLogs == null ? null : existTurnResultLogs.get(turnId);
  }

  public Map<Long, ExptTurnResultRunLog> getExistTurnResultLogs() {
    return getExptItemEvalResult().getTurnResultRunLogs();
  }

}
