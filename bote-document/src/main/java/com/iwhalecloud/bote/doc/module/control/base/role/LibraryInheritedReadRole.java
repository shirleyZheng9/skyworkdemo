package com.iwhalecloud.bote.doc.module.control.base.role;

import com.iwhalecloud.bote.doc.consts.PermissionTypeConstant.LibraryRoleEnum;

/**
 * 从文档库继承的只读权限角色
 *
 * <p>对应文档库的 READ 权限，只包含基本的查看权限。</p>
 * <p>用户只能查看内容和历史，不能进行任何修改或下载操作。</p>
 *
 * @author Aiqing
 * @since 2025-08-21
 */
public class LibraryInheritedReadRole extends NodeReaderRole {

  public LibraryInheritedReadRole() {
    super(true);
  }

  @Override
  public String getRoleTag() {
    return LibraryRoleEnum.READ.getCode();
  }

}
