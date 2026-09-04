package com.iwhalecloud.bote.loop.evaluation.domain.service.eval;

/**
 * 记录评估中间件接口
 * 迁移对应关系: Go语言RecordEvalMiddleware
 * - 功能: 定义中间件函数
 */
@FunctionalInterface
public interface RecordEvalMiddleware {
  RecordEvalEndPoint apply(RecordEvalEndPoint next);
}
