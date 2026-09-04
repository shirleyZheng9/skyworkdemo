package com.iwhalecloud.bote.doc.module.collaboration.workbook.dto.socket;

import com.iwhalecloud.bote.doc.module.collaboration.socket.ws.univer.UniverCmdType;
import com.iwhalecloud.bote.doc.module.collaboration.socket.ws.univer.model.CmdMessage;
import com.iwhalecloud.bote.doc.module.collaboration.workbook.dto.UpdateCursorEventDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 光标修改后的上行数据
 *
 * @author Aiqing
 * @since 2025/9/3
 */
@Getter
@Setter
@ToString
public class CursorUpdateUpDTO extends CmdMessage {

  /**
   * routeKey
   */
  private String routeKey;
  /**
   * collaMsg
   */
  private UpdateCursorEventDTO collaMsg;

  public CursorUpdateUpDTO() {
    this.cmd = UniverCmdType.CURSOR_CHANGE.getCode();
  }
}
