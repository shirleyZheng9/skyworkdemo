package com.iwhalecloud.bote.service.publish.platform.dingtalk;

import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.dto.beyond.PublishChannelDTO;
import com.iwhalecloud.bote.dto.publish.ResourcePublishRecordDTO;
import com.iwhalecloud.bote.dto.publish.StandardMessage;
import com.iwhalecloud.bote.mapper.publish.ResourcePublishRecordMapper;
import com.iwhalecloud.bote.service.publish.platform.PlatformAdapter;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

/**
 * 钉钉适配器 - 基于LangBot设计，支持WebSocket长连接
 *
 * @author system
 * @since 2025-01-09
 */
public final class DingTalkAdapter extends PlatformAdapter {
  private static final Logger logger = LoggerFactory.getLogger(DingTalkAdapter.class);
  private final DingTalkApiClient apiClient;
  private final Map<String, MessageCallback> listeners = new ConcurrentHashMap<>();
  // Stream模式连接相关
  private final DingTalkStreamClient streamClient;
  private boolean isConnected = false;

  public DingTalkAdapter(PublishChannelDTO config, ResourcePublishRecordDTO publishRecord, ResourcePublishRecordMapper resourcePublishRecordMapper) {
    super("dingtalk", config, publishRecord);
    this.resourcePublishRecordMapper = resourcePublishRecordMapper;
    DingTalkConfig dingTalkConfig = DingTalkConfig.fromMap(config);
    // 验证配置
    if (!dingTalkConfig.isValid()) {
      throw new IllegalArgumentException("钉钉配置不完整");
    }
    this.apiClient = new DingTalkApiClient(
      dingTalkConfig.getAppKey(),
      dingTalkConfig.getAppSecret(),
      dingTalkConfig.getRobotCode()
    );
    // 初始化Stream客户端
    this.streamClient = new DingTalkStreamClient(
      dingTalkConfig.getAppKey(),
      dingTalkConfig.getAppSecret(),
      this
    );
  }

  @Override
  public Future<Boolean> sendMessage(String targetType, String targetId, StandardMessage message) {
    return ThreadPools.getPublish().submit(() -> {
      // 更新最后访问时间
      if (publishRecord != null && publishRecord.getCallbackCode() != null) {
        updateLastAccessTime(publishRecord.getCallbackCode());
      }
      if ("person".equals(targetType)) {
        return sendProactiveMessageToUser(targetId, message);
      }
      else if ("group".equals(targetType)) {
        return sendProactiveMessageToGroup(targetId, message);
      }
      logger.warn("不支持的钉钉消息类型: {}", targetType);
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
      // 根据消息源类型选择回复方式
      if (messageSource.getGroupId() != null) {
        // 群组消息回复
        return sendProactiveMessageToGroup(messageSource.getGroupId(), message);
      }
      else if (messageSource.getUserId() != null) {
        // 私聊消息回复
        return sendProactiveMessageToUser(messageSource.getUserId(), message);
      }
      else {
        logger.warn("无法确定钉钉消息回复目标");
        return false;
      }
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
      // 钉钉流式输出，使用简单文本消息
      if (messageSource.getGroupId() != null) {
        // 群组消息流式回复
        return apiClient.sendSimpleTextMessage(messageSource.getGroupId(), "group", message.getContent());
      }
      else if (messageSource.getUserId() != null) {
        // 私聊消息流式回复
        return apiClient.sendSimpleTextMessage(messageSource.getUserId(), "person", message.getContent());
      }
      else {
        logger.warn("无法确定钉钉流式消息回复目标");
        return false;
      }
    });
  }

  @Override
  public void registerListener(String eventType, MessageCallback callback) {
    listeners.put(eventType, callback);
    // 同时注册到Stream客户端
    if (streamClient != null) {
      streamClient.registerListener(eventType, callback);
    }
  }

  @Override
  public void unregisterListener(String eventType, @Nullable MessageCallback callback) {
    listeners.remove(eventType);
    // 同时从Stream客户端注销
    if (streamClient != null) {
      streamClient.unregisterListener(eventType);
    }
  }

  @Override
  public void runAsync() {
    ThreadPools.getPublish().submit(() -> {
      if (streamClient == null) {
        logger.error("钉钉Stream客户端未初始化");
        return;
      }
      try {
        apiClient.getAccessToken();
        streamClient.start();
        isConnected = true;
      }
      catch (Exception e) {
        logger.error("钉钉Stream客户端启动失败，鉴权可能失败，请检查 appKey/appSecret 配置", e);
        isConnected = false;
      }
    });
  }

  @Override
  public void kill() {
    ThreadPools.getPublish().submit(() -> {
      if (streamClient != null) {
        streamClient.stop();
        isConnected = false;
        logger.info("钉钉适配器已停止");
      }
      return true;
    });
  }

  @Override
  public boolean isStreamOutputSupported() {
    return false; // 目前不支持
  }

  @Override
  public Future<Boolean> isMuted(String groupId) {
    return CompletableFuture.completedFuture(false); // 钉钉暂不支持禁言检查
  }

  @Override
  public Future<Boolean> createMessageCard(String messageId, StandardMessage event) {
    return ThreadPools.getPublish().submit(() -> {
      // 钉钉卡片消息通过API直接发送，不需要预创建
      logger.debug("钉钉卡片消息创建: messageId={}", messageId);
      return true;
    });
  }

  /**
   * 发送主动消息给用户
   */
  private boolean sendProactiveMessageToUser(String userId, StandardMessage message) {
    return apiClient.sendPrivateMessage(userId, message.getContent());
  }

  /**
   * 发送主动消息给群组
   */
  private boolean sendProactiveMessageToGroup(String groupId, StandardMessage message) {
    return apiClient.sendGroupMessage(groupId, message.getContent());
  }

  @Override
  public boolean isConnected() {
    return isConnected && streamClient != null && streamClient.isConnected();
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public String handleWebhookMessage(HttpServletRequest request, String encryptedMsg, ResourcePublishRecordDTO record) {
    // 钉钉使用Stream模式，消息通过WebSocket接收，这里主要用于Webhook验证
    logger.debug("钉钉消息处理: callbackCode={}", record.getCallbackCode());
    return "success";
  }
}
