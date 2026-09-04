package com.iwhalecloud.bote.common.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.lang.Nullable;

/**
 * 异常工具类
 *
 * @author bianjp
 * @since 2024-12-12
 */
public final class ExpUtil {
  private ExpUtil() {
  }

  /**
   * 检查异常的 cause 链中是否包含指定类型的 cause
   */
  public static boolean hasCause(Throwable throwable, Class<? extends Throwable> causeClass) {
    if (causeClass.isInstance(throwable)) {
      return true;
    }
    Throwable previous = throwable;
    Throwable cause = previous.getCause();
    while (cause != null && cause != previous) { //NOPMD - suppressed CompareObjectsWithEquals - 用 != 比较对象是正确的
      if (causeClass.isInstance(cause)) {
        return true;
      }
      previous = cause;
      cause = cause.getCause();
    }
    return false;
  }

  /**
   * 获取异常信息
   *
   * <p>确保异常信息不为空</p>
   */
  public static String getMsg(@Nullable Throwable throwable) {
    if (throwable == null) {
      return "";
    }
    String msg = throwable.getMessage();
    if (StringUtils.isEmpty(msg)) {
      msg = ExceptionUtils.getRootCauseMessage(throwable);
    }
    return msg;
  }

}
