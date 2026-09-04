package com.iwhalecloud.bote.doc.module.collaboration.socket.ws.endecode;

import com.iwhalecloud.bote.doc.module.collaboration.socket.message.SocketMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消息协议编码器
 * 将SocketSendInfo对象编码为WebSocket文本帧
 *
 * @author Aiqing
 * @since 2025/08/29
 */
@ChannelHandler.Sharable
public class MessageProtocolEncoder extends MessageToMessageEncoder<SocketMessage> {

  private static final Logger logger = LoggerFactory.getLogger(MessageProtocolEncoder.class);

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  protected void encode(ChannelHandlerContext ctx, SocketMessage message, List<Object> list) {
    try {
      String text = message.getData();
      TextWebSocketFrame frame = new TextWebSocketFrame(text);
      list.add(frame);
      logger.trace("消息编码成功: channel={}", ctx.channel().id().asShortText());
    }
    catch (Exception e) {
      logger.error("消息编码失败: channel={}, error={}", ctx.channel().id().asShortText(), e.getMessage(), e);
      throw e;
    }
  }
}
