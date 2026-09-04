package com.iwhalecloud.bote.service.publish.platform;

import com.iwhalecloud.bote.dto.publish.StandardMessage;

/**
 * 消息转换器基类 - 参考LangBot的MessageConverter设计
 *
 * @author system
 * @since 2025-01-09
 */
public abstract class MessageConverter {

  /**
   * 将标准消息转换为平台特定格式
   *
   * @param message 标准消息
   * @return 平台特定格式
   */
  public abstract Object standardToPlatform(StandardMessage message);
}
