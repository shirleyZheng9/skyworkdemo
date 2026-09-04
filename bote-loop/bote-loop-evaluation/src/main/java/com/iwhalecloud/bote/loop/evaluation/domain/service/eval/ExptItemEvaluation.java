package com.iwhalecloud.bote.loop.evaluation.domain.service.eval;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptItemEvalCtx;

public interface ExptItemEvaluation {

  void eval(ExptItemEvalCtx eiec) throws Exception;

}
