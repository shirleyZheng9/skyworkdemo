package com.iwhalecloud.bote.doc.module.collaboration.workbook.dto.socket;

import com.iwhalecloud.bote.doc.module.collaboration.socket.ws.univer.UniverCmdType;
import com.iwhalecloud.bote.doc.module.collaboration.socket.ws.univer.model.CmdMessage;
import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 表格协作加入上行数据
 *
 * @author Aiqing
 * @since 2025/9/3
 */
@Getter
@Setter
@ToString
public class RoomJoinUpDTO extends CmdMessage {

  /**
   * routeKey
   */
  private String routeKey;
  /**
   * joinReq
   */
  private JoinReqDTO joinReq;

  public RoomJoinUpDTO() {
    this.cmd = UniverCmdType.JOIN_ROOM.getCode();
  }

  /**
   * JoinReqDTO
   */
  @Data
  public static class JoinReqDTO {
    /**
     * rooms
     */
    private List<RoomsDTO> rooms;

    /**
     * RoomsDTO
     */
    @NoArgsConstructor
    @Data
    public static class RoomsDTO {
      /**
       * roomID
       */
      private String roomID;
    }
  }
}
