package com.iwhalecloud.bote.doc.module.collaboration.workbook.handler;

import com.iwhalecloud.bote.doc.module.collaboration.socket.service.SocketMessageSendService;
import com.iwhalecloud.bote.doc.module.collaboration.socket.session.SessionInfo;
import com.iwhalecloud.bote.doc.module.collaboration.socket.ws.univer.UniverCmdType;
import com.iwhalecloud.bote.doc.module.collaboration.socket.ws.univer.handler.AbstractUniverMessageHandler;
import com.iwhalecloud.bote.doc.module.collaboration.workbook.dto.socket.RegisterDownDTO;
import com.iwhalecloud.bote.doc.module.collaboration.workbook.dto.socket.RegisterDownDTO.InfoRspDTO;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 链接注册的响应处理
 *
 * @author Aiqing
 * @since 2025/9/3
 */
@Component
@RequiredArgsConstructor
public class RegisterMessageHandler extends AbstractUniverMessageHandler<String> {

  private final SocketMessageSendService messageSendService;

  @Override
  public UniverCmdType getCmdType() {
    return UniverCmdType.HEART_BEAT;
  }

  @Override
  protected void doHandle(ChannelHandlerContext ctx, String message) {
    SessionInfo sessionInfo = super.getSessionInfo(ctx);
    Long userId = sessionInfo.getUserId();
    if (userId == null) {
      userId = IDUtils.nextId();
      sessionInfo.setUserId(userId);
    }

    RegisterDownDTO downDTO = new RegisterDownDTO();
    RegisterDownDTO.InfoRspDTO infoRspDTO = new InfoRspDTO();
    infoRspDTO.setMemberID(String.valueOf(userId));
    downDTO.setInfoRsp(infoRspDTO);
    messageSendService.sendToSession(sessionInfo.getSessionId(), downDTO);
  }
}
