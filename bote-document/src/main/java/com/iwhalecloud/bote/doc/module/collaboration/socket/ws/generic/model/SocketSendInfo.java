package com.iwhalecloud.bote.doc.module.collaboration.socket.ws.generic.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * socket通信协议封装
 */
@Getter
@Setter
@ToString
public class SocketSendInfo<T> {

  /**
   * 命令
   */
  private Integer cmd;

  /**
   * 推送消息体
   */
  private T data;

}
