package com.iwhalecloud.bote.doc.common.exception;

import com.iwhalecloud.bote.doc.common.constant.DocErrorCodeConsts;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.io.Serial;

/**
 * 节点操作拒绝异常
 *
 * @author Aiqing
 * @since 2025/8/19
 */
public class NodeOperationDeniedException extends BssException {

  @Serial
  private static final long serialVersionUID = 1L;

  public NodeOperationDeniedException() {
    super(DocErrorCodeConsts.OPERATE_DENY, "无权限对文档节点进行操作");
  }

}
