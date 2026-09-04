package com.iwhalecloud.bote.doc.module.control.base.role;

import com.iwhalecloud.bote.doc.consts.PermissionTypeConstant.LibraryRoleEnum;

/**
 * 从文档库继承的编辑权限角色
 *
 * <p>对应文档库的 EDIT 权限，包含编辑相关的节点权限。</p>
 * <p>用户可以编辑内容、协作编辑，但不能管理权限和删除节点。</p>
 *
 * @author Aiqing
 * @since 2025-08-21
 */
public class LibraryInheritedEditRole extends NodeEditorRole {

  public LibraryInheritedEditRole() {
    super(true);
  }

  @Override
  public String getRoleTag() {
    return LibraryRoleEnum.EDIT.getCode();
  }
}
