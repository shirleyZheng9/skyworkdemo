package com.iwhalecloud.bote.doc.module.collaboration.workbook.dto.socket;

import com.iwhalecloud.bote.doc.module.collaboration.socket.ws.univer.UniverCmdType;
import com.iwhalecloud.bote.doc.module.collaboration.socket.ws.univer.model.CmdMessage;
import com.iwhalecloud.bss.litchi.util.sequence.UUIDUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * socket连接后的注册下行消息
 *
 * @author Aiqing
 * @since 2025/9/3
 */
@Getter
@Setter
@ToString
public class RegisterDownDTO extends CmdMessage {

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
   * infoRsp
   */
  private InfoRspDTO infoRsp;

  public RegisterDownDTO() {
    this.cmd = UniverCmdType.HEART_BEAT.getCode();
    this.code = 1;
    this.reason = "success";
    this.traceID = UUIDUtils.randomFormatUuid();
  }

  /**
   * InfoRspDTO
   */
  @NoArgsConstructor
  @Getter
  @Setter
  @ToString
  public static class InfoRspDTO {
    /**
     * memberID
     */
    private String memberID;
  }
}
