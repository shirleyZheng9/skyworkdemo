package com.iwhalecloud.bote.doc.module.collaboration.workbook.handler;

import com.iwhalecloud.bote.doc.module.collaboration.cache.WorkbookCollaborationCache;
import com.iwhalecloud.bote.doc.module.collaboration.constant.SocketBizEnum;
import com.iwhalecloud.bote.doc.module.collaboration.socket.service.SocketMessageSendService;
import com.iwhalecloud.bote.doc.module.collaboration.socket.session.ChannelSessionManager;
import com.iwhalecloud.bote.doc.module.collaboration.socket.session.SessionInfo;
import com.iwhalecloud.bote.doc.module.collaboration.socket.ws.univer.UniverCmdType;
import com.iwhalecloud.bote.doc.module.collaboration.socket.ws.univer.handler.AbstractUniverMessageHandler;
import com.iwhalecloud.bote.doc.module.collaboration.workbook.dto.socket.RoomJoinDownDTO;
import com.iwhalecloud.bote.doc.module.collaboration.workbook.dto.socket.RoomJoinDownDTO.JoinRspDTO;
import com.iwhalecloud.bote.doc.module.collaboration.workbook.dto.socket.RoomJoinDownDTO.RoomInfo;
import com.iwhalecloud.bote.doc.module.collaboration.workbook.dto.socket.RoomJoinDownDTO.RoomInfo.MembersDTO;
import com.iwhalecloud.bote.doc.module.collaboration.workbook.dto.socket.RoomJoinUpDTO;
import com.iwhalecloud.bote.doc.module.collaboration.workbook.dto.socket.RoomJoinUpDTO.JoinReqDTO;
import com.iwhalecloud.bote.doc.module.collaboration.workbook.dto.socket.RoomJoinUpDTO.JoinReqDTO.RoomsDTO;
import io.netty.channel.ChannelHandlerContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 在线表格加入房间的消息处理
 *
 * @author Aiqing
 * @since 2025/9/3
 */
@Component
@RequiredArgsConstructor
public class RoomJoinMessageHandler extends AbstractUniverMessageHandler<RoomJoinUpDTO> {

  private final ChannelSessionManager channelSessionManager;
  private final SocketMessageSendService messageSendService;
  private final WorkbookCollaborationCache workbookCollaborationCache;

  @Override
  public UniverCmdType getCmdType() {
    return UniverCmdType.JOIN_ROOM;
  }

  @Override
  protected void doHandle(ChannelHandlerContext ctx, RoomJoinUpDTO message) {
    SessionInfo sessionInfo = super.getSessionInfo(ctx);
    String sessionId = sessionInfo.getSessionId();
    Long userId = sessionInfo.getUserId();

    //
    JoinReqDTO joinReq = message.getJoinReq();
    List<RoomsDTO> rooms = joinReq.getRooms();

    List<String> roomIdList = rooms.stream().map(RoomsDTO::getRoomID).distinct().collect(Collectors.toList());

    workbookCollaborationCache.joinRoom(roomIdList, userId);
    roomIdList.forEach(roomId -> {
      channelSessionManager.subscribeBusiness(sessionId, SocketBizEnum.WORKSHEET.name(), roomId);
    });
    // 响应

    RoomJoinDownDTO downDTO = new RoomJoinDownDTO();
    downDTO.setRouteKey(message.getRouteKey());

    JoinRspDTO rspDTO = new JoinRspDTO();

    Map<String, RoomInfo> roomInfoMap = new HashMap<>();

    roomIdList.forEach(roomId -> {
      RoomInfo roomInfo = new RoomInfo();
      roomInfo.setRoomID(roomId);
      List<MembersDTO> membersDTOList = workbookCollaborationCache.roomMembers(roomId).stream()
        .map(item -> {
          return new MembersDTO(item, "");
        }).collect(Collectors.toList());
      roomInfo.setMembers(membersDTOList);
      roomInfoMap.put(roomId, roomInfo);
    });
    rspDTO.setRoomInfos(roomInfoMap);

    downDTO.setJoinRsp(rspDTO);
    messageSendService.sendToSession(sessionId, downDTO);
  }
}
