package com.iwhalecloud.bote.doc.module.control.base.role;

import com.iwhalecloud.bote.doc.consts.PermissionTypeConstant.DocRoleEnum;
import com.iwhalecloud.bote.doc.module.control.base.permission.NodePermission;

/**
 * 编辑角色
 */
public class NodeEditorRole extends NodeReaderRole {

  public NodeEditorRole() {
    this(false);
  }

  /**
   * constructor.
   *
   * @param inherit inherit from parent
   */
  public NodeEditorRole(boolean inherit) {
    super(inherit);
    permissions.add(NodePermission.EDIT_NODE);
    permissions.add(NodePermission.CREATE_NODE);
    permissions.add(NodePermission.RENAME_NODE);
    permissions.add(NodePermission.SHARE_NODE);
    permissions.add(NodePermission.EXPORT_NODE);
    permissions.add(NodePermission.COPY_NODE);
    permissions.add(NodePermission.COMMENT_NODE);
    permissions.add(NodePermission.VIEW_HISTORY);
    permissions.add(NodePermission.MOVE_NODE);
    permissions.add(NodePermission.CONTENT_CORRECTION);
    permissions.add(NodePermission.APPROVAL_CORRECTION);
    permissions.add(NodePermission.REUPLOAD_NODE);
  }

  @Override
  public String getRoleTag() {
    return DocRoleEnum.DOC_EDIT.getCode();
  }
}
