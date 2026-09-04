package com.iwhalecloud.bote.service.orchestration.impl;

import com.iwhalecloud.bote.cache.ChatflowContextCache;
import com.iwhalecloud.bote.cache.FlowDslCache;
import com.iwhalecloud.bote.cache.SceneDslCache;
import com.iwhalecloud.bote.cache.TenantSettingInfoCache;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.SceneContextUtil;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineRequest;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineResponse;
import com.iwhalecloud.bote.dto.orchestration.SceneDslDTO;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.service.base.IDataSourceProviderService;
import com.iwhalecloud.bote.service.orchestration.IOrchestrationEngine;
import com.iwhalecloud.bote.service.orchestration.runner.AbstractStepRunner;
import com.iwhalecloud.bote.service.skill.IFlowRunLogService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * 场景编排引擎
 *
 * @author bianjp
 * @since 2024-08-29
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class OrchestrationEngine implements IOrchestrationEngine {
  private static final Logger logger = LoggerFactory.getLogger(OrchestrationEngine.class);

  private final SceneDslCache sceneDslCache;
  private final FlowDslCache flowDslCache;
  private final ChatflowContextCache chatflowContextCache;
  private final TenantSettingInfoCache tenantSettingInfoCache;
  private final IFlowRunLogService flowTraceLogService;
  private final IDataSourceProviderService dataSourceProvider;

  @Override
  @SuppressWarnings("java:S2142")
  public OrchestrationEngineResponse run(OrchestrationEngineRequest request) {
    Date startTime = new Date();
    logger.trace("Received executing service request: startTime={}, request={}", startTime, request);
    // 未显式指定日志开关时，自动查询租户设置
    if (request.getLogEnabled() == null) {
      request.setLogEnabled(tenantSettingInfoCache.isFlowLogEnabled(request.getTenantId()));
    }
    // 任务型工作流可能没传入 contextId, 自动生成一个，方便流程内使用 contextId 唯一标识本次调用。A2aStepRunner 中构造缓存 key 时会用到
    if (StringUtils.isEmpty(request.getContextId())) {
      request.setContextId(SceneContextUtil.newContextId());
    }

    // 构造上下文
    SceneOrchestrationContext context = new SceneOrchestrationContext();
    context.setStartTime(startTime);
    context.setRequest(request);

    // 初始化
    OrchestrationEngineResponse response = initialize(context);
    if (response != null) {
      return response;
    }

    // 执行服务
    response = doRun(context);

    // 记录日志
    if (Boolean.TRUE.equals(request.getLogEnabled())) {
      flowTraceLogService.addLog(request, response, startTime);
    }
    return response;
  }

  /**
   * 引擎的主要执行逻辑
   */
  @SuppressWarnings("PMD.AvoidInstanceofChecksInCatchClause")
  private OrchestrationEngineResponse doRun(SceneOrchestrationContext context) {
    OrchestrationEngineResponse response = null;
    Date startTime = context.getStartTime();
    String serviceCode = context.getDsl().getCode();
    logger.trace("Start executing service: service={}, request={}", serviceCode, context.getRequest());
    // 准备工作成功时才执行步骤
    // 执行步骤
    Future<?> future = null;
    try {
      SceneContextUtil.setContext(context);
      // 限制执行时间，避免死循环或占用太多线程资源
      int maxExecutionSeconds = SystemParameter.FLOW_EXECUTION_TIME_LIMIT.getRequiredIntegerValueFromDb();
      if (maxExecutionSeconds > 0 && SceneContextUtil.getRootContext() == context) { //NOPMD - suppressed CompareObjectsWithEquals - 用 == 比较对象是正确的
        future = ThreadPools.getOrchestration().submit(() -> AbstractStepRunner.executeSteps(context, getStepCode(context)));
        future.get(maxExecutionSeconds, TimeUnit.SECONDS);
      }
      else {
        AbstractStepRunner.executeSteps(context, getStepCode(context));
      }
      // 执行成功后提交事务
      commitTransaction(context);
      response = OrchestrationEngineResponse.success(context);
      logger.trace("Execute service end: service={}, output={}", serviceCode, context.getOutputParameters());
    }
    catch (Exception e) {
      response = handleException(startTime, serviceCode, future, e instanceof ExecutionException ? e.getCause() : e);
      rollbackTransaction(context);
    }
    finally {
      context.cleanup();
      if (response == null) {
        response = OrchestrationEngineResponse.fail(startTime, "执行失败");
      }
      OrchestrationEngineRequest request = context.getRequest();
      response.setStepLogs(context.getStepLogs());
      response.setReplies(context.getReplyHandler() != null ? context.getReplyHandler().getReplies() : null);
      // 清除线程变量
      SceneContextUtil.removeContext();
      // 对话流需要记录全局变量状态
      if (context.shouldPersistContext()) {
        chatflowContextCache.put(context.getRequest().isDebugEnabled(), request.getSceneId(), request.getFlowId(), request.getContextId(), context.getGlobalVariables());
      }
    }
    response.setChatflow(context.getDsl().getChatflow());
    response.setGlobalVariables(context.getGlobalVariables());
    return response;
  }

  /**
   * 处理执行异常
   */
  private OrchestrationEngineResponse handleException(Date startTime, String serviceCode, @Nullable Future<?> future, Throwable e) {
    if (e instanceof BssException) {
      logger.error("Failed to execute service: service={}, error={}", serviceCode, e.getMessage());
      OrchestrationEngineResponse response = OrchestrationEngineResponse.fail(startTime, e.getMessage());
      response.setException((BssException) e);
      return response;
    }
    // 中断
    if (ExpUtil.hasCause(e, InterruptedException.class)) {
      logger.warn("Failed to execute service, interrupted: service={}, error={}", serviceCode, e.getMessage());
      SceneContextUtil.getRootContext().setInterrupted(true);
      if (future != null) {
        future.cancel(true);
      }
      return OrchestrationEngineResponse.fail(startTime, "执行中断");
    }
    // 超时
    if (e instanceof TimeoutException) {
      logger.warn("Failed to execute service, timeout: service={}, error={}", serviceCode, e.getMessage());
      SceneContextUtil.getRootContext().setInterrupted(true);
      if (future != null) {
        future.cancel(true);
      }
      return OrchestrationEngineResponse.fail(startTime, "执行超时");
    }
    logger.error("Failed to execute service: service={}", serviceCode, e);
    return OrchestrationEngineResponse.fail(startTime, e);
  }

  /**
   * 初始化
   */
  @Nullable
  @SuppressWarnings("PMD.AvoidCatchingThrowable")
  private OrchestrationEngineResponse initialize(SceneOrchestrationContext context) {
    OrchestrationEngineRequest request = context.getRequest();
    Date startTime = context.getStartTime();
    OrchestrationEngineResponse response = null;
    // 准备工作
    try {
      // 获取服务定义
      SceneDslDTO dsl = findDsl(request);
      // 校验服务定义
      validateDsl(dsl);

      if (Boolean.TRUE.equals(dsl.getChatflow())) {
        Assert.notNull(request.getReplyHandler(), "对话流必须指定回复处理器");
      }
      context.setReplyHandler(request.getReplyHandler());

      // 构造上下文
      context.setDsl(dsl);
      context.setOutputParameters(new HashMap<>());
      context.initRunLog();
      context.setDataSourceProvider(dataSourceProvider);
    }
    catch (BssException e) {
      logger.error("Failed to prepare executing service: sceneId={}, flowId={} error={}", request.getSceneId(), request.getFlowId(), e.getFailMsg());
      response = OrchestrationEngineResponse.fail(startTime, e.getFailMsg());
      response.setException(e);
    }
    catch (Throwable e) {
      logger.error("Failed to prepare executing service: sceneId={}, flowId={}", request.getSceneId(), request.getFlowId(), e);
      response = OrchestrationEngineResponse.fail(startTime, e);
    }
    return response;
  }

  /**
   * 获取 DSL
   */
  private SceneDslDTO findDsl(OrchestrationEngineRequest request) {
    if (request.getDynamicDsl() != null) {
      return request.getDynamicDsl();
    }
    // 调试模式不使用缓存
    boolean useCache = !request.isDebugEnabled();
    if (request.getSceneId() != null) {
      return sceneDslCache.getDsl(request.getTenantId(), request.getSceneId(), useCache);
    }
    return flowDslCache.getDsl(request.getTenantId(), request.getFlowId(), useCache);
  }

  /**
   * 校验服务定义
   */
  private void validateDsl(SceneDslDTO serviceDefinition) {
    String error = null;
    if (CollectionUtils.isEmpty(serviceDefinition.getSteps())) {
      error = "步骤列表不能为空";
    }
    if (error != null) {
      throw new BssException("DSL 不合法: " + error);
    }
  }

  /**
   * 获取本轮引擎，首个要执行的步骤编码
   * <p>1.常规模式下，每次都从开始节点执行</p>
   * <p>2.多智能调度模式，每次执行智能体节点，都会挂起主流程，智能体执行后，需要通知主流程继续执行</p>
   */
  private String getStepCode(SceneOrchestrationContext context) {
    if (StringUtils.isNotEmpty(context.getRequest().getCompletedNodeCode())) {
      return context.getRequest().getCompletedNodeCode();
    }
    else {
      return context.getDsl().getSteps().get(0).getCode();
    }
  }

  /**
   * 提交事务
   */
  private void commitTransaction(SceneOrchestrationContext context) {
    // 嵌套调用编排服务时，只在最外层管理事务，内层不用管
    if (SceneContextUtil.getRootContext() == context) {
      context.commitTransaction();
    }
  }

  /**
   * 回滚事务
   */
  private void rollbackTransaction(SceneOrchestrationContext context) {
    // 嵌套调用编排服务时，只在最外层管理事务，内层不用管
    if (SceneContextUtil.getRootContext() == context) {
      context.rollbackTransaction();
    }
  }
}
