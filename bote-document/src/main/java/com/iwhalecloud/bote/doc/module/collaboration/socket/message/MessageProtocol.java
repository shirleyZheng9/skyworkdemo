package com.iwhalecloud.bote.doc.module.collaboration.socket.message;

import com.iwhalecloud.bote.doc.module.collaboration.socket.SocketProtocolEnum;
import io.netty.channel.ChannelHandlerContext;

/**
 * 消息协议接口
 *
 * @author Aiqing
 * @since 2025/9/3
 */
public interface MessageProtocol {

  /**
   * 获取协议名称
   *
   * @return 协议名称
   */
  SocketProtocolEnum getProtocol();

  /**
   * socket接入的路径
   *
   * @return 路径
   */
  String getSocketPath();


  /**
   * 解析消息
   *
   * @param ctx channel
   * @param message socket上行消息
   */
  void channelRead(ChannelHandlerContext ctx, String message);

}
