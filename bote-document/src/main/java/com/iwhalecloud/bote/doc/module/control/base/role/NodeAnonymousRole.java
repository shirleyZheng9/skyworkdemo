package com.iwhalecloud.bote.doc.module.control.base.role;

import com.iwhalecloud.bote.doc.consts.PermissionTypeConstant.DocRoleEnum;

/**
 * 匿名角色
 */
public class NodeAnonymousRole extends NodeRole {

  @Override
  public boolean canAssignable() {
    return false;
  }

  @Override
  public String getRoleTag() {
    return DocRoleEnum.ANONYMOUS.getCode();
  }
}
