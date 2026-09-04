package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineRequest;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineResponse;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.log.OrchestrationStepRunLog;
import com.iwhalecloud.bote.dto.orchestration.step.AsyncWorkFlowStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * 异步工作流步骤执行器
 *
 * @author wangtingyun
 * @since 2026-02-25
 */
@SuppressWarnings("PMD.GuardLogStatement")
public class AsyncWorkFlowStepRunner extends AbstractWorkflowStepRunner<AsyncWorkFlowStep> {

  private static final Logger logger = LoggerFactory.getLogger(AsyncWorkFlowStepRunner.class);

  @Override
  protected Long getFlowId(AsyncWorkFlowStep step) {
    return step.getFlowId();
  }

  @Override
  protected ParameterSpec getParameters(AsyncWorkFlowStep step) {
    return step.getParameters();
  }

  @Override
  protected void doRun(SceneOrchestrationContext context, AsyncWorkFlowStep step) {
    // 构建编排引擎请求
    OrchestrationEngineRequest request = buildOrchestrationRequest(context, step);
    Optional<OrchestrationStepRunLog> logOptional = context.getLastStepRunLogOptional();
    // 异步调用工作流，使用编排引擎线程池执行，不等待服务执行完成，报错时也仅记录日志
    ThreadPools.getOrchestration().submit(() -> {
      try {
        OrchestrationEngineResponse response = invokeWorkflow(request, logOptional);
        // 处理工作流响应
        disposeResponse(context, step, response);
      }
      catch (Exception e) {
        logger.error("Failed to invoke workflow asynchronously: step={}", step.getCode(), e);
      }
    });
  }
}
