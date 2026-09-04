package com.iwhalecloud.bote.doc.module.collaboration.pubsub.none;

import com.iwhalecloud.bote.doc.module.collaboration.pubsub.MessagePubSubTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 空实现的消息发布订阅模板
 * 用于禁用功能或不兼容的场景
 *
 * @author Aiqing
 * @since 2025/12/4
 */
public class NoOpMessagePubSubTemplate implements MessagePubSubTemplate {
  private static final Logger logger = LoggerFactory.getLogger(NoOpMessagePubSubTemplate.class);

  @Override
  public String subscribe(String channel, com.iwhalecloud.bote.doc.module.collaboration.pubsub.MessageSubscriber subscriber) {
    logger.debug("NoOp实现: 跳过订阅 channel={}", channel);
    return null;
  }

  @Override
  public void unsubscribe(String channel, String subscriptionId) {
    logger.debug("NoOp实现: 跳过取消订阅 channel={}", channel);
  }

  @Override
  public boolean publish(String channel, String message) {
    logger.debug("NoOp实现: 跳过发布消息 channel={}", channel);
    return false;
  }

  @Override
  public boolean isSupported() {
    return false;
  }

  @Override
  public void start() {
    logger.debug("NoOp实现: 跳过启动");
  }

  @Override
  public void stop() {
    logger.debug("NoOp实现: 跳过停止");
  }
}
