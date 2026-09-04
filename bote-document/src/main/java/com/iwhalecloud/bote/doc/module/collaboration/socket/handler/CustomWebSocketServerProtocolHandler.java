package com.iwhalecloud.bote.doc.module.collaboration.socket.handler;

import com.iwhalecloud.bote.doc.module.collaboration.constant.SocketConst;
import com.iwhalecloud.bote.doc.module.collaboration.socket.config.WebSocketProperties;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolConfig;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 支持多路径的WebSocket协议处理器
 *
 * @author Aiqing
 * @since 2025/9/3
 */
public class CustomWebSocketServerProtocolHandler extends WebSocketServerProtocolHandler {
  private static final Logger logger = LoggerFactory.getLogger(CustomWebSocketServerProtocolHandler.class);

  private final String basePath;
  private final boolean checkStartsWith;

  public CustomWebSocketServerProtocolHandler(WebSocketProperties properties) {
    super(WebSocketServerProtocolConfig.newBuilder()
      .websocketPath(properties.getPath())
      .checkStartsWith(properties.isCheckStartsWith())
      .maxFramePayloadLength(properties.getMaxContentLength())
      .allowExtensions(properties.isEnableCompression())
      .allowMaskMismatch(properties.isAllowMaskMismatch())
      .handshakeTimeoutMillis(properties.getHandshakeTimeoutMillis())
      .build());
    this.basePath = properties.getPath();
    this.checkStartsWith = properties.isCheckStartsWith();
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (msg instanceof FullHttpRequest request) {
      String uri = request.uri();

      // 检查是否匹配WebSocket路径
      if (isWebSocketPath(uri)) {
        // 将路径信息存储到Channel属性中
        ctx.channel().attr(AttributeKey.valueOf(SocketConst.WEBSOCKET_PATH)).set(uri);
        logger.trace("WebSocket路径匹配成功: uri={}, channel={}", uri, ctx.channel().id().asShortText());
      }
      else {
        logger.warn("WebSocket路径不匹配: uri={}, basePath={}, checkStartsWith={}, channel={}",
          uri, basePath, checkStartsWith, ctx.channel().id().asShortText());
      }
    }

    super.channelRead(ctx, msg);
  }

  private boolean isWebSocketPath(String uri) {
    if (checkStartsWith) {
      return uri.startsWith(basePath);
    }
    else {
      return uri.equals(basePath);
    }
  }
}
