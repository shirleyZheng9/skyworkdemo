package com.iwhalecloud.bote.doc.module.control.base.role;

import com.iwhalecloud.bote.doc.consts.PermissionTypeConstant.LibraryRoleEnum;

/**
 * 从文档库继承的下载权限角色
 *
 * <p>对应文档库的 DOWNLOAD 权限，包含查看和下载相关的节点权限。</p>
 * <p>用户可以查看内容、下载文档、复制内容，但不能编辑。</p>
 *
 * @author Aiqing
 * @since 2025-08-21
 */
public class LibraryInheritedDownloadRole extends NodeDownloaderRole {

  public LibraryInheritedDownloadRole() {
    super(true);
  }

  @Override
  public String getRoleTag() {
    return LibraryRoleEnum.DOWNLOAD.getCode();
  }

}
