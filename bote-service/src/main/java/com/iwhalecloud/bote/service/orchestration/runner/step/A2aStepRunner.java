package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.iwhalecloud.bote.cache.A2aTaskInfoCache;
import com.iwhalecloud.bote.common.util.A2aUtil;
import com.iwhalecloud.bote.common.util.SceneParamUtil;
import com.iwhalecloud.bote.dto.a2a.A2aAgentDTO;
import com.iwhalecloud.bote.dto.a2a.A2aTaskInfo;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.file.AbstractFile;
import com.iwhalecloud.bote.dto.orchestration.step.A2aStep;
import com.iwhalecloud.bote.dto.orchestration.step.A2aStep.A2aTaskConfig;
import com.iwhalecloud.bote.service.a2a.IA2aAgentManageService;
import com.iwhalecloud.bote.service.a2a.helper.A2aInvokeHandler;
import com.iwhalecloud.bote.service.orchestration.reply.ReplyHandler;
import com.iwhalecloud.bote.service.orchestration.reply.handlers.NonStreamFlowReplyHandler;
import com.iwhalecloud.bote.service.orchestration.runner.AbstractStepRunner;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import io.a2a.spec.FilePart;
import io.a2a.spec.Message;
import io.a2a.spec.TaskState;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * A2A 服务步骤执行器
 *
 * @author bianjp
 * @since 2025-10-21
 */
public class A2aStepRunner extends AbstractStepRunner<A2aStep> {

  private final A2aTaskInfoCache taskInfoCache = SpringUtil.getBean(A2aTaskInfoCache.class);
  private final IA2aAgentManageService a2aAgentManageService = SpringUtil.getBean(IA2aAgentManageService.class);

  @Override
  protected void doRun(SceneOrchestrationContext context, A2aStep step) {
    Long tenantId = context.getTenantId();
    Long agentId = step.getAgentId();
    // 查询 A2A 服务
    A2aAgentDTO agent = a2aAgentManageService.findA2aAgent(tenantId, agentId);
    agent.parseJsonConfig();

    // 查询当前会话关联的任务
    String taskCacheKey = context.getRequest().getContextId() + ":" + context.getDsl().getId() + ":" + step.getCode() + ":" + agentId;
    A2aTaskInfo taskInfo = taskInfoCache.get(taskCacheKey);
    processTaskConfig(taskInfo, step.getTaskConfig());

    // 构造消息
    String messageText = resolveTemplate(step.getMessageText());
    Map<String, Object> messageData = resolveMessageData(step.getMessageData());
    List<FilePart> messageFiles = resolveMessageFiles(step.getMessageFile());
    Map<String, Object> metadata = resolveMetadata(step.getMetadata());
    Assert.isTrue(StringUtils.isNotEmpty(messageText) || messageData != null || messageFiles != null, "消息内容、消息数据、消息文件不能同时为空");
    Message userMessage = A2aUtil.buildUserMessage(messageText, messageData, messageFiles, metadata, taskInfo);

    // 记录入参日志
    context.getLastStepRunLogOptional().ifPresent(l -> {
      Map<String, Object> input = new LinkedHashMap<>();
      input.put("taskId", taskInfo.getTaskId());
      input.put("contextId", taskInfo.getContextId());
      input.put("message", userMessage);
      l.setInput(input);
    });

    ReplyHandler replyHandler;
    // 对话型
    if (Boolean.TRUE.equals(context.getDsl().getChatflow())) {
      replyHandler = context.getReplyHandler();
      Assert.notNull(replyHandler, "回复处理器不能为空");
    }
    else {
      // 任务型，不发送回复，只通过节点出参返回信息
      replyHandler = new NonStreamFlowReplyHandler(null);
    }

    // 调用 A2A 服务
    A2aInvokeHandler handler = A2aInvokeHandler.builder()
      .agent(agent)
      .clientId(replyHandler.getClientId())
      .replyHandler(replyHandler)
      .taskInfo(taskInfo)
      .skipAuthRequiredMessage(true)
      .stream(step.getStream())
      .notificationFlowId(step.getNotificationFlowId())
      .build();
    handler.initialize();
    handler.invoke(userMessage);

    // 设置节点出参
    TaskState taskState = taskInfo.getTaskState();
    Map<String, Object> output = new LinkedHashMap<>();
    output.put("taskId", taskInfo.getTaskId());
    output.put("contextId", taskInfo.getContextId());
    output.put("taskState", taskState != null ? taskState.asString() : null);
    output.put("authMessage", taskState == TaskState.AUTH_REQUIRED ? handler.getAuthMessage() : null);
    output.put("reasoningContent", handler.getReasoningContent());
    output.put("content", handler.getReplyContent());
    context.setStepOutput(step, output);
  }

  /**
   * 处理任务配置
   */
  private void processTaskConfig(A2aTaskInfo taskInfo, @Nullable A2aTaskConfig taskConfig) {
    if (taskConfig == null) {
      return;
    }
    // 自定义上下文 ID
    if (Boolean.TRUE.equals(taskConfig.getUseCustomContextId())) {
      Object value = SceneParamUtil.getParamValue(taskConfig.getContextId());
      String contextId;
      if (value == null) {
        contextId = null;
      }
      else {
        Assert.isTrue(value instanceof String, () -> "A2A 节点的上下文 ID 必须是字符串类型，实际是 " + value.getClass().getName());
        contextId = StringUtils.isNotEmpty((String) value) ? (String) value : null;
      }
      taskInfo.setContextId(contextId);
    }
    // 自定义任务 ID
    if (Boolean.TRUE.equals(taskConfig.getUseCustomTaskId())) {
      Object value = SceneParamUtil.getParamValue(taskConfig.getTaskId());
      String taskId;
      if (value == null) {
        taskId = null;
      }
      else {
        Assert.isTrue(value instanceof String, () -> "A2A 节点的任务 ID 必须是字符串类型，实际是 " + value.getClass().getName());
        taskId = StringUtils.isNotEmpty((String) value) ? (String) value : null;
      }
      taskInfo.setTaskId(taskId);
    }
  }

  /**
   * 解析消息数据，用作 A2A 消息的 DataPart
   */
  @Nullable
  @SuppressWarnings("unchecked")
  private Map<String, Object> resolveMessageData(@Nullable String expression) {
    Object data = SceneParamUtil.getParamValue(expression);
    if (data != null) {
      if (data instanceof Map<?, ?> map) {
        return map.isEmpty() ? null : (Map<String, Object>) map;
      }
      throw new BssException("消息数据必须是 Map 类型");
    }
    return null;
  }

  /**
   * 解析消息元数据，用作 A2A 消息的 Metadata
   */
  @Nullable
  @SuppressWarnings("unchecked")
  private Map<String, Object> resolveMetadata(@Nullable String expression) {
    Object data = SceneParamUtil.getParamValue(expression);
    if (data != null) {
      if (data instanceof Map<?, ?> map) {
        return map.isEmpty() ? null : (Map<String, Object>) map;
      }
      throw new BssException("消息元数据必须是 Map 类型");
    }
    return null;
  }

  /**
   * 解析消息文件
   */
  @Nullable
  private List<FilePart> resolveMessageFiles(@Nullable String expression) {
    List<AbstractFile> files = resolveFiles(expression);
    if (files.isEmpty()) {
      return null;
    }
    return A2aUtil.buildMessageFiles(files);
  }

}
