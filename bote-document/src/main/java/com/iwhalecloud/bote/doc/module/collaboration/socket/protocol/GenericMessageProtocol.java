package com.iwhalecloud.bote.doc.module.collaboration.socket.protocol;

import com.iwhalecloud.bote.doc.module.collaboration.socket.SocketProtocolEnum;
import com.iwhalecloud.bote.doc.module.collaboration.socket.config.WebSocketProperties;
import com.iwhalecloud.bote.doc.module.collaboration.socket.handler.AbstractMessageProtocol;
import com.iwhalecloud.bote.doc.module.collaboration.socket.message.ExtractedMessage;
import com.iwhalecloud.bote.doc.module.collaboration.socket.ws.generic.GenericCmdType;
import com.iwhalecloud.bote.doc.module.collaboration.socket.ws.generic.GenericCommand;
import com.iwhalecloud.bote.doc.module.collaboration.socket.ws.generic.model.SocketSendInfo;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 通用socket 协议定义
 *
 * @author Aiqing
 * @since 2025/9/4
 */
@Component
@RequiredArgsConstructor
public class GenericMessageProtocol extends AbstractMessageProtocol {

  private static final Logger logger = LoggerFactory.getLogger(GenericMessageProtocol.class);

  private final WebSocketProperties webSocketProperties;

  @Override
  public SocketProtocolEnum getProtocol() {
    return SocketProtocolEnum.GENERIC;
  }

  @Override
  public String getSocketPath() {
    return webSocketProperties.getGenericSocketPath();
  }

  @Override
  protected ExtractedMessage extractMessage(String message) {
    SocketSendInfo<?> socketSendInfo = JsonUtil.parseJson(message, SocketSendInfo.class);
    if (socketSendInfo == null) {
      return null;
    }
    Integer cmd = socketSendInfo.getCmd();
    GenericCmdType genericCmdType = GenericCmdType.fromCode(cmd);
    if (genericCmdType == null) {
      logger.warn("不支持的cmd :{}", cmd);
      return null;
    }
    return new ExtractedMessage(new GenericCommand(genericCmdType), socketSendInfo.getData());
  }
}
