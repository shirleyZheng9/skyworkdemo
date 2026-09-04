package com.iwhalecloud.bote.doc.module.collaboration.workbook.handler;

import com.iwhalecloud.bote.doc.module.collaboration.socket.service.SocketMessageSendService;
import com.iwhalecloud.bote.doc.module.collaboration.socket.session.SessionInfo;
import com.iwhalecloud.bote.doc.module.collaboration.socket.ws.univer.UniverCmdType;
import com.iwhalecloud.bote.doc.module.collaboration.socket.ws.univer.handler.AbstractUniverMessageHandler;
import com.iwhalecloud.bote.doc.module.collaboration.workbook.dto.socket.UniverCommonDownDTO;
import io.netty.channel.ChannelHandlerContext;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 用户同步socket处理
 *
 * @author Aiqing
 * @since 2025/9/4
 */
@Component
@RequiredArgsConstructor
public class MemberSyncHandler extends AbstractUniverMessageHandler<String> {

  private final SocketMessageSendService socketMessageSendService;

  @Override
  public UniverCmdType getCmdType() {
    return UniverCmdType.SYNC_MEMBER;
  }

  @Override
  protected void doHandle(ChannelHandlerContext ctx, String message) {
    SessionInfo sessionInfo = super.getSessionInfo(ctx);
    Map<String, String> infoRsp = new HashMap<>();
    infoRsp.put("memberID", String.valueOf(sessionInfo.getUserId()));
    UniverCommonDownDTO commonDownDTO = UniverCommonDownDTO.success(UniverCmdType.SYNC_MEMBER.getCode(),
      null, null, infoRsp);
    socketMessageSendService.sendToSession(sessionInfo.getSessionId(), commonDownDTO);
  }
}
