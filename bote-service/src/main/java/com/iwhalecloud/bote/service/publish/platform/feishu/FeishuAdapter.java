package com.iwhalecloud.bote.service.publish.platform.feishu;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.enums.PublishChannelEnum;
import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.dto.beyond.PublishChannelDTO;
import com.iwhalecloud.bote.dto.publish.ResourcePublishRecordDTO;
import com.iwhalecloud.bote.dto.publish.StandardMessage;
import com.iwhalecloud.bote.mapper.publish.ResourcePublishRecordMapper;
import com.iwhalecloud.bote.service.publish.platform.PlatformAdapter;
import com.iwhalecloud.bote.service.publish.platform.feishu.dto.FeishuConfig;
import com.iwhalecloud.bote.service.publish.platform.feishu.dto.FeishuMessageResponse;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.lark.oapi.event.EventDispatcher;
import com.lark.oapi.service.im.ImService;
import com.lark.oapi.service.im.v1.model.P2MessageReceiveV1;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

/**
 * 飞书适配器 - 参考LangBot的LarkAdapter设计
 *
 * @author system
 * @since 2025-01-09
 */
@SuppressWarnings("PMD.GuardLogStatement")
public final class FeishuAdapter extends PlatformAdapter {
  private static final Logger logger = LoggerFactory.getLogger(FeishuAdapter.class);
  private final FeishuMessageConverter messageConverter;
  private final FeishuApiClient apiClient;
  private final FeishuConfig feishuConfig;
  private final Map<String, MessageCallback> listeners = new ConcurrentHashMap<>();
  private final FeishuWebSocketClient wsClient;

  public FeishuAdapter(PublishChannelDTO config, ResourcePublishRecordDTO publishRecord, ResourcePublishRecordMapper resourcePublishRecordMapper) {
    super(PublishChannelEnum.FEISHU.getCode(), config, publishRecord);
    this.resourcePublishRecordMapper = resourcePublishRecordMapper;
    this.messageConverter = new FeishuMessageConverter();
    feishuConfig = FeishuConfig.fromMap(config);
    this.apiClient = new FeishuApiClient(feishuConfig.getAppId(), feishuConfig.getAppSecret());
    // 验证配置
    if (!feishuConfig.isValid()) {
      throw new BssException("飞书配置不完整");
    }
    // 初始化WebSocket客户端
    this.wsClient = new FeishuWebSocketClient(feishuConfig.getAppId(), feishuConfig.getAppSecret(), createEventDispatcher());
  }

  @Override
  public Future<Boolean> sendMessage(String targetType, String targetId, StandardMessage message) {
    return ThreadPools.getPublish().submit(() -> {
      // 更新最后访问时间
      if (publishRecord != null && publishRecord.getCallbackCode() != null) {
        updateLastAccessTime(publishRecord.getCallbackCode());
      }
      FeishuMessageResponse platformMessage = messageConverter.standardToPlatform(message);
      if ("person".equals(targetType)) {
        return sendPrivateMessage(targetId, platformMessage);
      }
      else if ("group".equals(targetType)) {
        return sendGroupMessage(targetId, platformMessage);
      }
      return false;
    });
  }

  @Override
  public void replyMessage(StandardMessage messageSource, StandardMessage message, boolean quoteOrigin) {
    ThreadPools.getPublish().submit(() -> {
      // 更新最后访问时间
      if (publishRecord != null && publishRecord.getCallbackCode() != null) {
        updateLastAccessTime(publishRecord.getCallbackCode());
      }
      FeishuMessageResponse platformMessage = messageConverter.standardToPlatform(message);
      // 从消息源获取会话信息
      String chatId = getChatIdFromMessage(messageSource);
      return sendGroupMessage(chatId, platformMessage);
    });
  }

  @Override
  public Future<Boolean> replyMessageChunk(StandardMessage messageSource, Object botMessage,
                                           StandardMessage message, boolean quoteOrigin, boolean isFinal) {
    return ThreadPools.getPublish().submit(() -> {
      // 更新最后访问时间
      if (publishRecord != null && publishRecord.getCallbackCode() != null) {
        updateLastAccessTime(publishRecord.getCallbackCode());
      }
      // 飞书支持流式输出，使用卡片消息
      FeishuMessageResponse platformMessage = messageConverter.standardToPlatform(message);
      // 获取会话信息
      String chatId = getChatIdFromMessage(messageSource);
      return sendGroupMessage(chatId, platformMessage);
    });
  }

  @Override
  public void registerListener(String eventType, MessageCallback callback) {
    listeners.put(eventType, callback);
  }

  @Override
  public void unregisterListener(String eventType, @Nullable MessageCallback callback) {
    listeners.remove(eventType);
  }

  @Override
  public void runAsync() {
    // 启动飞书长连接
    ThreadPools.getPublish().submit(this::startFeishuConnection);
  }

  @Override
  public void kill() {
    ThreadPools.getPublish().submit(() -> {
      // 停止飞书连接
      stopFeishuConnection();
      return true;
    });
  }

  @Override
  public boolean isStreamOutputSupported() {
    return true; // 飞书支持流式输出
  }

  @Override
  public Future<Boolean> isMuted(String groupId) {
    return CompletableFuture.completedFuture(false); // 飞书暂不支持禁言检查
  }

  @Override
  public Future<Boolean> createMessageCard(String messageId, StandardMessage event) {
    return ThreadPools.getPublish().submit(() -> {
      // 创建飞书卡片消息
      String chatId = getChatIdFromMessage(event);
      FeishuMessageResponse cardResponse = FeishuMessageResponse.createCardResponse(
        buildCardContent(event)
      );
      return sendGroupMessage(chatId, cardResponse);
    });
  }

  /**
   * 发送私聊消息
   */
  private boolean sendPrivateMessage(String userId, FeishuMessageResponse message) {
    return dealMessage(userId, message);
  }

  private boolean dealMessage(String userId, FeishuMessageResponse message) {
    switch (message) {
      case FeishuMessageResponse.TextResponse textResponse -> {
        return apiClient.sendTextMessage(userId, textResponse.getText());
      }
      case FeishuMessageResponse.RichTextResponse richTextResponse -> {
        return apiClient.sendRichTextMessage(userId, richTextResponse.getPost());
      }
      case FeishuMessageResponse.CardResponse cardResponse -> {
        return apiClient.sendCardMessage(userId, cardResponse.getInteractive());
      }
      case FeishuMessageResponse.ImageResponse imageResponse -> {
        return apiClient.sendImageMessage(userId, imageResponse.getImage());
      }
      default -> {
        logger.warn("不支持的飞书消息类型: {}", message.getClass().getSimpleName());
        return false;
      }
    }
  }

  public Map<String, String> getApplicationInfo() {
    return apiClient.getApplicationInfo(feishuConfig.getAppId());
  }

  /**
   * 发送群组消息
   */
  private boolean sendGroupMessage(String groupId, FeishuMessageResponse message) {
    return dealMessage(groupId, message);
  }

  private volatile boolean isConnected;

  /**
   * 启动飞书连接
   */
  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private void startFeishuConnection() {
    try {
      apiClient.validateCredentials();
      wsClient.start();
      isConnected = true;
    }
    catch (Exception e) {
      logger.error("飞书连接启动失败，鉴权可能失败，请检查 appId/appSecret 配置: {}", e.getMessage(), e);
      isConnected = false;
    }
  }

  /**
   * 停止飞书连接
   */
  private void stopFeishuConnection() {
    isConnected = false;
    unregisterListener("message", null);
    wsClient.close();
  }

  /**
   * 构建卡片内容
   */
  private String buildCardContent(StandardMessage event) {
    return event.getContent();
  }

  /**
   * 安全获取ChatID
   */
  private String getChatIdFromMessage(StandardMessage message) {
    Object rawMessage = message.getRawMessage();
    if (rawMessage instanceof Map) {
      @SuppressWarnings("unchecked")
      Map<String, Object> messageMap = (Map<String, Object>) rawMessage;
      return (String) messageMap.get("chat_id");
    }
    return message.getGroupId();
  }

  /**
   * 创建事件分发器
   * 收到消息时立即添加 Typing 表情，给用户即时反馈
   */
  private EventDispatcher createEventDispatcher() {
    return EventDispatcher.newBuilder("", "") // 长连接不需要这两个参数
      .onP2MessageReceiveV1(new ImService.P2MessageReceiveV1Handler() {
        @Override
        public void handle(P2MessageReceiveV1 event) {
          String messageId = event.getEvent().getMessage().getMessageId();
          if (messageId != null && !messageId.isBlank()) {
            ThreadPools.getCommon().submit(() ->
              apiClient.addReactionToMessage(messageId, FeishuApiClient.EMOJI_TYPE_TYPING));
          }
          // 转换为标准消息格式
          StandardMessage standardMessage = convertFeishuEventToStandard(event);
          // 触发消息监听器
          MessageCallback callback = listeners.get("message");
          if (callback != null) {
            callback.onMessage(standardMessage, FeishuAdapter.this);
          }
        }
      })
      .build();
  }

  /**
   * 转换飞书事件为标准消息
   */
  private StandardMessage convertFeishuEventToStandard(P2MessageReceiveV1 event) {
    StandardMessage standardMessage = new StandardMessage();
    // 设置基本信息
    standardMessage.setMessageId(event.getEvent().getMessage().getMessageId());
    standardMessage.setChannelType(PublishChannelEnum.FEISHU.getCode());
    standardMessage.setMessageType("text");
    standardMessage.setTimestamp(System.currentTimeMillis());
    // 设置消息内容
    Map<String, String> content = JsonUtil.parseJsonRequired(event.getEvent().getMessage().getContent(), new TypeReference<Map<String, String>>() {
    });
    standardMessage.setContent(content.get("text"));
    // 设置用户信息
    standardMessage.setUserId(event.getEvent().getSender().getSenderId().getUserId());
    standardMessage.setUserName(event.getEvent().getSender().getSenderType());
    // 设置会话信息
    standardMessage.setGroupId(event.getEvent().getMessage().getChatId());
    // 设置原始消息
    standardMessage.setRawMessage(event);
    return standardMessage;
  }

  @Override
  public boolean isConnected() {
    return isConnected;
  }
}
