package com.iwhalecloud.bote.doc.module.collaboration.socket.ws.univer;

import com.iwhalecloud.bote.doc.module.collaboration.socket.SocketProtocolEnum;
import com.iwhalecloud.bote.doc.module.collaboration.socket.message.Command;

/**
 * univer交互命令
 *
 * @author Aiqing
 * @since 2025/9/3
 */
public class UniverCommand implements Command {


  private final UniverCmdType cmdType;

  public UniverCommand(UniverCmdType cmdType) {
    this.cmdType = cmdType;
  }

  @Override
  public String getCommand() {
    return String.valueOf(this.cmdType.getCode());
  }

  @Override
  public String getProtocol() {
    return SocketProtocolEnum.UNIVER.name();
  }

  @Override
  public String toString() {
    return "UniverCommand{" +
      "command=" + this.getCommand() +
      ", protocol=" + this.getProtocol() +
      '}';
  }
}
