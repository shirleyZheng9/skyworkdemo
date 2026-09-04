package com.iwhalecloud.bote.doc.module.control.base.role;

import com.iwhalecloud.bote.doc.module.control.base.permission.PermissionDefinition;
import java.util.Map;
import java.util.Set;

/**
 * 角色属性接口
 *
 * @author Shawn Deng
 */
public interface ControlRole extends RoleComparable<ControlRole> {

  /**
   * 是否可以分配给组织单元
   *
   * @return false | true
   */
  default boolean canAssignable() {
    return true;
  }

  /**
   * 是否是管理员
   *
   * @return false | true
   */
  default boolean isAdmin() {
    return false;
  }

  /**
   * 角色是否可继承
   *
   * @return false | true
   */
  boolean isInherit();

  /**
   * 角色标签
   *
   * @return 角色标签名称
   */
  String getRoleTag();

  /**
   * 角色的权限集合
   *
   * @return 权限集合
   */
  Set<PermissionDefinition> getPermissions();

  /**
   * 权限集合占位符分组
   *
   * @return 分组占位符
   */
  Map<Integer, Long> getGroupPermissionBit();

  /**
   * 角色权限的位运算结果
   *
   * @return long值
   */
  long getBits();

  /**
   * 是否包含某个权限
   *
   * @param permission 权限
   * @return false | true
   */
  boolean hasPermission(PermissionDefinition permission);

  /**
   * 将权限集合分配给类实例
   *
   * @param beanClass 实例类
   * @return 新的实例对象
   */
  <T> T permissionToBean(Class<T> beanClass);

}
