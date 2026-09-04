package com.iwhalecloud.bote.doc.module.control.base.role;

import com.iwhalecloud.bote.doc.consts.PermissionTypeConstant.DocRoleEnum;
import com.iwhalecloud.bote.doc.module.control.base.permission.NodePermission;

/**
 * 文档只读角色
 *
 * @author Shawn Deng
 */
public class NodeReaderRole extends NodeRole {

  public NodeReaderRole() {
    this(false);
  }

  /**
   * constructor.
   *
   * @param inherit inherit from parent
   */
  public NodeReaderRole(boolean inherit) {
    super(inherit);
    permissions.add(NodePermission.READ_NODE);
  }

  @Override
  public String getRoleTag() {
    return DocRoleEnum.DOC_READ.getCode();
  }
}
