package com.iwhalecloud.bote.doc.module.collaboration.socket.ws.generic;

import com.iwhalecloud.bote.doc.module.collaboration.socket.SocketProtocolEnum;
import com.iwhalecloud.bote.doc.module.collaboration.socket.message.Command;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Aiqing
 * @since 2025/9/3
 */
@Getter
@Setter
@AllArgsConstructor
public class GenericCommand implements Command {

  private GenericCmdType genericCmdType;

  @Override
  public String getCommand() {
    return String.valueOf(this.genericCmdType.getCode());
  }

  @Override
  public String getProtocol() {
    return SocketProtocolEnum.GENERIC.name();
  }

  @Override
  public String toString() {
    return "GenericCommand{" +
      "command=" + this.getCommand() +
      ", protocol=" + this.getProtocol() +
      '}';
  }
}
