package com.iwhalecloud.bote.service.chat.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Suppliers;
import com.iwhalecloud.bote.cache.SseEmitterCache;
import com.iwhalecloud.bote.common.consts.ChatConsts;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.exception.SecurityFenceException;
import com.iwhalecloud.bote.dto.chat.ChatRequestMessageDTO;
import com.iwhalecloud.bote.dto.chat.ChatTraceLogDTO.ChatTraceLogBuilder;
import com.iwhalecloud.bote.dto.chat.HistoryMessageDTO;
import com.iwhalecloud.bote.dto.chat.SceneProcessDTO;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineResponse;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.llm.client.dto.message.UserMessage;
import com.iwhalecloud.bote.mapper.chat.SceneProcessMapper;
import com.iwhalecloud.bote.mapper.chat.SessionMsgMapper;
import com.iwhalecloud.bote.service.chat.context.ChatContext;
import com.iwhalecloud.bote.service.orchestration.reply.handlers.ChatReplyHandler;
import com.iwhalecloud.bote.service.scene.ISceneChatService;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 调用场景辅助类
 *
 * @author Admin
 */
@Component
@RequiredArgsConstructor
public class CallSceneHelper {
  private final SessionMsgMapper sessionMsgMapper;
  private final SceneProcessMapper sceneProcessMapper;
  private final ISceneChatService sceneChatService;
  private final SseEmitterCache sseEmitterCache;

  /**
   * 调用场景
   */
  public void invoke(ChatContext context) {
    ChatTraceLogBuilder log = context.newLog("调用智能体");
    SceneChatParamsDTO sceneChatParams = buildSceneChatParams(context);
    log.input(sceneChatParams);

    // 调用场景
    OrchestrationEngineResponse response = sceneChatService.run(sceneChatParams);
    log.end();
    // 执行成功
    if (Boolean.TRUE.equals(response.getSuccess())) {
      // 场景结束
      if (Boolean.TRUE.equals(response.getSceneFinished())) {
        sendFinishMessage(context);
      }
      // 如果场景未返回任何回复，向前端发送就绪消息，避免显示空消息
      else if (!Boolean.TRUE.equals(context.getIsClaw()) && context.getChatMessageEvents().isEmpty()) {
        context.sendReadyMessage();
      }
      context.sendDoneMessage();
      return;
    }

    // 处理执行失败
    log.failed();
    log.addLog(response.getFailMsg()).addLog(response.getFailStack());

    // claw 类型的智能体在 GeneralAgent 中已经发送了错误消息，这里不需要再发送
    if (Boolean.TRUE.equals(context.getIsClaw())) {
      context.sendDoneMessage();
      return;
    }

    // 用户主动取消时不需要发送错误信息
    if (sseEmitterCache.isCancelled(context.getRequest().getClientId())) {
      context.addErrorMessage("用户取消");
      context.complete();
      return;
    }
    if (response.getException() instanceof SecurityFenceException) {
      context.sendMessage(ChatMessageType.TEXT, response.getFailMsg());
      context.sendDoneMessage();
      return;
    }
    context.sendMessage(ChatMessageType.ERROR, response.getFailMsg());
    String stackTrace;
    if (StringUtils.isNotEmpty(response.getFailStack())) {
      stackTrace = response.getFailMsg() + "\n" + response.getFailStack();
    }
    else if (response.getException() != null) {
      stackTrace = response.getFailMsg() + "\n" + ExceptionUtils.getStackTrace(response.getException());
    }
    else {
      stackTrace = response.getFailMsg();
    }
    context.sendMessage(ChatMessageType.EXCEPTION, stackTrace);
    context.sendDoneMessage();
  }

  /**
   * 构造场景会话请求场景
   */
  public SceneChatParamsDTO buildSceneChatParams(ChatContext context) {
    ChatRequestMessageDTO userMessage = context.getUserMessage();
    SceneChatParamsDTO sceneChatParams = new SceneChatParamsDTO();
    sceneChatParams.setTenantId(context.getTenantId());
    sceneChatParams.setBotId(context.getBotId());
    sceneChatParams.setSceneId(context.getSceneId());
    sceneChatParams.setConversationId(context.getSessionId());
    sceneChatParams.setTransactionId(context.getTransactionId());
    sceneChatParams.setContextId(context.getContextId());
    sceneChatParams.setToolCallId(userMessage.getToolCallId());
    sceneChatParams.setFileIds(userMessage.getFileIds());
    sceneChatParams.setMessageContent(userMessage.getContent());
    sceneChatParams.setParams(userMessage.getParams());
    sceneChatParams.setContextParams(context.getRequest().getContextParams());
    sceneChatParams.setReplyHandler(new ChatReplyHandler(context));
    sceneChatParams.setHistoryMessagesLoader(Suppliers.memoize(() -> loadHistoryMessages(context)));
    sceneChatParams.setPlanId(context.getPlanId());
    sceneChatParams.setClientId(context.getClientId());
    sceneChatParams.setChatContext(context);
    return sceneChatParams;
  }

  /**
   * 加载历史消息
   */
  private List<Message> loadHistoryMessages(ChatContext context) {
    Long sessionId = context.getSessionId();
    String contextId = context.getContextId();
    if (sessionId == null || ChatConsts.DEFAULT_SESSION_ID.equals(sessionId) || StringUtils.isEmpty(contextId)) {
      return Collections.emptyList();
    }
    int limitMinute = SystemParameter.CHAT_MESSSGE_HIS_LIMIT_MINUTE.getRequiredIntegerValueFromDb();
    long cutoffMillis = limitMinute * 60L * 1000L;
    Date date = new Date(System.currentTimeMillis() - cutoffMillis);
    List<HistoryMessageDTO> sessionMessages = sessionMsgMapper.selectHistoryMessagesByContextId(sessionId, contextId, date);
    List<Message> messages = new ArrayList<>(sessionMessages.size());
    for (HistoryMessageDTO msg : sessionMessages) {
      Message message = buildHistoryMessage(msg);
      if (message != null) {
        messages.add(message);
      }
    }
    return messages;
  }

  /**
   * 构造历史消息
   */
  @Nullable
  private Message buildHistoryMessage(HistoryMessageDTO msg) {
    ChatMessageType msgType = ChatMessageType.ofCode(msg.getMsgType());
    // 用户输入、指令
    if (msgType == ChatMessageType.INPUT || msgType == ChatMessageType.POINT) {
      return StringUtils.isNotEmpty(msg.getMsgText()) ? new UserMessage(msg.getMsgText()) : null;
    }

    // 页面、页面函数，只取入参
    if (msgType == ChatMessageType.PAGE || msgType == ChatMessageType.PAGE_FUNC) {
      // 使用记忆内容
      if (msg.getMemoryContent() != null) {
        return new AssistantMessage(msg.getMemoryContent());
      }
      // 历史数据没有记忆内容，需要提取入参。兼容处理只保留一段时间，下个版本删除
      Map<String, Object> map = JsonUtil.parseJson(msg.getMsgText(), new TypeReference<Map<String, Object>>() {
      });
      // 只在开启了允许记忆时处理
      if (Boolean.TRUE.equals(MapUtils.getBoolean(map, "memorized"))) {
        Object parameters = map.get("parameters");
        return new AssistantMessage(parameters == null ? "" : JsonUtil.toJsonString(parameters));
      }
      return null;
    }

    // 文本回复
    return new AssistantMessage(msg.getMsgText());
  }

  /**
   * 发送场景结束消息
   */
  private void sendFinishMessage(ChatContext context) {
    // 发送退出场景消息
    context.sendExitSceneMessage();

    // 标记对话场景完成
    SceneProcessDTO log = new SceneProcessDTO();
    log.setSessionId(context.getSessionId());
    log.setSceneId(context.getSceneId());
    log.setContextId(context.getContextId());
    log.setChatStatus(ChatConsts.CHAT_SCENE_STATUS_FINISH);
    log.setUpdatorId(context.getUserId());
    sceneProcessMapper.updateChatSceneStatus(log);
  }
}
