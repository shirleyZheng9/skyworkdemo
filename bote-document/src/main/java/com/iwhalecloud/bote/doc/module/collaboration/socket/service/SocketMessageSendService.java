package com.iwhalecloud.bote.doc.module.collaboration.socket.service;

import com.iwhalecloud.bote.doc.module.collaboration.socket.message.SocketMessage;
import com.iwhalecloud.bote.doc.module.collaboration.socket.session.ChannelSessionManager;
import com.iwhalecloud.bote.doc.module.collaboration.socket.session.SessionInfo;
import com.iwhalecloud.bote.doc.module.collaboration.socket.ws.generic.GenericCmdType;
import com.iwhalecloud.bote.doc.module.collaboration.socket.ws.generic.model.SocketSendInfo;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 消息发送服务
 * 提供统一的消息发送能力，支持单播、多播、广播等多种发送模式
 *
 * @author Aiqing
 * @since 2025/08/29
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class SocketMessageSendService {
  private static final Logger logger = LoggerFactory.getLogger(SocketMessageSendService.class);

  private final ChannelSessionManager sessionManager;

  /**
   * 发送消息到指定会话
   *
   * @param sessionId 会话ID
   * @param data 消息数据
   * @return 是否发送成功
   */
  public boolean sendToSession(String sessionId, Object data) {
    ChannelHandlerContext ctx = sessionManager.getChannel(sessionId);
    if (ctx == null) {
      logger.warn("发送消息失败：找不到会话 sessionId={}", sessionId);
      return false;
    }

    return sendMessage(ctx, data);
  }

  /**
   * 发送消息到指定用户的所有会话
   *
   * @param userId 用户ID
   * @param data 消息数据
   * @return 发送成功的会话数量
   */
  public int sendToUser(Long userId, Object data) {
    List<ChannelHandlerContext> channels = sessionManager.getUserChannels(userId);
    return sendToChannels(channels, data);
  }

  /**
   * 发送消息到指定用户的指定终端类型
   *
   * @param userId 用户ID
   * @param terminalType 终端类型
   * @param data 消息数据
   * @return 发送成功的会话数量
   */
  public int sendToUser(Long userId, Integer terminalType, Object data) {
    List<SessionInfo> sessions = sessionManager.getUserSessions(userId, terminalType);
    int successCount = 0;

    for (SessionInfo session : sessions) {
      if (sendToSession(session.getSessionId(), data)) {
        successCount++;
      }
    }

    return successCount;
  }

  /**
   * 发送消息到指定业务的所有会话
   *
   * @param businessId 业务ID
   * @param data 消息数据
   * @return 发送成功的会话数量
   */
  public int sendToBusiness(String businessType, String businessId, Object data) {
    List<ChannelHandlerContext> channels = sessionManager.getBusinessChannels(businessType, businessId);
    return sendToChannels(channels, data);
  }

  /**
   * 发送消息到指定业务的所有会话, 排除指定的session
   *
   * @param businessId 业务ID
   * @param data 消息数据
   * @return 发送成功的会话数量
   */
  public int sendToBusinessExcludeSession(String businessType, String businessId, String excludeSessionId, Object data) {
    List<ChannelHandlerContext> channels = sessionManager.getBusinessChannels(businessType, businessId);
    List<ChannelHandlerContext> filterChannels = channels.stream().filter(ctx -> {
      SessionInfo sessionInfo = ctx.channel().attr(ChannelSessionManager.SESSION_INFO_ATTR).get();
      return !Objects.equals(sessionInfo.getSessionId(), excludeSessionId);
    }).collect(Collectors.toList());
    return sendToChannels(filterChannels, data);
  }

  /**
   * 发送消息到业务中除指定用户外的所有会话
   *
   * @param businessId 业务ID
   * @param excludeUserId 排除的用户ID
   * @param data 消息数据
   * @return 发送成功的会话数量
   */
  public int sendToBusinessExcludeUser(String businessType,
                                       String businessId,
                                       Long excludeUserId,
                                       Object data) {
    List<SessionInfo> sessions = sessionManager.getBusinessSessions(businessType, businessId);
    int successCount = 0;

    for (SessionInfo session : sessions) {
      if (!excludeUserId.equals(session.getUserId())) {
        if (sendToSession(session.getSessionId(), data)) {
          successCount++;
        }
      }
    }

    return successCount;
  }

  /**
   * 根据条件发送消息
   *
   * @param predicate 会话过滤条件
   * @param data 消息数据
   * @return 发送成功的会话数量
   */
  public int sendByCondition(Predicate<SessionInfo> predicate, Object data) {
    List<SessionInfo> sessions = sessionManager.findSessions(predicate);
    int successCount = 0;

    for (SessionInfo session : sessions) {
      if (sendToSession(session.getSessionId(), data)) {
        successCount++;
      }
    }

    return successCount;
  }

  /**
   * 异步发送消息到指定会话
   *
   * @param sessionId 会话ID
   * @param data 消息数据
   * @return CompletableFuture
   */
  public CompletableFuture<Boolean> sendToSessionAsync(String sessionId, Object data) {
    return CompletableFuture.supplyAsync(() -> sendToSession(sessionId, data));
  }

  /**
   * 异步发送消息到指定用户
   *
   * @param userId 用户ID
   * @param data 消息数据
   * @return CompletableFuture
   */
  public CompletableFuture<Integer> sendToUserAsync(Long userId, Object data) {
    return CompletableFuture.supplyAsync(() -> sendToUser(userId, data));
  }

  /**
   * 异步发送消息到指定业务
   *
   * @param businessId 业务ID
   * @param data 消息数据
   * @return CompletableFuture
   */
  public CompletableFuture<Integer> sendToBusinessAsync(String businessType, String businessId, Object data) {
    return CompletableFuture.supplyAsync(() -> sendToBusiness(businessType, businessId, data));
  }

  /**
   * 批量发送消息到多个会话
   */
  private int sendToChannels(List<ChannelHandlerContext> channels, Object data) {
    int successCount = 0;

    for (ChannelHandlerContext ctx : channels) {
      if (sendMessage(ctx, data)) {
        successCount++;
      }
    }

    return successCount;
  }

  /**
   * 发送消息到指定通道
   */
  private boolean sendMessage(ChannelHandlerContext ctx, Object data) {
    try {
      if (ctx == null || !ctx.channel().isActive()) {
        logger.atWarn().setMessage("发送消息失败：通道无效 channel={}").addArgument(() -> ctx != null ? ctx.channel().id().asShortText() : "null").log();
        return false;
      }

      ctx.channel().writeAndFlush(new SocketMessage(JsonUtil.toJsonString(data)));
      if (logger.isTraceEnabled()) {
        SessionInfo sessionInfo = ctx.channel().attr(ChannelSessionManager.SESSION_INFO_ATTR).get();
        logger.atTrace().setMessage("消息发送成功: channel={}, sessionId={}")
          .addArgument(() -> ctx.channel().id().asShortText())
          .addArgument(sessionInfo::getSessionId)
          .log();
      }
      return true;

    }
    catch (Exception e) {
      logger.error("发送消息异常: channel={}, error={}",
        ctx != null ? ctx.channel().id().asShortText() : "null", e.getMessage(), e);
      return false;
    }
  }

  /**
   * 创建消息发送信息
   *
   * @param genericCmdType 命令类型
   * @param data 消息数据
   * @return 消息发送信息
   */
  public <T> SocketSendInfo<T> createGenericMessage(GenericCmdType genericCmdType, T data) {
    SocketSendInfo<T> sendInfo = new SocketSendInfo<>();
    sendInfo.setCmd(genericCmdType.getCode());
    sendInfo.setData(data);
    return sendInfo;
  }
}
