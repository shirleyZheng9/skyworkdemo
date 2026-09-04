package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.BaseSystemParameter;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.MDC;
import org.springframework.lang.Nullable;

/**
 * 登录信息工具类
 *
 * @author chen.linfa
 * @since 2024-07-31
 */
public final class SessionUtil {
  private SessionUtil() {
  }

  /** session ID 线程变量 */
  private static final ThreadLocal<String> sessionIdThreadLocal = new ThreadLocal<>();
  /** 登录信息线程变量 */
  private static final ThreadLocal<LoginInfo> loginInfoThreadLocal = new ThreadLocal<>();

  /**
   * 获取 session ID
   */
  public static String getSessionId() {
    return sessionIdThreadLocal.get();
  }

  /**
   * 设置 session ID 到线程变量
   */
  public static void setSessionId(@Nullable String sessionId) {
    if (sessionId != null) {
      sessionIdThreadLocal.set(sessionId);
    }
    else {
      sessionIdThreadLocal.remove();
    }
  }

  /**
   * 设置登录信息到线程变量
   */
  public static void setLoginInfo(@Nullable LoginInfo loginInfo) {
    if (loginInfo != null) {
      loginInfoThreadLocal.set(loginInfo);
      if (loginInfo.getUserId() != null) {
        MDC.put("userId", loginInfo.getUserId().toString());
      }
    }
    else {
      loginInfoThreadLocal.remove();
    }
  }

  /**
   * 获取可选的登录信息
   */
  @Nullable
  public static LoginInfo getOptionalLoginInfo() {
    return loginInfoThreadLocal.get();
  }

  /**
   * 获取登录信息，不存在时抛异常
   */
  public static LoginInfo getLoginInfo() {
    LoginInfo loginInfo = loginInfoThreadLocal.get();
    if (loginInfo == null) {
      throw BaseErrorConstant.NO_LOGIN.toException();
    }
    return loginInfo;
  }

  /**
   * 获取可选的用户 ID
   *
   * @return 用户ID ID。不存在时返回默认值
   */
  public static Long getOptionalUserId(Long defaultUserId) {
    LoginInfo loginInfo = loginInfoThreadLocal.get();
    if (loginInfo == null) {
      return defaultUserId;
    }
    return loginInfo.getUserId();
  }

  /**
   * 获取可选的用户 ID
   */
  @Nullable
  public static Long getOptionalUserId() {
    LoginInfo loginInfo = loginInfoThreadLocal.get();
    return loginInfo == null ? null : loginInfo.getUserId();
  }

  /**
   * 判断是否超级管理员
   */
  public static boolean isSuperAdmin(Long userId) {
    List<Long> userIds = CollectionUtils.emptyIfNull(Arrays.asList(BaseSystemParameter.SUPER_ADMIN.getValueFromDb().split(","))).stream()
      .map(p -> Arrays.asList(p.split("\\|")).getFirst()).map(Long::valueOf).toList();
    return userIds.contains(userId);
  }

  /**
   * 清除线程变量
   */
  public static void clearThreadLocal() {
    sessionIdThreadLocal.remove();
    loginInfoThreadLocal.remove();
    MDC.remove("userId");
  }
}
