package com.iwhalecloud.bote.doc.module.control.base.role;


import com.iwhalecloud.bote.doc.consts.PermissionTypeConstant.DocRoleEnum;
import com.iwhalecloud.bote.doc.module.control.base.permission.NodePermission;

/**
 * 管理角色
 */
public class NodeManagerRole extends NodeEditorRole {

  private final boolean isAdmin;

  public NodeManagerRole() {
    this(false);
  }

  public NodeManagerRole(boolean inherit) {
    this(inherit, false);
  }

  /**
   * constructor.
   *
   * @param inherit inherit
   * @param isAdmin is admin
   */
  public NodeManagerRole(boolean inherit, boolean isAdmin) {
    super(inherit);
    this.isAdmin = isAdmin;
    permissions.add(NodePermission.MANAGE_NODE);
    permissions.add(NodePermission.EDIT_NODE_ICON);
    permissions.add(NodePermission.REMOVE_NODE);
    permissions.add(NodePermission.SET_NODE_SHARE_ALLOW_EDIT);
    permissions.add(NodePermission.ASSIGN_NODE_ROLE);
    permissions.add(NodePermission.REUPLOAD_NODE);
  }

  @Override
  public boolean isAdmin() {
    return this.isAdmin;
  }

  @Override
  public String getRoleTag() {
    return DocRoleEnum.DOC_MANAGE.getCode();
  }
}
