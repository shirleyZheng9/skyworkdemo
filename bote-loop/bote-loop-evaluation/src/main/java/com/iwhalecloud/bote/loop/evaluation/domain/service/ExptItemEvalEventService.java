package com.iwhalecloud.bote.loop.evaluation.domain.service;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.event.ExptItemEvalEvent;

/**
 * 实验数据项评估事件接口
 * 对应Go: ExptItemEvalEvent
 */
public interface ExptItemEvalEventService {

  /**
   * 评估数据项
   * 对应Go: Eval
   */
  void eval(ExptItemEvalEvent event);
}
