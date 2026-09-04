package com.iwhalecloud.bote.doc.module.collaboration.socket.ws.generic.handler;

import com.iwhalecloud.bote.doc.module.collaboration.socket.message.SocketMessage;
import com.iwhalecloud.bote.doc.module.collaboration.socket.service.SocketMessageSendService;
import com.iwhalecloud.bote.doc.module.collaboration.socket.session.SessionInfo;
import com.iwhalecloud.bote.doc.module.collaboration.socket.ws.generic.GenericCmdType;
import com.iwhalecloud.bote.doc.module.collaboration.socket.ws.generic.model.HeartbeatInfo;
import com.iwhalecloud.bote.doc.module.collaboration.socket.ws.generic.model.SocketSendInfo;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 心跳消息处理器
 *
 * @author Aiqing
 * @since 2025/08/29
 */
@Component
@RequiredArgsConstructor
public class HeartbeatMessageHandler extends AbstractGenericProtocolMessageHandler<HeartbeatInfo> {

  private static final Logger logger = LoggerFactory.getLogger(HeartbeatMessageHandler.class);
  private final SocketMessageSendService socketMessageSendService;

  @Override
  public GenericCmdType getCmdType() {
    return GenericCmdType.HEART_BEAT;
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  protected void doHandle(ChannelHandlerContext ctx, HeartbeatInfo heartbeatInfo) {
    SessionInfo sessionInfo = getSessionInfo(ctx);
    if (sessionInfo == null) {
      logger.warn("心跳处理失败：找不到会话信息，channel={}", ctx.channel().id().asShortText());
      return;
    }
    // 响应心跳
    SocketSendInfo<HeartbeatResponse> response = new SocketSendInfo<>();
    response.setCmd(GenericCmdType.HEART_BEAT.getCode());
    response.setData(new HeartbeatResponse(System.currentTimeMillis()));

    ctx.channel().writeAndFlush(new SocketMessage(JsonUtil.toJsonString(response)));

    // 更新心跳次数
    Integer heartbeatTimes = sessionInfo.getAttribute("heartbeatTimes");
    if (heartbeatTimes == null) {
      heartbeatTimes = 0;
    }
    heartbeatTimes++;
    sessionInfo.setAttribute("heartbeatTimes", heartbeatTimes);

    logger.trace("心跳处理完成: sessionId={}, userId={}, heartbeatTimes={}",
      sessionInfo.getSessionId(), sessionInfo.getUserId(), heartbeatTimes);
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  protected void handleException(ChannelHandlerContext ctx, HeartbeatInfo message, Exception e) {
    logger.error("心跳处理异常: channel={}, error={}", ctx.channel().id().asShortText(), e.getMessage());
    // 心跳异常通常不需要关闭连接，只记录日志
  }

  /**
   * 心跳响应数据
   */
  @Getter
  public static class HeartbeatResponse {
    private final long timestamp;

    public HeartbeatResponse(long timestamp) {
      this.timestamp = timestamp;
    }

  }
}
