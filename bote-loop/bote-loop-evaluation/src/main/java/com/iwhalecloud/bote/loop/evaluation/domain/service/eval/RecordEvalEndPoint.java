package com.iwhalecloud.bote.loop.evaluation.domain.service.eval;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.event.ExptItemEvalEvent;

/**
 * 记录评估端点接口
 * 迁移对应关系: Go语言RecordEvalEndPoint
 * - 功能: 定义中间件链的端点
 */
@FunctionalInterface
public interface RecordEvalEndPoint {
  void handle(ExptItemEvalEvent event);
}



