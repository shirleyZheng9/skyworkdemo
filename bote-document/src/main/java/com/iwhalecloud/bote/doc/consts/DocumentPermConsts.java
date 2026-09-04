package com.iwhalecloud.bote.doc.consts;

import com.iwhalecloud.bote.doc.common.exception.DocumentNodeAccessDenyException;
import com.iwhalecloud.bote.doc.common.exception.NodeEditDenyException;
import com.iwhalecloud.bote.doc.common.exception.NodeOperationDeniedException;
import com.iwhalecloud.bote.doc.consts.PermissionTypeConstant.LibraryRoleEnum;
import java.util.function.Consumer;

/**
 *
 * 文档相关的常量
 *
 * @author Aiqing
 * @since 2025/8/25
 */
public final class DocumentPermConsts {

  /**
   * 权限检查失败时的异常处理回调
   */
  public static final Consumer<Boolean> ACCESS_DENIED_CALLBACK = status -> {
    if (!status) {
      throw new DocumentNodeAccessDenyException();
    }
  };

  /**
   * 无权限操作回调
   */
  public static final Consumer<Boolean> OPERATE_DENIED_CALLBACK = status -> {
    if (!status) {
      throw new NodeOperationDeniedException();
    }
  };

  /**
   * 无权限编辑回调
   */
  public static final Consumer<Boolean> EDIT_DENIED_CALLBACK = status -> {
    if (!status) {
      throw new NodeEditDenyException();
    }
  };

  /**
   * 公共文档库的默认权限
   */
  public static final LibraryRoleEnum PUBLIC_LIBRARY_DEFAULT_ROLE = LibraryRoleEnum.DOWNLOAD;

  private DocumentPermConsts() {
    throw new IllegalStateException();
  }

}
