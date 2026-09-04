package com.iwhalecloud.bote.doc.module.collaboration.workbook.dto.socket;

import com.iwhalecloud.bote.doc.module.collaboration.socket.ws.univer.UniverCmdType;
import com.iwhalecloud.bote.doc.module.collaboration.socket.ws.univer.model.CmdMessage;
import com.iwhalecloud.bss.litchi.util.sequence.UUIDUtils;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 在线表格协作加入后的下行数据
 *
 * @author Aiqing
 * @since 2025/9/3
 */
@Getter
@Setter
@ToString
public class RoomJoinDownDTO extends CmdMessage {

  /**
   * routeKey
   */
  private String routeKey;
  /**
   * code
   */
  private Integer code;
  /**
   * reason
   */
  private String reason;
  /**
   * traceID
   */
  private String traceID;
  /**
   * joinRsp
   */
  private JoinRspDTO joinRsp;


  public RoomJoinDownDTO() {
    this.cmd = UniverCmdType.JOIN_ROOM.getCode();
    this.code = 1;
    this.reason = "success";
    this.traceID = UUIDUtils.randomFormatUuid();
  }


  /**
   * JoinRspDTO
   */
  @NoArgsConstructor
  @Data
  public static class JoinRspDTO {
    /**
     * roomInfos
     */
    private Map<String, RoomInfo> roomInfos;
  }

  /**
   * 房间信息
   */
  @NoArgsConstructor
  @Data
  public static class RoomInfo {
    /**
     * roomID
     */
    private String roomID;
    /**
     * members
     */
    private List<MembersDTO> members;

    /**
     * MembersDTO
     */
    @AllArgsConstructor
    @Data
    public static class MembersDTO {
      /**
       * memberID
       */
      private String memberID;
      /**
       * name
       */
      private String name;
    }
  }
}
