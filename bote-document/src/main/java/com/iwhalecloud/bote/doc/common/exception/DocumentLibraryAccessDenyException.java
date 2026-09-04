package com.iwhalecloud.bote.doc.common.exception;

import com.iwhalecloud.bote.doc.common.constant.DocErrorCodeConsts;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.io.Serial;

/**
 * 文档库访问拒绝异常
 *
 * @author Aiqing
 * @since 2025/9/11
 */
public class DocumentLibraryAccessDenyException extends BssException {

  @Serial
  private static final long serialVersionUID = 1L;

  public DocumentLibraryAccessDenyException() {
    super(DocErrorCodeConsts.LIBRARY_ACCESS_DENY, "无法访问此文档库");
  }
}
