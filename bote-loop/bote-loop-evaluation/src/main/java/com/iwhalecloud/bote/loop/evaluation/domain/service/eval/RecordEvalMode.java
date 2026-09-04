package com.iwhalecloud.bote.loop.evaluation.domain.service.eval;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptItemEvalCtx;

/**
 * 记录评估模式接口
 * 迁移对应关系: Go语言RecordEvalMode
 * - 功能: 定义评估模式
 * - 主要方法:
 * * preEval - 预评估处理
 * * postEval - 后评估处理
 */
public interface RecordEvalMode {

  /**
   * 预评估处理
   * 迁移对应关系: Go语言PreEval
   * - 功能: 评估前的准备工作
   * - 参数: eiec - 评估上下文
   * - 返回: 无
   * - 异常: BssException - 处理失败时抛出
   */
  void preEval(ExptItemEvalCtx eiec);

  /**
   * 后评估处理
   * 迁移对应关系: Go语言PostEval
   * - 功能: 评估后的清理工作
   * - 参数: eiec - 评估上下文
   * - 返回: 无
   * - 异常: BssException - 处理失败时抛出
   */
  void postEval(ExptItemEvalCtx eiec);
}
