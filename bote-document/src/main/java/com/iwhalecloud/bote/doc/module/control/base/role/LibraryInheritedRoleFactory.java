package com.iwhalecloud.bote.doc.module.control.base.role;

import com.iwhalecloud.bote.doc.consts.PermissionTypeConstant.LibraryRoleEnum;

/**
 * 文档库继承权限角色工厂
 *
 * <p>根据文档库权限类型创建对应的继承权限角色实例。</p>
 * <p>支持的权限类型映射：</p>
 * <ul>
 *   <li>MANAGE → LibraryInheritedManageRole</li>
 *   <li>EDIT → LibraryInheritedEditRole</li>
 *   <li>DOWNLOAD → LibraryInheritedDownloadRole</li>
 *   <li>READ → LibraryInheritedReadRole</li>
 * </ul>
 *
 * @author Aiqing
 * @since 2025-08-21
 */
public final class LibraryInheritedRoleFactory {

  /**
   * 私有构造函数，防止实例化
   */
  private LibraryInheritedRoleFactory() {

  }

  /**
   * 根据文档库权限类型创建对应的继承权限角色
   *
   * @param libraryRoleCode 文档库权限代码
   * @return 对应的继承权限角色实例
   * @throws IllegalArgumentException 当权限代码不支持时抛出
   */
  public static ControlRole createInheritedRole(String libraryRoleCode) {
    if (libraryRoleCode == null) {
      // 默认返回只读权限
      return new LibraryInheritedReadRole();
    }

    LibraryRoleEnum libraryRoleEnum = LibraryRoleEnum.valueOf(libraryRoleCode);
    switch (libraryRoleEnum) {
      case OWNER:
      case MANAGE:
        return new LibraryInheritedManageRole();
      case EDIT:
        return new LibraryInheritedEditRole();
      case CORRECTION:
        return new LibraryInheritedCorrectionRole();
      case DOWNLOAD:
        return new LibraryInheritedDownloadRole();
      case READ:
        return new LibraryInheritedReadRole();
      default:
        // 未知权限类型，返回只读权限作为安全默认值
        return new NodeAnonymousRole();
    }
  }

  /**
   * 获取默认的继承权限角色 无权限
   *
   * @return 默认的只读权限角色
   */
  public static ControlRole getDefaultInheritedRole() {
    return new NodeAnonymousRole();
  }
}
