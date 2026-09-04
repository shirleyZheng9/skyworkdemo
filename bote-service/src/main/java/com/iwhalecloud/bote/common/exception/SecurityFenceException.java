package com.iwhalecloud.bote.common.exception;

import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.io.Serial;

/**
 * 安全围栏校验异常
 *
 * <p>用于标识模型输入/输出命中安全围栏，便于业务层转为普通提示消息输出。</p>
 */
public class SecurityFenceException extends BssException {
  @Serial
  private static final long serialVersionUID = 1L;

  public SecurityFenceException(String message) {
    super(message);
  }
}
