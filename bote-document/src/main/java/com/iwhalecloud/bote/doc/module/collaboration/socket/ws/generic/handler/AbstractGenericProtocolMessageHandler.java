package com.iwhalecloud.bote.doc.module.collaboration.socket.ws.generic.handler;

import com.iwhalecloud.bote.doc.module.collaboration.socket.message.AbstractMessageHandler;
import com.iwhalecloud.bote.doc.module.collaboration.socket.message.Command;
import com.iwhalecloud.bote.doc.module.collaboration.socket.message.MessageRegistry.CommandAware;
import com.iwhalecloud.bote.doc.module.collaboration.socket.ws.generic.GenericCmdType;
import com.iwhalecloud.bote.doc.module.collaboration.socket.ws.generic.GenericCommand;

/**
 * 通用的socket协议消息处理
 *
 * @author Aiqing
 * @since 2025/9/3
 */
public abstract class AbstractGenericProtocolMessageHandler<T> extends AbstractMessageHandler<T>
  implements CommandAware {

  @Override
  public Command getCommand() {
    return new GenericCommand(this.getCmdType());
  }

  /**
   * 获取socket交互消息的cmdType
   *
   * @return cmdType
   */
  public abstract GenericCmdType getCmdType();
}
