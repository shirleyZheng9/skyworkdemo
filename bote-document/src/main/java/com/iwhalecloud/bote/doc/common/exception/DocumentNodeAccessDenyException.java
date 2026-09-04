package com.iwhalecloud.bote.doc.common.exception;

import com.iwhalecloud.bote.doc.common.constant.DocErrorCodeConsts;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.io.Serial;

/**
 * 文档访问拒绝异常
 *
 * @author Aiqing
 * @since 2025/8/18
 */
public class DocumentNodeAccessDenyException extends BssException {
  @Serial
  private static final long serialVersionUID = 1L;

  public DocumentNodeAccessDenyException() {
    super(DocErrorCodeConsts.NODE_ACCESS_DENY, "无权访问");
  }
}
