package com.iwhalecloud.bote.loop.evaluation.domain.service;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.CompleteExptOptionFn;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Experiment;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptRunCheckOption;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptRunLog;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptRunMode;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.InvokeExptReq;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Session;

import java.util.Map;

/**
 * 实验执行控制接口（负责实验的运行、监控和状态管理）
 * 对应Go: IExptExecutionManager
 */
public interface IExptExecutionManager {

  /**
   * 检查运行
   * 对应Go: CheckRun
   */
  void checkRun(Experiment expt, Long spaceId, Session session, ExptRunCheckOption... opts);

  /**
   * 运行实验
   * 对应Go: Run
   */
  void run(Long exptId, Long runId, Long spaceId, Session session, ExptRunMode runMode, Map<String, String> ext);

  /**
   * 重试未成功的实验
   * 对应Go: RetryUnSuccess
   */
  void retryUnSuccess(Long exptId, Long runId, Long spaceId, Session session, Map<String, String> ext);

  /**
   * 调用实验
   * 对应Go: Invoke
   */
  void invoke(InvokeExptReq invokeExptReq);

  /**
   * 完成实验
   * 对应Go: Finish
   */
  void finish(Experiment exptId, Long exptRunId, Session session);

  /**
   * 挂起运行
   * 对应Go: PendRun
   */
  void pendRun(Long exptId, Long exptRunId, Long spaceId, Session session);

  /**
   * 挂起实验
   * 对应Go: PendExpt
   */
  void pendExpt(Long exptId, Long spaceId, Session session, CompleteExptOptionFn... opts);

  /**
   * 完成运行
   * 对应Go: CompleteRun
   */
  void completeRun(Long exptId, Long exptRunId, ExptRunMode mode, Long spaceId, Session session, CompleteExptOptionFn... opts);

  /**
   * 完成实验
   * 对应Go: CompleteExpt
   */
  void completeExpt(Long exptId, Long spaceId, Session session, CompleteExptOptionFn... opts);

  /**
   * 记录运行
   * 对应Go: LogRun
   */
  void logRun(Long exptId, Long exptRunId, ExptRunMode mode, Long spaceId, Session session);

  /**
   * 获取运行日志
   * 对应Go: GetRunLog
   */
  ExptRunLog getRunLog(Long exptId, Long exptRunId, Long spaceId, Session session);
}
