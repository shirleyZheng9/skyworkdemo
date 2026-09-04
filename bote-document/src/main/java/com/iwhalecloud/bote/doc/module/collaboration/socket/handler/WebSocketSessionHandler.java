package com.iwhalecloud.bote.doc.module.collaboration.socket.handler;

import com.iwhalecloud.bote.doc.module.collaboration.socket.session.ChannelSessionManager;
import com.iwhalecloud.bote.doc.module.collaboration.socket.session.SessionInfo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WebSocket会话处理器
 * 负责处理WebSocket握手完成后的会话注册
 * 会话创建已在WebSocketAuthHandler中完成
 *
 * @author Aiqing
 * @since 2025/08/29
 */
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class WebSocketSessionHandler extends ChannelInboundHandlerAdapter {
  private static final Logger logger = LoggerFactory.getLogger(WebSocketSessionHandler.class);

  private final ChannelSessionManager sessionManager;

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
      onHandshakeComplete(ctx, (WebSocketServerProtocolHandler.HandshakeComplete) evt);
    }
    super.userEventTriggered(ctx, evt);
  }


  /**
   * WebSocket握手完成
   */
  private void onHandshakeComplete(ChannelHandlerContext ctx, WebSocketServerProtocolHandler.HandshakeComplete evt) {
    SessionInfo sessionInfo = ctx.channel().attr(ChannelSessionManager.SESSION_INFO_ATTR).get();
    if (sessionInfo != null) {
      // 正式注册会话
      sessionManager.addSession(ctx, sessionInfo);
      logger.info("WebSocket连接建立: sessionId={}, userId={}, requestUri={}",
        sessionInfo.getSessionId(), sessionInfo.getUserId(), evt.requestUri());
    }
    else {
      logger.warn("WebSocket握手完成，但找不到会话信息: requestUri={}", evt.requestUri());
    }
  }


}
