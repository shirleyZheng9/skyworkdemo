package com.iwhalecloud.bote.common.interceptor;

import com.iwhalecloud.bote.cache.LlmTokenCountCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.ModelConsts;
import com.iwhalecloud.bote.common.exception.SecurityFenceException;
import com.iwhalecloud.bote.common.util.CustomizedSensitiveWordUtil;
import com.iwhalecloud.bote.common.util.SceneContextUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.entity.model.ModelUsageLogEntity;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.ModelConfigInfoDTO;
import com.iwhalecloud.bote.llm.client.dto.Usage;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.llm.client.dto.message.MessageContent;
import com.iwhalecloud.bote.llm.client.dto.message.SystemMessage;
import com.iwhalecloud.bote.llm.client.dto.message.UserMessage;
import com.iwhalecloud.bote.llm.client.util.LlmTraceUtil;
import com.iwhalecloud.bote.llm.interceptor.LlmClientInterceptor;
import com.iwhalecloud.bote.observability.LangfuseTracingService;
import com.iwhalecloud.bote.service.model.IModelUsageLogService;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 调用扩展点的大模型客户端拦截器
 *
 * @author bianjp
 * @since 2025-11-10
 */
@Component
@RequiredArgsConstructor
public class BoteLlmClientInterceptor implements LlmClientInterceptor {

  private final LlmTokenCountCache tokenCountCache;
  private final IModelUsageLogService modelUsageLogService;
  private final LangfuseTracingService langfuseTracingService;

  @Override
  public void beforeInvoke(ChatCompletionRequest request) {
    checkSensitive(buildRequestContent(request), request.getTenantId(), BaseConsts.SECURITY_TYPE_LLM_INPUT);
    // 设置链路追踪标识，用于统计 token 数量
    if (StringUtils.isEmpty(request.getTraceId())) {
      request.setTraceId(LlmTraceUtil.getTraceId());
    }
    request.setStartTime(new Date());
  }

  @Override
  public void onPartial(ModelConfigInfoDTO modelConfigInfo, ChatCompletionRequest request, ChatCompletionResponse response) {
    checkSensitive(response.getMessageContent(), request.getTenantId(), BaseConsts.SECURITY_TYPE_LLM_OUTPUT);
  }

  @Override
  public void onSuccess(ModelConfigInfoDTO modelConfigInfo, ChatCompletionRequest request, ChatCompletionResponse response) {
    checkSensitive(response.getMessageContent(), request.getTenantId(), BaseConsts.SECURITY_TYPE_LLM_OUTPUT);
    // 统计 token 数量
    tokenCountCache.increment(request.getTraceId(), response.getUsage(), modelConfigInfo.getModelCode());
    // 上报 Langfuse（异步，不影响主流程）
    langfuseTracingService.recordChatCompletion(modelConfigInfo, request, response);
    // 记录模型使用量日志
    Usage usage = response.getUsage();
    if (usage == null) {
      return;
    }
    Integer timeSpent = (int) (System.currentTimeMillis() - request.getStartTime().getTime());
    ModelUsageLogEntity log = new ModelUsageLogEntity();
    setId(request, log);
    log.setModelId(modelConfigInfo.getModelId());
    log.setModelName(modelConfigInfo.getModelName());
    log.setInputToken(usage.getPromptTokens());
    log.setOutputToken(usage.getCompletionTokens());
    log.setRequestJson(JsonUtil.toJsonString(request));
    log.setResponseJson(JsonUtil.toJsonString(response));
    log.setStartTime(request.getStartTime());
    log.setUserId(SessionUtil.getOptionalUserId());
    log.setTimeSpent(timeSpent);
    log.setTraceId(request.getTraceId());
    log.setSourceFrom(getSourceFrom(request));
    modelUsageLogService.addLog(log);
  }

  private void setId(ChatCompletionRequest request, ModelUsageLogEntity log) {
    Long tenantId = null;
    Long botId = null;
    Long sceneId = null;
    Long flowId = null;
    Long sessionId = null;
    if (SceneContextUtil.hasContext()) {
      tenantId = SceneContextUtil.getContext().getRequest().getTenantId();
      botId = SceneContextUtil.getContext().getRequest().getBotId();
      sceneId = SceneContextUtil.getContext().getRequest().getSceneId();
      flowId = SceneContextUtil.getContext().getRequest().getFlowId();
      sessionId = SceneContextUtil.getContext().getRequest().getConversationId();
    }
    // 非工作流场景（如 GeneralAgent）下兜底从请求对象读取
    if (tenantId == null) {
      tenantId = request.getTenantId();
    }
    if (botId == null) {
      botId = request.getBotId();
    }
    if (sceneId == null) {
      sceneId = request.getSceneId();
    }
    if (sessionId == null) {
      sessionId = request.getSessionId();
    }

    log.setTenantId(tenantId);
    log.setBotId(botId);
    log.setSceneId(sceneId);
    log.setFlowId(flowId);
    log.setSessionId(sessionId);
  }

  private String getSourceFrom(ChatCompletionRequest request) {
    String sourceFrom = request.getSourceFrom();
    if (StringUtils.isEmpty(sourceFrom)) {
      if (SceneContextUtil.hasContext()) {
        if (SceneContextUtil.getContext().getRequest().isDebugEnabled()) {
          sourceFrom = ModelConsts.SOURCE_TEST;
        }
        else {
          sourceFrom = ModelConsts.SOURCE_AGENT;
        }
      }
    }
    return sourceFrom;
  }

  private void checkSensitive(String content, @Nullable Long tenantId, String type) {
    if (StringUtils.isEmpty(content)) {
      return;
    }
    if (tenantId == null) {
      return;
    }
    try {
      CustomizedSensitiveWordUtil.checkSensitive(content, tenantId, type);
    }
    catch (IllegalArgumentException e) {
      throw new SecurityFenceException(e.getMessage());
    }
  }


  private String buildRequestContent(ChatCompletionRequest request) {
    List<Message> messages = request.getMessages();
    if (CollectionUtils.isEmpty(messages)) {
      return "";
    }
    return getMessageText(messages.getLast());
  }

  private String getMessageText(Message message) {
    if (message instanceof UserMessage userMessage) {
      Object content = userMessage.getContent();
      if (content instanceof String text) {
        return text;
      }
      if (content instanceof List<?> list) {
        StringBuilder text = new StringBuilder();
        for (Object item : list) {
          if (item instanceof MessageContent messageContent && StringUtils.isNotEmpty(messageContent.getText())) {
            if (!text.isEmpty()) {
              text.append('\n');
            }
            text.append(messageContent.getText());
          }
        }
        return text.toString();
      }
      return "";
    }
    if (message instanceof SystemMessage systemMessage) {
      return systemMessage.getContent();
    }
    if (message instanceof AssistantMessage assistantMessage) {
      return assistantMessage.getContent();
    }
    return "";
  }
}
