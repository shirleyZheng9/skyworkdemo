package com.iwhalecloud.bote.doc.module.collaboration.workbook.handler;

import com.iwhalecloud.bote.doc.module.collaboration.constant.SocketBizEnum;
import com.iwhalecloud.bote.doc.module.collaboration.socket.service.SocketMessageSendService;
import com.iwhalecloud.bote.doc.module.collaboration.socket.session.SessionInfo;
import com.iwhalecloud.bote.doc.module.collaboration.socket.ws.univer.UniverCmdType;
import com.iwhalecloud.bote.doc.module.collaboration.socket.ws.univer.handler.AbstractUniverMessageHandler;
import com.iwhalecloud.bote.doc.module.collaboration.workbook.dto.UpdateCursorEventDTO;
import com.iwhalecloud.bote.doc.module.collaboration.workbook.dto.socket.CursorUpdateUpDTO;
import com.iwhalecloud.bote.doc.module.collaboration.workbook.dto.socket.UniverCommonDownDTO;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *
 * @author AiqingŒŒ
 * @since 2025/9/4
 */
@Component
@RequiredArgsConstructor
public class CursorChangesetHandler extends AbstractUniverMessageHandler<CursorUpdateUpDTO> {

  private static final Logger logger = LoggerFactory.getLogger(CursorChangesetHandler.class);

  private final SocketMessageSendService socketMessageSendService;

  @Override
  public UniverCmdType getCmdType() {
    return UniverCmdType.CURSOR_CHANGE;
  }

  @Override
  protected void doHandle(ChannelHandlerContext ctx, CursorUpdateUpDTO updateUpDTO) {
    // 广播同一文档的协作者
    SessionInfo sessionInfo = super.getSessionInfo(ctx);

    UpdateCursorEventDTO collaMsg = updateUpDTO.getCollaMsg();
    String routeKey = updateUpDTO.getRouteKey();

    UniverCommonDownDTO commonDownDTO = UniverCommonDownDTO.success(UniverCmdType.CHANGESET.getCode(),
      routeKey, collaMsg, null);

    int count = socketMessageSendService.sendToBusinessExcludeSession(SocketBizEnum.WORKSHEET.name(), routeKey,
      sessionInfo.getSessionId(), commonDownDTO);
    logger.trace("推送了 {} 个session, routeKey:{}", count, routeKey);
  }
}
