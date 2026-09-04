package com.iwhalecloud.bote.doc.module.control.base.role;

import com.iwhalecloud.bote.doc.consts.PermissionTypeConstant.LibraryRoleEnum;

/**
 * 从文档库继承的可修订权限角色
 *
 * @author Aiqing
 * @since 2025/10/16
 */
public class LibraryInheritedCorrectionRole extends NodeCorrectionRole {

  public LibraryInheritedCorrectionRole() {
    super(true);
  }

  @Override
  public String getRoleTag() {
    return LibraryRoleEnum.CORRECTION.getCode();
  }
}
