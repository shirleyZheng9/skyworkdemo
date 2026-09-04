package com.iwhalecloud.bote.service.publish.impl;

import com.iwhalecloud.bote.agent.agents.GeneralAgent;
import com.iwhalecloud.bote.agent.event.handlers.ChannelAgentEventHandler;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.consts.ChatConsts;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.consts.PublishResourceConsts;
import com.iwhalecloud.bote.common.enums.PublishChannelEnum;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.dto.beyond.PublishChannelDTO;
import com.iwhalecloud.bote.dto.chat.ChatRequestDTO;
import com.iwhalecloud.bote.dto.chat.ChatRequestMessageDTO;
import com.iwhalecloud.bote.dto.chat.ReplyDTO;
import com.iwhalecloud.bote.dto.chat.SessionDTO;
import com.iwhalecloud.bote.dto.publish.ResourcePublishRecordDTO;
import com.iwhalecloud.bote.dto.publish.StandardMessage;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bote.llm.client.consts.MessageRole;
import com.iwhalecloud.bote.mapper.bot.BotQueryMapper;
import com.iwhalecloud.bote.mapper.chat.SessionMapper;
import com.iwhalecloud.bote.mapper.publish.ResourcePublishRecordMapper;
import com.iwhalecloud.bote.service.chat.context.ChatContext;
import com.iwhalecloud.bote.service.orchestration.reply.handlers.NonStreamFlowReplyHandler;
import com.iwhalecloud.bote.service.publish.SdkConnectionManager;
import com.iwhalecloud.bote.service.publish.platform.PlatformAdapter;
import com.iwhalecloud.bote.service.publish.platform.dingtalk.DingTalkAdapter;
import com.iwhalecloud.bote.service.publish.platform.feishu.FeishuAdapter;
import com.iwhalecloud.bote.service.publish.platform.weclaw.WeClawBotAdapter;
import com.iwhalecloud.bote.service.publish.platform.wework.WeWorkAdapter;
import com.iwhalecloud.bote.service.scene.ISceneChatService;
import com.iwhalecloud.bote.wechat.WechatParamHelper;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * SDK连接管理器实现
 *
 * @author system
 * @since 2025-01-09
 */
@Service
@RequiredArgsConstructor
public class SdkConnectionManagerImpl implements SdkConnectionManager {
  private static final Logger logger = LoggerFactory.getLogger(SdkConnectionManagerImpl.class);
  private final Map<String, PlatformAdapter> adapters = new ConcurrentHashMap<>();
  private final WechatParamHelper wechatParamHelper;
  private final ISceneChatService sceneChatService;
  private final ResourcePublishRecordMapper resourcePublishRecordMapper;
  private final SessionMapper sessionMapper;
  private final BotQueryMapper botQueryMapper;
  private IRefreshCacheService refreshCacheService;

  @Override
  public void startConnection(ResourcePublishRecordDTO record) {
    String callbackCode = record.getCallbackCode();
    String channelType = record.getPublishChannel();
    if (PublishResourceConsts.BOTECLAW_EXT_RESOURCE_ID.equals(record.getExtResourceId())) {
      // boteclaw 类型，需要补充应用归属的空间 ID
      Long botTenantId = record.getTenantId();
      if (!BaseConsts.BOTE_AI_ID.equals(record.getResourceId())) {
        botTenantId = botQueryMapper.getBoteClawSpacId(record.getResourceId());
      }
      record.setBotTenantId(botTenantId);
    }
    // 如果适配器已存在，先停止
    if (adapters.containsKey(callbackCode)) {
      stopConnection(callbackCode);
    }
    // 创建新的适配器
    PlatformAdapter adapter = createAdapter(channelType, record);
    if (adapter != null) {
      adapter.runAsync();
      adapters.put(callbackCode, adapter);
    }
  }

  @Override
  public void stopConnection(String callbackCode) {
    PlatformAdapter adapter = adapters.remove(callbackCode);
    if (adapter != null) {
      adapter.kill();
    }
  }

  @Override
  public void broadcastStopConnection(String callbackCode) {
    if (StringUtils.isEmpty(callbackCode)) {
      return;
    }
    // 不使用依赖注入，以避免循环依赖
    if (refreshCacheService == null) {
      refreshCacheService  = SpringUtil.getBean(IRefreshCacheService.class);
    }
    refreshCacheService.refresh(CacheConsts.CACHE_NAME_CHANNEL_CONNECTION, callbackCode);
  }

  @Override
  public String getConnectionStatus(String callbackCode) {
    PlatformAdapter adapter = getAdapter(callbackCode);
    return adapter.isConnected() ? "CONNECTED" : "DISCONNECTED";
  }

  @Override
  public void restartConnection(String callbackCode) {
    broadcastStopConnection(callbackCode);
    // 重新获取发布记录并启动连接
    ResourcePublishRecordDTO record = resourcePublishRecordMapper.getRecordByCallbackCode(callbackCode);
    if (record != null) {
      startConnection(record);
    }
  }

  @Override
  public Map<String, String> getAllConnectionStatus() {
    Map<String, String> statusMap = new ConcurrentHashMap<>();
    adapters.forEach((callbackCode, adapter) -> {
      if (adapter.isConnected()) {
        statusMap.put(callbackCode, "CONNECTED");
      }
    });
    return statusMap;
  }

  @Override
  public PlatformAdapter getAdapter(String callbackCode) {
    PlatformAdapter adapter = adapters.get(callbackCode);
    // 如果适配器不存在，尝试从数据库重新加载并创建
    if (adapter == null) {
      ResourcePublishRecordDTO record = resourcePublishRecordMapper.getRecordByCallbackCode(callbackCode);
      if (record != null) {
        startConnection(record);
        adapter = adapters.get(callbackCode);
      }
    }
    Assert.notNull(adapter, "未实现该适配器");
    return adapter;
  }

  /**
   * 创建平台适配器
   */
  @Nullable
  private PlatformAdapter createAdapter(String channelType, ResourcePublishRecordDTO record) {
    // 从发布记录中获取配置信息
    PublishChannelDTO config = JsonUtil.parseJsonRequired(record.getPublishParams(), PublishChannelDTO.class);
    PlatformAdapter adapter;
    switch (PublishChannelEnum.getByCode(channelType)) {
      case PublishChannelEnum.WEWORK:
        adapter = new WeWorkAdapter(config, record, wechatParamHelper, resourcePublishRecordMapper);
        break;
      case PublishChannelEnum.DINGTALK:
        adapter = new DingTalkAdapter(config, record, resourcePublishRecordMapper);
        break;
      case PublishChannelEnum.FEISHU:
        adapter = new FeishuAdapter(config, record, resourcePublishRecordMapper);
        break;
      case PublishChannelEnum.WECLAWBOT:
        adapter = new WeClawBotAdapter(record, resourcePublishRecordMapper);
        break;
      default:
        logger.warn("不支持的平台渠道类型: {}", channelType);
        return null;
    }
    // 注册默认的消息监听器
    registerDefaultMessageListener(adapter, record);
    return adapter;
  }

  /**
   * 注册默认的消息监听器
   */
  private void registerDefaultMessageListener(PlatformAdapter adapter, ResourcePublishRecordDTO record) {
    // 注册消息监听器 - 异步处理
    adapter.registerListener("message", (message, sourceAdapter) -> ThreadPools.getPublish().submit(() -> {
      //文本类消息回复
      if ("text".equals(message.getMessageType()) && message.getContent() != null) {
        Long sceneId = record.getResourceId();
        String replyText;
        if (PublishResourceConsts.BOTECLAW_EXT_RESOURCE_ID.equals(record.getExtResourceId())) {
          // 调用通用智能体引擎
          ChatRequestMessageDTO requestMessage = new ChatRequestMessageDTO();
          requestMessage.setContent(message.getContent());
          requestMessage.setType("input");
          requestMessage.setRole(MessageRole.USER.getCode());

          ChatRequestDTO request = new ChatRequestDTO();
          request.setSpaceId(record.getTenantId());
          request.setTenantId(record.getBotTenantId());
          request.setBotId(sceneId);
          // sessionID唯一：群ID+spaceId+userId找到唯一的会话，没有就新建
          String groupId = message.getGroupId() == null ? message.getUserId() : message.getGroupId();
          Long sessionId = getAndCreateSession(request.getSpaceId(), request.getTenantId(), request.getBotId(), groupId,
            record.getCreatorId());
          request.setSessionId(sessionId);
          request.setMessage(requestMessage);
          request.setChannelType(message.getChannelType());

          ChatContext context = new ChatContext(request, null, record.getCreatorId());
          GeneralAgent agent = new GeneralAgent(context, new ChannelAgentEventHandler(adapter, message));
          agent.execute();
        }
        else {
          SceneChatParamsDTO sceneChatParams = new SceneChatParamsDTO();
          sceneChatParams.setDebug(false);
          sceneChatParams.setDebugInnerService(false);
          sceneChatParams.setLogEnabled(false);
          sceneChatParams.setTenantId(record.getTenantId());
          sceneChatParams.setSceneId(sceneId);
          sceneChatParams.setMessageContent(message.getContent());
          sceneChatParams.setContextId(message.getGroupId() != null ? message.getGroupId() : message.getUserId());
          sceneChatParams.setHistoryMessagesLoader(ArrayList::new);
          sceneChatParams.setConversationId(ChatConsts.SDK_SESSION_ID);
          // 设置非流式回复处理器
          NonStreamFlowReplyHandler replyHandler = new NonStreamFlowReplyHandler(message.getSessionId());
          sceneChatParams.setReplyHandler(replyHandler);
          sceneChatService.run(sceneChatParams);
          // 获取回复内容
          replyText = extractReplyTextFromHandler(sceneChatParams);

          // 创建回复消息
          StandardMessage replyMessage = new StandardMessage();
          replyMessage.setContent(replyText);
          replyMessage.setMessageType("text");
          replyMessage.setTimestamp(System.currentTimeMillis());
          // 发送回复
          sourceAdapter.replyMessage(message, replyMessage, false);
        }
      }
    }));
  }

  /**
   * 从回复处理器中提取回复文本
   *
   * @param sceneChatParams 场景聊天参数
   * @return 回复文本内容
   */
  @Nullable
  private String extractReplyTextFromHandler(SceneChatParamsDTO sceneChatParams) {
    // 从场景聊天参数中获取回复处理器
    NonStreamFlowReplyHandler replyHandler = (NonStreamFlowReplyHandler) sceneChatParams.getReplyHandler();
    if (replyHandler == null) {
      return null;
    }
    // 获取回复列表
    List<ReplyDTO> replies = replyHandler.getReplies();
    if (CollectionUtils.isEmpty(replies)) {
      return null;
    }
    // 查找文本类型的回复
    Optional<ReplyDTO> textReply = replies.stream()
      .filter(reply -> ChatMessageType.TEXT.equals(reply.getType()) && StringUtils.isNotBlank(reply.getText()))
      .findFirst();
    return textReply.map(ReplyDTO::getText).orElse(null);
  }

  /**
   * 获取会话ID，如果不存在则创建
   */
  private Long getAndCreateSession(Long spaceId, Long botTenantId, Long botId, String groupId, Long userId) {
    SessionDTO session = sessionMapper.getSessionByGroupId(groupId, spaceId, userId);
    if (session == null) {
      Long sessionId = Sequences.BOT_SESSION_ID.next();
      session = new SessionDTO();
      session.setSessionId(sessionId);
      if (BaseConsts.BOTE_AI_ID.equals(botId)) {
        botTenantId = BaseConsts.PLATFORM_TENANT_ID;
      }
      session.setBotId(botId);
      session.setBotTenantId(botTenantId);
      session.setPlatBotId(botId);
      session.setSpaceId(spaceId);
      session.setSessionTitle(ChatConsts.DEFAULT_SESSION_TITLE);
      session.setBeginTime(new Date());
      session.setStatusCd(BaseConsts.STATUS_CD_VALID);
      session.setCreatorId(userId);
      session.setUpdatorId(userId);
      session.setRemark(groupId);
      sessionMapper.insertSession(session);
    }
    return session.getSessionId();
  }
}
