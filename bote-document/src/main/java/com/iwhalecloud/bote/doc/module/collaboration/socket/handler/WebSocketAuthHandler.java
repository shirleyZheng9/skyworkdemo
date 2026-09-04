package com.iwhalecloud.bote.doc.module.collaboration.socket.handler;

import com.iwhalecloud.bote.doc.module.collaboration.constant.ConnectTypeEnum;
import com.iwhalecloud.bote.doc.module.collaboration.socket.auth.TokenValidator;
import com.iwhalecloud.bote.doc.module.collaboration.socket.auth.UserAuthInfo;
import com.iwhalecloud.bote.doc.module.collaboration.socket.session.ChannelSessionManager;
import com.iwhalecloud.bote.doc.module.collaboration.socket.session.SessionInfo;
import com.iwhalecloud.bote.doc.module.collaboration.socket.util.ClientUtils;
import com.iwhalecloud.bss.litchi.util.sequence.UUIDUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.AttributeKey;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WebSocket认证处理器
 * 在WebSocket握手之前进行token校验
 *
 * @author Aiqing
 * @since 2025/08/29
 */
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class WebSocketAuthHandler extends ChannelInboundHandlerAdapter {
  /**
   * 用户认证信息在Channel中的属性键
   */
  public static final AttributeKey<UserAuthInfo> USER_AUTH_INFO_ATTR = AttributeKey.valueOf("USER_AUTH_INFO");
  private static final Logger logger = LoggerFactory.getLogger(WebSocketAuthHandler.class);
  private final TokenValidator tokenValidator;

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (msg instanceof FullHttpRequest request) {
      // 只处理WebSocket升级请求
      if (isWebSocketUpgradeRequest(request)) {
        if (!authenticateRequest(ctx, request)) {
          // 认证失败，返回401响应并关闭连接
          sendUnauthorizedResponse(ctx);
          return;
        }
        // 认证成功后，创建会话信息
        createSessionInfo(ctx, request);
      }
    }
    // 认证成功，继续处理
    super.channelRead(ctx, msg);
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    // 获取会话信息用于日志记录
    UserAuthInfo authInfo = ctx.channel().attr(USER_AUTH_INFO_ATTR).get();
    String userId = authInfo != null ? String.valueOf(authInfo.getUserId()) : "unknown";

    if (cause instanceof ClosedChannelException) {
      // 连接已关闭，这是正常情况，不需要记录为错误
      logger.trace("WebSocket认证处理器：连接已关闭: {}, userId: {}", ctx.channel().remoteAddress(), userId);
    }
    else if (cause instanceof IOException && cause.getMessage() != null
      && cause.getMessage().contains("Connection reset by peer")) {
      // 客户端主动断开连接，这也是正常情况
      logger.trace("WebSocket认证处理器：客户端断开连接: {}, userId: {}, 原因: {}",
        ctx.channel().remoteAddress(), userId, cause.getMessage());
    }
    else {
      logger.error("WebSocket认证处理器异常: {}, 远程地址: {}, userId: {}",
        cause.getMessage(), ctx.channel().remoteAddress(), userId, cause);

      // 只有在连接仍然活跃时才关闭
      if (ctx.channel().isActive()) {
        ctx.close();
      }
    }
  }

  /**
   * 检查是否是WebSocket升级请求
   */
  private boolean isWebSocketUpgradeRequest(FullHttpRequest request) {
    String connection = request.headers().get("Connection");
    String upgrade = request.headers().get("Upgrade");
    return "Upgrade".equalsIgnoreCase(connection) && "websocket".equalsIgnoreCase(upgrade);
  }

  /**
   * 认证请求
   */
  private boolean authenticateRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
    try {
      UserAuthInfo authInfo = validateRequest(request);
      if (authInfo == null) {
        return false;
      }
      // 设置客户端信息
      String clientIp = ClientUtils.getClientIp(request.headers(), ctx);
      String userAgent = request.headers().get("User-Agent");
      authInfo.setClientIp(clientIp);
      authInfo.setUserAgent(userAgent);

      // 将认证信息存储到Channel属性中
      ctx.channel().attr(USER_AUTH_INFO_ATTR).set(authInfo);

      logger.info("WebSocket认证成功: userId={}, username={}, clientIp={}, uri={}",
        authInfo.getUserId(), authInfo.getUserName(), clientIp, request.uri());

      return true;

    }
    catch (Exception e) {
      logger.error("WebSocket认证异常: uri={}, error={}", request.uri(), e.getMessage(), e);
      return false;
    }
  }

  private UserAuthInfo validateRequest(FullHttpRequest request) {
    String uri = request.uri();
    Map<String, String> params = ClientUtils.parseQueryParams(uri);
    // 从URL参数中获取token
    String token = params.get("token");
    if (token == null || token.trim().isEmpty()) {
      UserAuthInfo authInfo = tokenValidator.validateFromRequest(request);
      if (authInfo != null) {
        return authInfo;
      }
      logger.debug("WebSocket认证失败：缺少token参数, uri={}", uri);
      return null;
    }
    String connectType = params.getOrDefault("connectType", ConnectTypeEnum.OUTER.name());
    // 校验token
    return tokenValidator.validateToken(token, connectType);
  }

  /**
   * 发送401未授权响应
   */
  private void sendUnauthorizedResponse(ChannelHandlerContext ctx) {
    DefaultHttpResponse response = new DefaultHttpResponse(
      HttpVersion.HTTP_1_1,
      HttpResponseStatus.UNAUTHORIZED
    );
    response.headers().set("Content-Type", "text/plain; charset=UTF-8");
    response.headers().set("Content-Length", "0");
    response.headers().set("Connection", "close");

    ctx.writeAndFlush(response).addListener(future -> {
      logger.warn("WebSocket认证失败，已发送401响应并关闭连接: {}", ctx.channel().remoteAddress());
      ctx.close();
    });
  }

  /**
   * 创建会话信息
   */
  private void createSessionInfo(ChannelHandlerContext ctx, FullHttpRequest request) {
    try {
      String uri = request.uri();
      Map<String, String> params = ClientUtils.parseQueryParams(uri);

      // 获取认证信息
      UserAuthInfo authInfo = ctx.channel().attr(USER_AUTH_INFO_ATTR).get();
      if (authInfo == null) {
        logger.error("找不到用户认证信息，无法创建会话");
        return;
      }
      ChannelId channelId = ctx.channel().id();
      logger.info("channel info, channelShortId:{}, longId:{}", channelId.asShortText(), channelId.asLongText());
      SessionInfo sessionInfo = new SessionInfo();
      // 生成会话ID
      sessionInfo.setSessionId(generateSessionId());
      // 从认证信息中获取用户信息
      sessionInfo.setUserId(authInfo.getUserId());

      String tenantId = params.get("tenantId");
      if (StringUtils.isNotBlank(tenantId)) {
        sessionInfo.setTenantId(Long.parseLong(tenantId));
      }
      // 默认web
      sessionInfo.setTerminalType(1);

      // 从认证信息中获取用户详细信息
      sessionInfo.setAttribute("userName", authInfo.getUserName());
      sessionInfo.setAttribute("userAgent", Optional.ofNullable(authInfo.getUserAgent()).orElse("-"));
      sessionInfo.setAttribute("clientIp", Optional.ofNullable(authInfo.getClientIp()).orElse("-"));

      // 复制认证信息中的扩展属性
      if (authInfo.getAttributes() != null) {
        authInfo.getAttributes().forEach((key, value) -> {
          if (key != null) {
            sessionInfo.setAttribute(key, value);
          }
        });
      }
      // 临时存储会话信息，在握手完成后正式注册
      ctx.channel().attr(ChannelSessionManager.SESSION_INFO_ATTR).set(sessionInfo);

      logger.debug("WebSocket会话信息创建成功: uri={}, sessionId={}, userId={}",
        uri, sessionInfo.getSessionId(), sessionInfo.getUserId());

    }
    catch (Exception e) {
      logger.error("创建会话信息失败: uri={}, error={}", request.uri(), e.getMessage(), e);
    }
  }

  /**
   * 生成会话ID
   */
  private String generateSessionId() {
    return UUIDUtils.randomFormatUuid();
  }
}
