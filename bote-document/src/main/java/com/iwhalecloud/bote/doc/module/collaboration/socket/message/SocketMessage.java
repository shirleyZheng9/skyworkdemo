package com.iwhalecloud.bote.doc.module.collaboration.socket.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 提供socket消息的统一封装
 *
 * @author Aiqing
 * @since 2025/9/4
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SocketMessage {

  /**
   * 数据
   */
  private String data;
}
