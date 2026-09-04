package com.iwhalecloud.bote.loop.evaluation.domain.service.eval;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnEvalCtx;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnRunResult;

public interface ExptItemTurnEvaluation {

  ExptTurnRunResult eval(ExptTurnEvalCtx etec);

}
