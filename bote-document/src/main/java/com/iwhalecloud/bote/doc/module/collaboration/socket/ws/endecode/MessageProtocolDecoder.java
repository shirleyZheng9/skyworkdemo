package com.iwhalecloud.bote.doc.module.collaboration.socket.ws.endecode;

import com.iwhalecloud.bote.doc.module.collaboration.socket.message.SocketMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消息协议解码器
 * 将WebSocket文本帧解码为SocketSendInfo对象
 * 使用@Sharable注解允许在多个Channel间共享实例
 *
 * @author Aiqing
 * @since 2025/08/29
 */
@ChannelHandler.Sharable
public class MessageProtocolDecoder extends MessageToMessageDecoder<TextWebSocketFrame> {

  private static final Logger logger = LoggerFactory.getLogger(MessageProtocolDecoder.class);

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  protected void decode(ChannelHandlerContext ctx, TextWebSocketFrame frame, List<Object> list) {
    try {
      String text = frame.text();
      if (StringUtils.isBlank(text)) {
        return;
      }
      list.add(new SocketMessage(text));
      logger.trace("消息解码成功: channel={}", ctx.channel().id().asShortText());
    }
    catch (Exception e) {
      logger.error("消息解码失败: text={}, channel={}, error={}",
        frame.text(), ctx.channel().id().asShortText(), e.getMessage(), e);
      throw e;
    }
  }
}
