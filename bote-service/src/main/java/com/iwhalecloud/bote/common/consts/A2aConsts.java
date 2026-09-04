package com.iwhalecloud.bote.common.consts;

/**
 * A2A 相关常量
 *
 * @author bianjp
 * @since 2025-09-09
 */
public final class A2aConsts {
  private A2aConsts() {
  }

  /** A2A 开放接口鉴权请求头 */
  public static final String A2A_AUTH_HEADER = "X-A2A-Key";

  /** 默认 A2A 平台 ID (表示未关联 A2A 平台) */
  public static final Long DEFAULT_A2A_PLATFORM_ID = -1L;

}
