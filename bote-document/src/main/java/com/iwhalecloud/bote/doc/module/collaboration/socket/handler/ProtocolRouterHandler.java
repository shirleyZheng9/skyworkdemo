package com.iwhalecloud.bote.doc.module.collaboration.socket.handler;

import com.iwhalecloud.bote.doc.module.collaboration.constant.SocketConst;
import com.iwhalecloud.bote.doc.module.collaboration.socket.ProtocolRegistry;
import com.iwhalecloud.bote.doc.module.collaboration.socket.config.WebSocketProperties;
import com.iwhalecloud.bote.doc.module.collaboration.socket.message.MessageProtocol;
import com.iwhalecloud.bote.doc.module.collaboration.socket.message.SocketMessage;
import com.iwhalecloud.bote.doc.module.collaboration.socket.protocol.GenericMessageProtocol;
import com.iwhalecloud.bote.doc.module.collaboration.socket.session.ChannelSessionManager;
import com.iwhalecloud.bote.doc.module.collaboration.socket.session.SessionInfo;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 协议感知的路径路由处理器 - 根据路径使用不同的协议进行消息的处理
 *
 * @author Aiqing
 * @since 2025/9/3
 */
@Component
@RequiredArgsConstructor
@Sharable
@SuppressWarnings("PMD.GuardLogStatement")
public class ProtocolRouterHandler extends SimpleChannelInboundHandler<SocketMessage> {

  private static final Logger logger = LoggerFactory.getLogger(ProtocolRouterHandler.class);

  private final ProtocolRegistry protocolRegistry;
  private final GenericMessageProtocol genericMessageProtocol;
  private final WebSocketProperties webSocketProperties;
  private final ChannelSessionManager channelSessionManager;

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, SocketMessage message) {
    // 获取当前连接的WebSocket路径
    String path = getWebSocketPath(ctx);
    MessageProtocol protocol = protocolRegistry.getProtocolByPath(path);
    if (protocol != null) {
      // 使用路径对应的协议处理WebSocket帧
      if (message != null) {
        // 将处理后的消息传递给对应的业务处理器
        protocol.channelRead(ctx, message.getData());
      }
    }
    else {
      this.genericMessageProtocol.channelRead(ctx, message.getData());
    }
  }

  private String getWebSocketPath(ChannelHandlerContext ctx) {
    // 从Channel属性中获取WebSocket路径
    Object pathAttr = ctx.channel().attr(AttributeKey.valueOf(SocketConst.WEBSOCKET_PATH)).get();
    if (pathAttr != null) {
      return (String) pathAttr;
    }
    // 如果属性中没有路径信息，返回默认路径
    return webSocketProperties.getGenericSocketPath();
  }

  /**
   * 连接建立
   */
  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public void handlerAdded(ChannelHandlerContext ctx) {
    logger.debug("新连接建立: channel={}", ctx.channel().id().asLongText());
  }

  /**
   * 连接断开
   */
  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public void handlerRemoved(ChannelHandlerContext ctx) {
    try {
      SessionInfo sessionInfo = ctx.channel().attr(ChannelSessionManager.SESSION_INFO_ATTR).get();
      if (sessionInfo != null) {
        channelSessionManager.removeSession(sessionInfo.getSessionId());
        logger.debug("连接正常断开: sessionId={}, userId={}, channel={}",
          sessionInfo.getSessionId(), sessionInfo.getUserId(), ctx.channel().id().asLongText());
      }
      else {
        logger.debug("连接断开但无会话信息: channel={}", ctx.channel().id().asLongText());
      }
    }
    catch (Exception e) {
      logger.error("处理连接断开时发生异常: channel={}, error={}",
        ctx.channel().id().asLongText(), e.getMessage(), e);
    }
  }

  /**
   * 用户事件处理（如空闲检测）
   */
  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    if (evt instanceof IdleStateEvent idleEvent) {
      if (idleEvent.state() == IdleState.READER_IDLE) {
        // 读空闲超时，断开连接
        SessionInfo sessionInfo = ctx.channel().attr(ChannelSessionManager.SESSION_INFO_ATTR).get();
        String sessionId = sessionInfo != null ? sessionInfo.getSessionId() : "unknown";
        Long userId = sessionInfo != null ? sessionInfo.getUserId() : null;

        logger.debug("心跳超时，断开连接: sessionId={}, userId={}, channel={}",
          sessionId, userId, ctx.channel().id().asShortText());
        ctx.close();
      }
    }
    else {
      super.userEventTriggered(ctx, evt);
    }
  }

  /**
   * 异常处理
   */
  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    SessionInfo sessionInfo = ctx.channel().attr(ChannelSessionManager.SESSION_INFO_ATTR).get();
    String sessionId = sessionInfo != null ? sessionInfo.getSessionId() : "unknown";

    logger.error("通道异常: sessionId={}, channel={}, error={}",
      sessionId, ctx.channel().id().asShortText(), cause.getMessage(), cause);

    // 可以根据异常类型决定是否关闭连接
    // ctx.close();
  }
}
