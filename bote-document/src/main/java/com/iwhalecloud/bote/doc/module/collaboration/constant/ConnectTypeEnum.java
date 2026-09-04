package com.iwhalecloud.bote.doc.module.collaboration.constant;

import java.util.Arrays;

/**
 * socket的连接类型
 *
 * @author Aiqing
 * @since 2025/8/29
 */
public enum ConnectTypeEnum {

  /**
   * 外部链接
   */
  OUTER,
  /**
   * 内部链接
   */
  INTERNAL;


  public static ConnectTypeEnum fromCode(String connectType) {
    return Arrays.stream(values())
      .filter(connectTypeEnum -> connectTypeEnum.name().equalsIgnoreCase(connectType))
      .findFirst()
      .orElse(null);
  }
}
