package com.iwhalecloud.bote.service.chat.impl;

import com.iwhalecloud.bote.agent.agents.GeneralAgent;
import com.iwhalecloud.bote.agent.event.handlers.CollectTextAgentEventHandler;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.common.util.SceneParamUtil;
import com.iwhalecloud.bote.common.util.TemplateUtil;
import com.iwhalecloud.bote.dto.channel.SimpleChannelJobDTO;
import com.iwhalecloud.bote.dto.chat.ChatRequestDTO;
import com.iwhalecloud.bote.dto.chat.ChatRequestMessageDTO;
import com.iwhalecloud.bote.dto.chat.SessionDTO;
import com.iwhalecloud.bote.dto.chat.SessionMsgDTO;
import com.iwhalecloud.bote.entity.chat.SessionMsgTextEntity;
import com.iwhalecloud.bote.llm.client.consts.MessageRole;
import com.iwhalecloud.bote.mapper.chat.SessionMapper;
import com.iwhalecloud.bote.mapper.chat.SessionMsgMapper;
import com.iwhalecloud.bote.mapper.chat.SessionMsgTextMapper;
import com.iwhalecloud.bote.service.chat.IChatChannelService;
import com.iwhalecloud.bote.service.chat.context.ChatContext;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class ChatChannelServiceImpl implements IChatChannelService {

  private final Logger logger = LoggerFactory.getLogger(ChatChannelServiceImpl.class);

  private final SessionMapper sessionMapper;

  private final SessionMsgMapper sessionMsgMapper;

  private final SessionMsgTextMapper sessionMsgTextMapper;

  @Override
  @Transactional
  public void execute(SimpleChannelJobDTO channel) {
    // 新建会话
    Long sessionId = createSession(channel);
    String message = "";
    // 处理请求输入，解析动态系统变量参数
    channel.setRequestInput(processRequestInput(channel.getRequestInput()));
    if (BaseConsts.TASK_TYPE_TEXT.equals(channel.getTaskType())) {
      // 文本方式，不经过通用智能体引擎
      message = channel.getRequestInput();
      // 按照不同渠道，推送消息
      ResultVO<Void> result = sendMessage(channel, message);
      if (!result.isSuccess()) {
        // 将报错信息，补充到会话消息中，方便排查问题
        message = result.getResultMsg() + " 待推送消息：" + message;
      }
      // 记录会话消息
      saveMockMessage(sessionId, channel.getUserId(), message, channel.getBotId());
    }
    else {
      // agent 方式：异步调用智能体引擎，完成后推送消息
      ThreadPools.getCommon().submit(() -> executeAgentAsync(channel, sessionId));
    }
  }

  /**
   * 处理请求输入，解析动态系统变量参数
   */
  private String processRequestInput(String requestInput) {
    // 解析模板中的参数
    List<Object> templateFragments = TemplateUtil.parseTemplate(requestInput, this::resolveTemplateParam);
    return templateFragments.stream().map(String.class::cast).collect(Collectors.joining());
  }

  /**
   * 解析模板字符串中的参数
   */
  @Nullable
  protected final Object resolveTemplateParam(String expression) {
    String convertedExpression = "$." + expression;
    Object value = SceneParamUtil.getParamValue(convertedExpression);
    // 不支持的表达式原样返回，避免把 ${expression} 转为 $.expression
    if (convertedExpression.equals(value)) {
      return "${" + expression + "}";
    }
    return value;
  }

  /**
   * 异步执行 agent：调用智能体 → 按渠道推送消息 → 记录会话消息
   */
  private void executeAgentAsync(SimpleChannelJobDTO channel, Long sessionId) {
    try {
      ResultVO<String> agentResult = callAgent(channel, sessionId);
      if (agentResult.isSuccess() && StringUtils.isNotEmpty(agentResult.getResultObject())) {
        sendMessage(channel, agentResult.getResultObject());
      }
      else {
        // 记录会话消息
        saveMockMessage(sessionId, channel.getUserId(), agentResult.getResultMsg(), channel.getBotId());
      }
    }
    catch (Exception e) {
      logger.error("Agent execution failed: sessionId={}, channelType={}", sessionId, channel.getChannelType(), e);
    }
  }

  /**
   * 每次定时任务调度，都创建一个会话
   */
  private Long createSession(SimpleChannelJobDTO channel) {
    Long sessionId = Sequences.BOT_SESSION_ID.next();
    Long botTenantId = channel.getTenantId();
    if (BaseConsts.BOTE_AI_ID.equals(channel.getBotId())) {
      botTenantId = BaseConsts.PLATFORM_TENANT_ID;
    }
    SessionDTO session = new SessionDTO();
    session.setSessionId(sessionId);
    session.setBotId(channel.getBotId());
    session.setBotTenantId(botTenantId);
    session.setPlatBotId(channel.getBotId());
    session.setSpaceId(channel.getSpaceId());
    session.setSessionTitle("[定时]" + channel.getTitle());
    session.setBeginTime(new Date());
    session.setStatusCd(BaseConsts.STATUS_CD_VALID);
    session.setCreatorId(channel.getUserId());
    session.setUpdatorId(channel.getUserId());
    sessionMapper.insertSession(session);
    return sessionId;
  }

  /**
   * 构造一条 assistant 角色类型的消息
   */
  private void saveMockMessage(Long sessionId, Long userId, String content, Long botId) {
    SessionMsgDTO msg = new SessionMsgDTO();
    msg.setMsgId(Sequences.BOT_SESSION_MSG_ID.next());
    msg.setSessionId(sessionId);
    msg.setTransactionId(IDUtils.nextId());
    msg.setSceneId(BaseConsts.BOTE_AI_ID);
    msg.setBotId(botId);
    msg.setContextId(null);
    msg.setRole(MessageRole.ASSISTANT.getCode());
    msg.setSort(1);
    msg.setMsgType("general");
    msg.setMemorized(BaseConsts.TRUE);
    msg.setMsgText(content);
    msg.setMsgStatus(BaseConsts.STATE_SUCCESS);
    msg.setBeginTime(new Date());
    msg.setEndTime(new Date());
    msg.setStatusCd(BaseConsts.STATUS_CD_VALID);
    msg.setCreatorId(userId);
    msg.setUpdatorId(userId);

    SessionMsgTextEntity text = new SessionMsgTextEntity();
    text.setMsgId(msg.getMsgId());
    // BoteClaw 对话，助手类型消息，需要包装成 JSON 格式
    String msgTxt = JsonUtil.toJsonString(
      Map.of("role", MessageRole.ASSISTANT.getCode(), "content", StringUtils.defaultIfEmpty(msg.getMsgText(), null)));
    text.setMsgText(msgTxt);
    text.setContentType(msg.getContentType());

    sessionMsgMapper.batchInsertSessionMsg(Collections.singletonList(msg));
    sessionMsgTextMapper.batchInsertSessionMsgText(Collections.singletonList(text));
  }

  /**
   * 按照不同渠道，推送消息。目前只对接钉钉、飞书，都采用 webhook 方式发送消息
   */
  private ResultVO<Void> sendMessage(SimpleChannelJobDTO channel, String message) {
    if (StringUtils.isEmpty(channel.getChannelType())) {
      return ResultVO.success();
    }
    if (StringUtils.isEmpty(channel.getWebhook())) {
      return ResultVO.fail("渠道 webhook 未配置");
    }
    String channelType = channel.getChannelType();
    try {
      if (BaseConsts.CHANNEL_TYPE_DINGTALK.equals(channelType)) {
        return sendDingTalkMessage(channel.getWebhook(), channel.getTitle(), message);
      }
      if (BaseConsts.CHANNEL_TYPE_FEISHU.equals(channelType)) {
        return sendFeishuMessage(channel.getWebhook(), message);
      }
      return ResultVO.fail("不支持的渠道类型: " + channelType);
    }
    catch (Exception e) {
      logger.error("Failed to send message. channelType={}, webhook={}", channelType, channel.getWebhook(), e);
      return ResultVO.fail("推送消息失败: " + e.getMessage());
    }
  }

  /**
   * 钉钉机器人 webhook 发送文本消息
   * <p>文档: https://open.dingtalk.com/document/orgapp/custom-bot-to-send-group-chat-messages</p>
   */
  private ResultVO<Void> sendDingTalkMessage(String webhook, String title, String message) {
    Map<String, Object> body = new HashMap<>(4);
    body.put("msgtype", "markdown");
    Map<String, String> markdown = new HashMap<>(1);
    markdown.put("text", message);
    markdown.put("title", title);
    body.put("markdown", markdown);
    Map<String, Object> at = new HashMap<>(1);
    at.put("isAtAll", false);
    body.put("at", at);

    Map<String, Object> result = HttpUtil.post(webhook, body, new ParameterizedTypeReference<Map<String, Object>>() {
    });
    if (result == null) {
      return ResultVO.fail("钉钉 webhook 响应为空");
    }
    Object errcode = result.get("errcode");
    if (errcode == null || !"0".equals(String.valueOf(errcode))) {
      String errmsg = MapUtils.getString(result, "errmsg", "未知错误");
      return ResultVO.fail("钉钉推送失败: " + errmsg);
    }
    return ResultVO.success();
  }

  /**
   * 飞书机器人 webhook 发送文本消息
   * <p>文档: https://open.feishu.cn/document/ukTMukTMukTM/ucTM5YjL3ETO24yNxkjN</p>
   */
  private ResultVO<Void> sendFeishuMessage(String webhook, String message) {
    Map<String, Object> body = new HashMap<>(2);
    body.put("msg_type", "text");
    Map<String, String> content = new HashMap<>(1);
    content.put("text", message);
    body.put("content", content);

    Map<String, Object> result = HttpUtil.post(webhook, body, new ParameterizedTypeReference<>() {
    });
    if (result == null) {
      return ResultVO.success();
    }
    // 飞书成功时可能返回 {} 或 {"StatusCode":0}，失败时返回 {"code": 9499, "msg": "..."} 等
    Object code = result.get("code");
    Object statusCode = result.get("StatusCode");
    if (code != null && !Integer.valueOf(0).equals(code)) {
      String msg = MapUtils.getString(result, "msg", "未知错误");
      return ResultVO.fail("飞书推送失败: " + msg);
    }
    if (statusCode != null && !Integer.valueOf(0).equals(statusCode)) {
      String statusMessage = MapUtils.getString(result, "StatusMessage", "未知错误");
      return ResultVO.fail("飞书推送失败: " + statusMessage);
    }
    return ResultVO.success();
  }

  /**
   * 调用通用智能体引擎执行，并收集回复文本（用于渠道推送）
   *
   * @param channel 渠道任务
   * @param sessionId 会话 ID
   * @return 成功时返回回复文本，失败时返回错误信息
   */
  private ResultVO<String> callAgent(SimpleChannelJobDTO channel, Long sessionId) {
    try {
      ChatRequestMessageDTO message = new ChatRequestMessageDTO();
      message.setContent(StringUtils.defaultString(channel.getRequestInput()));
      message.setType("input");
      message.setRole(MessageRole.USER.getCode());

      ChatRequestDTO request = new ChatRequestDTO();
      request.setSpaceId(channel.getSpaceId());
      request.setTenantId(channel.getTenantId());
      request.setBotId(channel.getBotId() != null ? channel.getBotId() : BaseConsts.BOTE_AI_ID);
      request.setSessionId(sessionId);
      request.setMessage(message);
      request.setChannelType(channel.getChannelType());

      ChatContext context = new ChatContext(request, null, channel.getUserId());
      CollectTextAgentEventHandler eventHandler = new CollectTextAgentEventHandler();
      GeneralAgent agent = new GeneralAgent(context, eventHandler);
      agent.execute();
      return ResultVO.success(eventHandler.getReplyContent());
    }
    catch (Exception e) {
      logger.error("Failed to call agent: sessionId={}", sessionId, e);
      return ResultVO.fail("智能体执行失败: " + e.getMessage());
    }
  }
}
