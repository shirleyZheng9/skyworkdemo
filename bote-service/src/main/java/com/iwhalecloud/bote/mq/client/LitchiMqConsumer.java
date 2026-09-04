package com.iwhalecloud.bote.mq.client;

import com.iwhalecloud.bote.dto.mq.MqConfig;
import com.iwhalecloud.bote.mq.AbstractMqClient;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.mq.consts.MQProcessStatus;
import com.iwhalecloud.bss.litchi.mq.consumer.MQConsumer;
import com.iwhalecloud.bss.litchi.mq.dto.ConsumerMessage;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.apache.commons.lang3.ArrayUtils;

/**
 * litchi-mq 消费者
 *
 * @author chen.linfa
 * @since 2025-12-04
 */
@SuppressWarnings("PMD.GuardLogStatement")
public class LitchiMqConsumer extends AbstractMqClient<MQConsumer> {
  /** 处理器映射。key 为主题名称 */
  private final Map<String, Function<ConsumerMessage, MQProcessStatus>> topicHandlerMap = new ConcurrentHashMap<>();

  public LitchiMqConsumer(Long mqInstId, MqConfig mqConfig, MQConsumer client) {
    super(mqInstId, mqConfig, client);
  }

  /**
   * 订阅主题
   * <p>注意：需要覆盖已订阅的主题列表，以前订阅但现在不再订阅的需要取消订阅。</p>
   * <p>订阅失败时抛异常，不要静默忽略。</p>
   *
   * @param topic 主题
   * @param handler 处理器
   */
  @SuppressWarnings("java:S2139")
  public void subscribe(String topic, Function<ConsumerMessage, MQProcessStatus> handler) {
    // 先取消已订阅的主题，避免重复订阅导致的异常
    unsubscribeTopic(topic);
    // 即使已经订阅的也重新订阅一下（MQ 客户端会自动覆盖），以兼容一些异常情况（可能之前未实际订阅成功）
    try {
      client.subscribe(topic, this::handleMessage);
      topicHandlerMap.put(topic, handler);
      logger.debug("Subscribed to topic: mqInstId={}, topic={}", mqInstId, topic);
    }
    catch (Exception e) {
      logger.error("Failed to subscribe topic: mqInstId={}, topic={}", mqInstId, topic, e);
      // 抛异常以便外部感知到订阅失败并获取错误信息
      throw new BssException(e);
    }
  }

  /**
   * 处理消息
   * <p>处理失败时只打印日志，不抛异常，避免导致线程池挂掉。</p>
   *
   * @param message MQ 消息
   * @return 处理状态
   */
  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private MQProcessStatus handleMessage(ConsumerMessage message) {
    String topic = message.getTopic();
    Function<ConsumerMessage, MQProcessStatus> handler = topicHandlerMap.get(topic);
    // 找不到消费者配置时忽略
    if (handler == null) {
      logger.warn("No mq consumer found: mqInstId={}, topic={}", mqInstId, topic);
      return MQProcessStatus.DONE;
    }

    try {
      return handler.apply(message);
    }
    catch (RuntimeException e) {
      int length = ArrayUtils.getLength(message.getBody());
      // 消息体太大时只打印大小，不打印内容
      if (length > 0 && length < 1024 * 10) {
        // 转为字符串以便阅读
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        logger.error("Failed to handle mq message: mqInstId={}, topic={}, msgId={}, body={}", mqInstId, topic, message.getMsgId(), body, e);
      }
      else {
        logger.error("Failed to handle mq message: mqInstId={}, topic={}, msgId={}, bodySize={}", mqInstId, topic, message.getMsgId(), length, e);
      }
      return MQProcessStatus.RETRY;
    }
  }

  /**
   * 取消订阅已存在的所有主题
   */
  private void unsubscribeTopic(String topic) {
    // 先取消所有已订阅的主题，避免重复订阅导致的异常
    try {
      Set<String> subscribedTopics = client.getSubscribedTopics();
      for (String currentTopic : subscribedTopics) {
        if (topic.equals(currentTopic)) {
          client.unsubscribe(topic);
          topicHandlerMap.remove(topic);
          logger.debug("Unsubscribed from topic: mqInstId={}, topic={}", mqInstId, topic);
        }
      }
    }
    catch (Exception e) {
      // 取消订阅失败影响不大（收到消息后找不到 MQ 消费者会忽略），只记录日志
      logger.warn("Failed to unsubscribe existing topics: mqInstId={}", mqInstId, e);
    }
  }

  @Override
  public boolean isHealthy() {
    return client.isHealthy();
  }
}
