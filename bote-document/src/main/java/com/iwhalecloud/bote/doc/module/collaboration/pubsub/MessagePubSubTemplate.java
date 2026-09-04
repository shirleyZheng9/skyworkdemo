package com.iwhalecloud.bote.doc.module.collaboration.pubsub;

/**
 * 消息发布订阅模板接口
 * 提供统一的发布订阅抽象，隐藏底层实现细节，方便在不同环境中切换
 *
 * @author Aiqing
 * @since 2025/12/04
 */
public interface MessagePubSubTemplate {

  /**
   * 订阅指定通道的消息
   *
   * @param channel 通道名称
   * @param subscriber 消息订阅监听器
   * @return 订阅标识，用于后续取消订阅
   */
  String subscribe(String channel, MessageSubscriber subscriber);

  /**
   * 取消订阅指定通道
   *
   * @param channel 通道名称
   * @param subscriptionId 订阅标识
   */
  void unsubscribe(String channel, String subscriptionId);

  /**
   * 发布消息到指定通道
   *
   * @param channel 通道名称
   * @param message 消息内容
   * @return 是否成功发布
   */
  boolean publish(String channel, String message);

  /**
   * 检查是否支持发布订阅功能
   *
   * @return 是否支持
   */
  boolean isSupported();

  /**
   * 启动发布订阅服务
   * 通常在初始化时调用
   */
  void start();

  /**
   * 停止发布订阅服务
   * 通常在应用关闭时调用
   */
  void stop();
}

