package com.iwhalecloud.bote.doc.module.collaboration.socket.message;

import io.netty.channel.ChannelHandlerContext;

/**
 * 消息处理器接口
 *
 * @param <T> 消息数据类型
 * @author Aiqing
 * @since 2025/08/29
 */
public interface MessageHandler<T> {

  /**
   * 处理消息
   *
   * @param ctx 通道上下文
   * @param message 消息数据
   */
  void handle(ChannelHandlerContext ctx, T message);

  /**
   * 获取支持的消息类型
   *
   * @return 消息类型的Class对象
   */
  Class<T> getMessageType();

  /**
   * 获取处理器名称
   *
   * @return 处理器名称
   */
  default String getHandlerName() {
    return this.getClass().getSimpleName();
  }
}
