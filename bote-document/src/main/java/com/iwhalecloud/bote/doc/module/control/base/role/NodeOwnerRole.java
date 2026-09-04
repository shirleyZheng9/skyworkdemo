package com.iwhalecloud.bote.doc.module.control.base.role;

import com.iwhalecloud.bote.doc.consts.PermissionTypeConstant.DocRoleEnum;
import com.iwhalecloud.bote.doc.module.control.base.permission.NodePermission;

/**
 * 文档节点所有者角色
 *
 * @author Shawn Deng
 */
public class NodeOwnerRole extends NodeEditorRole {

  public NodeOwnerRole() {
    super();
    permissions.add(NodePermission.REMOVE_NODE);
    permissions.add(NodePermission.EDIT_NODE_ICON);
    permissions.add(NodePermission.REUPLOAD_NODE);
  }

  @Override
  public boolean canAssignable() {
    return false;
  }

  @Override
  public String getRoleTag() {
    return DocRoleEnum.DOC_OWNER.getCode();
  }
}
