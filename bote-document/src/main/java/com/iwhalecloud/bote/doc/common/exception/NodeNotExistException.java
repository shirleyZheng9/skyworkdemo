package com.iwhalecloud.bote.doc.common.exception;

import com.iwhalecloud.bote.doc.common.constant.DocErrorCodeConsts;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.io.Serial;

/**
 *
 * @author Aiqing
 * @since 2025/9/25
 */
public class NodeNotExistException extends BssException {

  @Serial
  private static final long serialVersionUID = 1L;

  public NodeNotExistException() {
    super(DocErrorCodeConsts.NODE_NOT_EXIST, "文档不存在");
  }
}
