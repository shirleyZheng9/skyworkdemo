package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineRequest;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineResponse;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.log.OrchestrationStepRunLog;
import com.iwhalecloud.bote.service.orchestration.IOrchestrationEngine;
import com.iwhalecloud.bote.service.orchestration.runner.AbstractStepRunner;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 工作流步骤执行器抽象类
 *
 * @author wangtingyun
 * @since 2026-02-26
 */
public abstract class AbstractWorkflowStepRunner<T extends AbstractStep> extends AbstractStepRunner<T> {

  protected static final IOrchestrationEngine engine = SpringUtil.getBean(IOrchestrationEngine.class);

  /**
   * 获取流程 ID
   *
   * @param step 步骤
   * @return 流程 ID
   */
  protected abstract Long getFlowId(T step);

  /**
   * 获取参数定义
   *
   * @param step 步骤
   * @return 参数定义
   */
  protected abstract ParameterSpec getParameters(T step);

  /**
   * 构建编排引擎请求
   *
   * @param context 上下文
   * @param step 步骤
   * @return 请求对象
   */
  protected OrchestrationEngineRequest buildOrchestrationRequest(SceneOrchestrationContext context, T step) {
    Map<String, Object> params = buildRequestParametersToMap(getParameters(step));
    // 忽略值为 null 的参数
    if (!params.isEmpty()) {
      params = params.entrySet().stream()
          .filter(e -> e.getValue() != null)
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    return new OrchestrationEngineRequest(context, getFlowId(step), params);
  }

  /**
   * 调用工作流
   *
   * @param request 请求对象
   * @param logOptional 日志记录
   * @return 响应对象
   */
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  protected OrchestrationEngineResponse invokeWorkflow(OrchestrationEngineRequest request, Optional<OrchestrationStepRunLog> logOptional) {
    logOptional.ifPresent(l -> {
      Map<String, Object> input = new HashMap<>();
      input.put("params", request.getParameters());
      input.put("contextParams", request.getContextParams());
      l.setInput(input);
    });
    OrchestrationEngineResponse response = engine.run(request);
    logOptional.ifPresent(log -> {
      // 调试时，子流程日志不会记录到数据库中，需要直接返回
      // 非调试且记录日志时，子流程日志会单独记录到数据库，节点日志中不应重复记录，只记录日志 ID 以便能关联查询即可
      if (Boolean.TRUE.equals(request.getDebug())) {
        log.setInnerServiceLogs(response.getStepLogs());
      }
      else {
        log.setInnerServiceLogId(response.getLogId());
      }
    });
    if (!Boolean.TRUE.equals(response.getSuccess())) {
      if (response.getException() == null) {
        throw new BssException("调用工作流失败: " + response.getFailMsg());
      }
      throw new BssException("调用工作流失败: " + response.getFailMsg(), response.getException());
    }

    return response;
  }

  /**
   * 处理工作流响应
   *
   * @param context 上下文
   * @param step 步骤
   * @param response 响应对象
   */
  protected void disposeResponse(SceneOrchestrationContext context, T step, OrchestrationEngineResponse response) {
    // 如果是对话流，且产生了回复，则中止执行
    if (Boolean.TRUE.equals(response.getChatflow())) {
      context.setStepOutput(step, response.getGlobalVariables());
      if (Boolean.TRUE.equals(response.getSceneFinished())) {
        context.setSceneFinished(true);
        context.setReturned();
      }
    }
    else {
      context.setStepOutput(step, response.getOutput());
    }
  }

}
