package com.iwhalecloud.bote.loop.evaluation.domain.service;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.Evaluator;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorInputData;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorOutputData;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorRunResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorType;

/**
 * 评估器源服务接口
 * 对应Go: EvaluatorSourceService
 */
public interface EvaluatorSourceService {

  /**
   * 获取评估器类型
   * 对应Go: EvaluatorType
   */
  EvaluatorType evaluatorType();

  /**
   * 运行评估器
   * 对应Go: Run
   */
  EvaluatorRunResult run(Long spaceId, Evaluator evaluator, EvaluatorInputData input);

  /**
   * 调试评估器
   * 对应Go: Debug
   */
  EvaluatorOutputData debug(Long workspaceId, Evaluator evaluator, EvaluatorInputData input);

  /**
   * 预处理
   * 对应Go: PreHandle
   */
  void preHandle(Evaluator evaluator);
}
