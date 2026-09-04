package com.iwhalecloud.bote.service.scene.runner.impl;

import com.iwhalecloud.bote.agent.agents.GeneralAgent;
import com.iwhalecloud.bote.agent.event.handlers.SseChatAgentEventHandler;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.bot.SimpleBotSceneDTO;
import com.iwhalecloud.bote.dto.chat.ChatRequestDTO;
import com.iwhalecloud.bote.dto.chat.ChatRequestMessageDTO;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineResponse;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bote.service.chat.context.ChatContext;
import com.iwhalecloud.bote.service.scene.runner.AbstractSceneRunner;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.Date;
import org.springframework.stereotype.Service;

/**
 * Claw 智能体执行器
 *
 * @author bianjp
 * @since 2026-04-24
 */
@Service
public class ClawSceneRunner extends AbstractSceneRunner {
  @Override
  public OrchestrationEngineResponse run(SimpleBotSceneDTO scene, SceneChatParamsDTO sceneChatParams) {
    Date startTime = new Date();
    ChatContext context = getChatContext(sceneChatParams);
    OrchestrationEngineResponse response = new OrchestrationEngineResponse();
    response.setSuccess(true);
    try {
      SseChatAgentEventHandler eventHandler = new SseChatAgentEventHandler(sceneChatParams.getReplyHandler());
      GeneralAgent generalAgent = new GeneralAgent(context, scene, null, eventHandler);
      generalAgent.execute();
      if (eventHandler.isFailed()) {
        response.setSuccess(false);
        response.setFailMsg(eventHandler.getErrorMsg());
        Exception e = eventHandler.getErrorThrown();
        if (e instanceof BssException bssException) {
          response.setException(bssException);
        }
        else if (e != null) {
          response.setException(new BssException(e));
        }
      }
      else {
        response.setReplies(eventHandler.getReplies());
      }
    }
    catch (Exception e) {
      response.setSuccess(false);
      response.setFailMsg(ExpUtil.getMsg(e));
      response.setException(new BssException(e));
    }

    response.setTimeSpent(System.currentTimeMillis() - startTime.getTime());
    return response;
  }

  /**
   * 获取会话上下文
   */
  private ChatContext getChatContext(SceneChatParamsDTO sceneChatParams) {
    ChatContext context = sceneChatParams.getChatContext();
    if (context != null) {
      return context;
    }

    ChatRequestMessageDTO message = new ChatRequestMessageDTO();
    message.setToolCallId(sceneChatParams.getToolCallId());
    message.setFileIds(sceneChatParams.getFileIds());
    message.setContent(sceneChatParams.getMessageContent());
    message.setParams(sceneChatParams.getParams());

    ChatRequestDTO request = new ChatRequestDTO();
    request.setMessage(message);
    request.setTenantId(sceneChatParams.getTenantId());
    request.setBotId(sceneChatParams.getBotId());
    request.setClientId(sceneChatParams.getClientId());
    request.setSessionId(sceneChatParams.getConversationId());
    request.setContextId(sceneChatParams.getContextId());
    request.setSceneId(sceneChatParams.getSceneId());
    request.setContextParams(sceneChatParams.getContextParams());

    return new ChatContext(request, null, SessionUtil.getOptionalUserId());
  }
}
