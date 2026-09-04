package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.iwhalecloud.bote.agent.tool.support.ToolExecutionResult;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineRequest;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineResponse;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.log.OrchestrationStepRunLog;
import com.iwhalecloud.bote.dto.orchestration.step.WorkflowStep;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.lang.Nullable;

/**
 * 工作流步骤执行器
 *
 * @author bianjp
 * @since 2024-08-29
 */
public class WorkflowStepRunner extends AbstractWorkflowStepRunner<WorkflowStep> {

  @Override
  protected Long getFlowId(WorkflowStep step) {
    return step.getFlowId();
  }

  @Override
  protected ParameterSpec getParameters(WorkflowStep step) {
    return step.getParameters();
  }

  @Override
  protected void doRun(SceneOrchestrationContext context, WorkflowStep step) {
    // 构建编排引擎请求
    OrchestrationEngineRequest request = buildOrchestrationRequest(context, step);
    Optional<OrchestrationStepRunLog> logOptional = context.getLastStepRunLogOptional();
    // 调用工作流
    OrchestrationEngineResponse response = invokeWorkflow(request, logOptional);
    // 处理工作流响应
    disposeResponse(context, step, response);
  }

  @Override
  public Object runAsTool(SceneChatParamsDTO sceneChatParams, WorkflowStep step, String toolCallId, @Nullable Map<String, Object> toolArguments, Optional<OrchestrationStepRunLog> log) {
    boolean debug = Boolean.TRUE.equals(sceneChatParams.getDebug()) && Boolean.TRUE.equals(sceneChatParams.getDebugInnerService());
    // 留给工作流引擎转换入参
    OrchestrationEngineRequest request = new OrchestrationEngineRequest(sceneChatParams, null, step.getFlowId(), toolArguments);
    request.setDebug(debug);
    request.setDebugInnerService(debug);
    // 简单场景调用对话流时不需要持久化上下文
    request.setPersistContext(false);
    OrchestrationEngineResponse response = invokeWorkflow(request, log);
    // 如果是对话型工作流，返回响应对象，以便调用方识别
    if (Boolean.TRUE.equals(response.getChatflow())) {
      // 通用智能体调用时特殊处理
      if (sceneChatParams.isGeneralAgent()) {
        // 提取回复
        Triple<String, ChatMessageType, Object> triple = response.extractReply();
        String replyText = triple.getLeft();
        ChatMessageType msgType = triple.getMiddle();
        Object msgContent = triple.getRight();
        return ToolExecutionResult.builder()
          .success(true)
          .result(StringUtils.defaultString(replyText))
          // 返回页面时直接返回
          .returnDirect(msgType == ChatMessageType.PAGE || msgType == ChatMessageType.A2UI)
          .msgType(msgType)
          .msgContent(msgContent)
          .build();
      }
      return response;
    }
    return response.getOutput();
  }

}
