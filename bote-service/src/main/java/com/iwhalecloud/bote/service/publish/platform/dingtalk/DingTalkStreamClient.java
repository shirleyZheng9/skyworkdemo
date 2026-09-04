package com.iwhalecloud.bote.service.publish.platform.dingtalk;

import com.dingtalk.open.app.api.OpenDingTalkClient;
import com.dingtalk.open.app.api.OpenDingTalkStreamClientBuilder;
import com.dingtalk.open.app.api.callback.DingTalkStreamTopics;
import com.dingtalk.open.app.api.callback.OpenDingTalkCallbackListener;
import com.dingtalk.open.app.api.models.bot.ChatbotMessage;
import com.dingtalk.open.app.api.models.bot.MessageContent;
import com.dingtalk.open.app.api.security.AuthClientCredential;
import com.iwhalecloud.bote.dto.publish.StandardMessage;
import com.iwhalecloud.bote.service.publish.platform.PlatformAdapter.MessageCallback;
import java.io.Serial;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 钉钉Stream模式客户端
 * 基于官方dingtalk-stream SDK实现
 *
 * @author system
 * @since 2025-01-09
 */
public class DingTalkStreamClient {
  private static final Logger logger = LoggerFactory.getLogger(DingTalkStreamClient.class);
  private final String appKey;
  private final String appSecret;
  private final DingTalkAdapter adapter;
  private final Map<String, MessageCallback> listeners = new ConcurrentHashMap<>();
  private OpenDingTalkClient streamClient;
  private boolean isConnected = false;

  public DingTalkStreamClient(String appKey, String appSecret, DingTalkAdapter adapter) {
    this.appKey = appKey;
    this.appSecret = appSecret;
    this.adapter = adapter;
  }

  /**
   * 启动Stream连接
   */
  public void start() {
    try {
      // 构建Stream客户端
      streamClient = OpenDingTalkStreamClientBuilder.custom()
        // 配置应用的身份信息
        .credential(new AuthClientCredential(appKey, appSecret))
        // 注册机器人回调监听器
        .registerCallbackListener(DingTalkStreamTopics.BOT_MESSAGE_TOPIC, new ChatBotCallbackListener())
        .build();
      // 启动连接
      streamClient.start();
      isConnected = true;
      logger.info("钉钉Stream客户端启动成功");
    }
    catch (Exception e) {
      logger.error("启动钉钉Stream客户端失败", e);
    }
  }

  /**
   * 停止Stream连接
   */
  public void stop() {
    try {
      if (streamClient != null) {
        streamClient.stop();
      }
      isConnected = false;
      logger.info("钉钉Stream客户端已停止");
    }
    catch (Exception e) {
      logger.error("停止钉钉Stream客户端失败", e);
    }
  }

  /**
   * 注册消息监听器
   */
  public void registerListener(String eventType, MessageCallback callback) {
    listeners.put(eventType, callback);
  }

  /**
   * 注销消息监听器
   */
  public void unregisterListener(String eventType) {
    listeners.remove(eventType);
  }

  /**
   * 检查连接状态
   */
  public boolean isConnected() {
    return isConnected && streamClient != null;
  }

  /**
   * 机器人消息回调监听器
   */
  private final class ChatBotCallbackListener implements OpenDingTalkCallbackListener<ChatbotMessage, Map<String, Object>> {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public Map<String, Object> execute(ChatbotMessage message) {
      MessageContent text = message.getText();
      if (text != null) {
        String content = text.getContent();
        String senderId = message.getSenderStaffId();
        String conversationId = message.getConversationId();
        // 转换为标准消息格式
        StandardMessage standardMessage = new StandardMessage();
        standardMessage.setMessageId(message.getMsgId());
        standardMessage.setUserId(senderId);
        if ("2".equals(message.getConversationType())) {
          standardMessage.setGroupId(conversationId);
        }
        standardMessage.setContent(content);
        standardMessage.setMessageType("text");
        standardMessage.setChannelType("DINGTALK");
        standardMessage.setTimestamp(System.currentTimeMillis());
        // 触发监听器
        MessageCallback callback = listeners.get("message");
        if (callback != null) {
          callback.onMessage(standardMessage, adapter);
        }
      }
      // 返回空响应
      return Map.of();
    }
  }
}
