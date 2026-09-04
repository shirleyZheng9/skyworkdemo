package com.iwhalecloud.bote.doc.module.collaboration.socket.protocol;

import com.iwhalecloud.bote.doc.module.collaboration.socket.SocketProtocolEnum;
import com.iwhalecloud.bote.doc.module.collaboration.socket.config.WebSocketProperties;
import com.iwhalecloud.bote.doc.module.collaboration.socket.handler.AbstractMessageProtocol;
import com.iwhalecloud.bote.doc.module.collaboration.socket.message.ExtractedMessage;
import com.iwhalecloud.bote.doc.module.collaboration.socket.ws.univer.UniverCmdType;
import com.iwhalecloud.bote.doc.module.collaboration.socket.ws.univer.UniverCommand;
import com.iwhalecloud.bote.doc.module.collaboration.socket.ws.univer.model.CmdMessage;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * univer socket协议定义
 *
 * @author Aiqing
 * @since 2025/9/4
 */
@Component
@RequiredArgsConstructor
public class UniverMessageProtocol extends AbstractMessageProtocol {

  private final WebSocketProperties webSocketProperties;

  @Override
  public SocketProtocolEnum getProtocol() {
    return SocketProtocolEnum.UNIVER;
  }

  @Override
  public String getSocketPath() {
    return webSocketProperties.getUniverSocketPath();
  }

  @Override
  protected ExtractedMessage extractMessage(String message) {
    CmdMessage cmdMessage = JsonUtil.parseJson(message, CmdMessage.class);
    if (cmdMessage == null) {
      return null;
    }
    UniverCmdType univerCmdType = UniverCmdType.fromCode(cmdMessage.getCmd());
    return new ExtractedMessage(new UniverCommand(univerCmdType), message);
  }
}
