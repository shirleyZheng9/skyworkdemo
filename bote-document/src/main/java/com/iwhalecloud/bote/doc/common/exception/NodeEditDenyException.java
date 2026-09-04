package com.iwhalecloud.bote.doc.common.exception;

import com.iwhalecloud.bote.doc.common.constant.DocErrorCodeConsts;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.io.Serial;

/**
 * 编辑被拒绝异常
 *
 * @author Aiqing
 * @since 2025/9/19
 */
public class NodeEditDenyException extends BssException {

  @Serial
  private static final long serialVersionUID = 1L;

  public NodeEditDenyException() {
    super(DocErrorCodeConsts.EDIT_DENY, "无权编辑该文档");
  }
}
