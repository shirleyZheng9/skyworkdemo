package com.iwhalecloud.bote.doc.module.collaboration.socket.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 从消息协议中解构的消息，必须包含消息的交互命令
 *
 * @author Aiqing
 * @since 2025/9/3
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
public class ExtractedMessage {

  /**
   * 交互命令
   */
  private Command command;

  /**
   * 实际业务需要的数据
   */
  private Object data;
}
