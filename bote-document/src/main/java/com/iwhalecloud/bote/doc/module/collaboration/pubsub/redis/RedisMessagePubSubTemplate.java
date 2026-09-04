package com.iwhalecloud.bote.doc.module.collaboration.pubsub.redis;

import com.iwhalecloud.bote.doc.module.collaboration.pubsub.MessagePubSubTemplate;
import com.iwhalecloud.bote.doc.module.collaboration.pubsub.MessageSubscriber;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * 基于Redis的消息发布订阅模板实现
 * 封装RedisMessageListenerContainer的使用细节
 *
 * @author Aiqing
 * @since 2025/12/04
 */
public class RedisMessagePubSubTemplate implements MessagePubSubTemplate, InitializingBean {
  private static final Logger logger = LoggerFactory.getLogger(RedisMessagePubSubTemplate.class);

  private final ICacheClient cacheClient;

  /**
   * 存储订阅信息
   * Key: channel, Value: SubscriptionInfo
   */
  private final Map<String, SubscriptionInfo> subscriptions = new ConcurrentHashMap<>();

  private RedisMessageListenerContainer messageListenerContainer;

  public RedisMessagePubSubTemplate(ICacheClient cacheClient) {
    this.cacheClient = cacheClient;
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public String subscribe(String channel, MessageSubscriber subscriber) {
    if (channel == null || subscriber == null) {
      logger.warn("订阅参数不能为空: channel={}, subscriber={}", channel, subscriber);
      return null;
    }

    if (!isSupported()) {
      logger.warn("当前缓存不支持发布订阅模式");
      return null;
    }

    // 确保 messageListenerContainer 已初始化
    if (messageListenerContainer == null) {
      messageListenerContainer = cacheClient.getMessageListenerContainer();
    }

    // 检查是否已经订阅
    if (subscriptions.containsKey(channel)) {
      logger.debug("通道 {} 已经订阅，跳过重复订阅", channel);
      return subscriptions.get(channel).subscriptionId();
    }

    try {
      String subscriptionId = UUID.randomUUID().toString();
      // 将通用的 MessageSubscriber 适配为 Redis 的 MessageListener
      MessageListener redisListener = new MessageSubscriberAdapter(subscriber);
      MessageListenerAdapter adapter = new MessageListenerAdapter(redisListener, "onMessage");
      ChannelTopic topic = new ChannelTopic(channel);

      logger.debug("准备订阅消息: channel={}, subscriptionId={}, containerRunning={}",
        channel, subscriptionId, messageListenerContainer.isRunning());

      messageListenerContainer.addMessageListener(adapter, topic);
      subscriptions.put(channel, new SubscriptionInfo(subscriptionId, adapter, topic, subscriber));

      logger.debug("成功订阅消息: channel={}, subscriptionId={}, 当前订阅数={}",
        channel, subscriptionId, subscriptions.size());

      return subscriptionId;
    }
    catch (Exception e) {
      logger.error("订阅消息失败: channel={}, error={}", channel, e.getMessage(), e);
      return null;
    }
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public void unsubscribe(String channel, String subscriptionId) {
    if (channel == null) {
      logger.warn("通道名称不能为空");
      return;
    }

    if (!isSupported()) {
      logger.warn("当前缓存不支持发布订阅模式");
      return;
    }

    SubscriptionInfo subscriptionInfo = subscriptions.remove(channel);
    if (subscriptionInfo == null) {
      logger.debug("通道 {} 未订阅，无需取消订阅", channel);
      return;
    }

    // 验证订阅ID是否匹配
    if (subscriptionId != null && !subscriptionId.equals(subscriptionInfo.subscriptionId())) {
      logger.warn("订阅ID不匹配: channel={}, expected={}, actual={}",
        channel, subscriptionInfo.subscriptionId(), subscriptionId);
      // 仍然继续取消订阅
    }

    // 检查 messageListenerContainer 是否可用
    if (messageListenerContainer == null) {
      logger.warn("消息监听容器未初始化，无法取消订阅: channel={}", channel);
      return;
    }

    try {
      messageListenerContainer.removeMessageListener(subscriptionInfo.adapter());
      logger.debug("成功取消订阅消息: channel={}, subscriptionId={}", channel, subscriptionInfo.subscriptionId());
    }
    catch (Exception e) {
      logger.error("取消订阅消息失败: channel={}, error={}", channel, e.getMessage(), e);
    }
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public boolean publish(String channel, String message) {
    if (channel == null || message == null) {
      logger.warn("发布参数不能为空: channel={}, message={}", channel, message);
      return false;
    }

    if (!isSupported()) {
      logger.warn("当前缓存不支持发布订阅模式");
      return false;
    }

    try {
      cacheClient.convertAndSend(channel, message);
      logger.trace("成功发布消息: channel={}, messageLength={}", channel, message.length());
      return true;
    }
    catch (Exception e) {
      logger.error("发布消息失败: channel={}, error={}", channel, e.getMessage(), e);
      return false;
    }
  }

  @Override
  public boolean isSupported() {
    return cacheClient != null && cacheClient.supportsPubSub();
  }

  @Override
  public void start() {
    if (!isSupported()) {
      logger.warn("[文档协作]当前缓存不支持发布订阅模式，无法启动");
      return;
    }

    if (messageListenerContainer == null) {
      messageListenerContainer = cacheClient.getMessageListenerContainer();
    }

    try {
      if (!messageListenerContainer.isRunning()) {
        messageListenerContainer.afterPropertiesSet();
        messageListenerContainer.start();
        logger.info("[文档协作]消息发布订阅服务启动成功");
      }
      else {
        logger.debug("[文档协作]消息发布订阅服务已在运行中");
      }
    }
    catch (Exception e) {
      logger.error("[文档协作]启动消息发布订阅服务失败", e);
      throw new RuntimeException("启动消息发布订阅服务失败", e);
    }
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public void stop() {
    if (messageListenerContainer == null || !messageListenerContainer.isRunning()) {
      return;
    }

    logger.info("[文档协作]开始停止消息发布订阅服务，当前订阅数量: {}", subscriptions.size());
    // 防御性清理：清理所有剩余的订阅（可能已经被 unsubscribe 清理过）
    if (!subscriptions.isEmpty()) {
      for (Map.Entry<String, SubscriptionInfo> entry : subscriptions.entrySet()) {
        try {
          messageListenerContainer.removeMessageListener(entry.getValue().adapter());
          logger.debug("[文档协作]清理剩余订阅: channel={}", entry.getKey());
        }
        catch (Exception e) {
          logger.warn("[文档协作]清理订阅失败: channel={}, error={}", entry.getKey(), e.getMessage());
        }
      }
      subscriptions.clear();
    }

    // 停止消息监听容器
    try {
      messageListenerContainer.stop();
      logger.info("[文档协作]消息发布订阅服务已停止");
    }
    catch (Exception e) {
      logger.error("[文档协作]停止消息发布订阅服务失败", e);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    start();
  }

  /**
     * 订阅信息内部类
     */
    private record SubscriptionInfo(String subscriptionId, MessageListenerAdapter adapter, ChannelTopic topic, MessageSubscriber subscriber) {

  }

  /**
     * MessageSubscriber 适配器
     * 将通用的 MessageSubscriber 适配为 Redis 的 MessageListener
     */
    private record MessageSubscriberAdapter(MessageSubscriber subscriber) implements MessageListener {

    @Override
      public void onMessage(Message message, byte[] pattern) {
        String channelName = new String(message.getChannel(), StandardCharsets.UTF_8);
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        // 调用通用的 MessageSubscriber 接口
        subscriber.onMessage(channelName, body);
      }
    }
}

