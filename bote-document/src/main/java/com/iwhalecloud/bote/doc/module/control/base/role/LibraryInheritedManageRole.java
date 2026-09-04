package com.iwhalecloud.bote.doc.module.control.base.role;

import com.iwhalecloud.bote.doc.consts.PermissionTypeConstant.LibraryRoleEnum;

/**
 * 从文档库继承的管理权限角色
 *
 * <p>对应文档库的 MANAGE 权限，包含所有节点权限。</p>
 * <p>用户拥有完全的管理权限，包括增删改查、权限管理、分享等。</p>
 *
 * @author Aiqing
 * @since 2025-08-21
 */
public class LibraryInheritedManageRole extends NodeManagerRole {
  public LibraryInheritedManageRole() {
    super(true);
  }

  @Override
  public boolean isAdmin() {
    return true;
  }

  @Override
  public String getRoleTag() {
    return LibraryRoleEnum.MANAGE.getCode();
  }
}
