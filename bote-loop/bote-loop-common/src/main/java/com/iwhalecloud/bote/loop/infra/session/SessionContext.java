package com.iwhalecloud.bote.loop.infra.session;

import com.iwhalecloud.bote.common.util.SessionUtil;

public final class SessionContext {

  private SessionContext() {
    // 工具类，禁止实例化
  }

  public static String getCurrentUserId() {
    return SessionUtil.getOptionalUserId(999999999L).toString();
  }

  public static Integer getAppId() {
    return 999999999;
  }

}
