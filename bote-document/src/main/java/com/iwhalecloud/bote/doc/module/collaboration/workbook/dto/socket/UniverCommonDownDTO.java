package com.iwhalecloud.bote.doc.module.collaboration.workbook.dto.socket;

import com.iwhalecloud.bote.doc.module.collaboration.socket.ws.univer.model.CmdMessage;
import com.iwhalecloud.bss.litchi.util.sequence.UUIDUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * univer socket协议通用的下行数据结构
 *
 * @author Aiqing
 * @since 2025/9/3
 */
@Getter
@Setter
@ToString
public class UniverCommonDownDTO extends CmdMessage {

  private Integer code;
  private String reason;
  private String routeKey;
  private String traceID;
  private Object infoRsp;

  private Object collaMsg;

  public static UniverCommonDownDTO success(int cmd, String routeKey, Object collaMsg, Object infoRsp) {
    UniverCommonDownDTO univerCommonDownDTO = new UniverCommonDownDTO();
    univerCommonDownDTO.setCmd(cmd);
    univerCommonDownDTO.setCode(1);
    univerCommonDownDTO.setReason("success");
    univerCommonDownDTO.setRouteKey(routeKey);
    univerCommonDownDTO.setTraceID(UUIDUtils.randomFormatUuid());
    univerCommonDownDTO.setInfoRsp(infoRsp);
    univerCommonDownDTO.setCollaMsg(collaMsg);
    return univerCommonDownDTO;
  }

  public static UniverCommonDownDTO fail(int cmd, Integer code, String errorMessage, Object infoRsp) {
    UniverCommonDownDTO univerCommonDownDTO = new UniverCommonDownDTO();
    univerCommonDownDTO.setCmd(cmd);
    univerCommonDownDTO.setCode(code);
    univerCommonDownDTO.setReason(errorMessage);
    univerCommonDownDTO.setTraceID(UUIDUtils.randomFormatUuid());
    univerCommonDownDTO.setInfoRsp(infoRsp);
    return univerCommonDownDTO;
  }
}
