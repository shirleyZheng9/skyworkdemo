package com.iwhalecloud.bote.adapter.dify.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * Dify SSE 事件类型
 *
 * @author qian.sisheng
 * @since 2025-10-16
 */
@Getter
@RequiredArgsConstructor
public enum DifySseEventEnum {

  /** 消息事件 */
  MESSAGE("message"),
  /** 错误事件 */
  ERROR("error"),
  /** 消息结束事件 */
  MESSAGE_END("message_end");

  /** 事件编码 */
  private final String code;

  /**
   * 根据编码获取事件
   */
  public static DifySseEventEnum of(String code) {
    if (StringUtils.isEmpty(code)) {
      return null;
    }
    for (DifySseEventEnum value : values()) {
      if (code.equals(value.code)) {
        return value;
      }
    }
    return null;
  }
}
