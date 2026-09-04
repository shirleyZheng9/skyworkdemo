package com.iwhalecloud.bote.doc.module.collaboration.pubsub;

/**
 * 消息订阅监听器接口
 * 通用的消息监听器，不依赖具体的消息中间件实现
 *
 * @author Aiqing
 * @since 2025/12/04
 */
public interface MessageSubscriber {

  /**
   * 处理接收到的消息
   *
   * @param channel 通道名称
   * @param message 消息内容（JSON字符串）
   */
  void onMessage(String channel, String message);
}

