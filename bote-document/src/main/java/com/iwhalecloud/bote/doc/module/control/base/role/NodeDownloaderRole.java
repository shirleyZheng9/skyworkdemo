package com.iwhalecloud.bote.doc.module.control.base.role;

import com.iwhalecloud.bote.doc.consts.PermissionTypeConstant.DocRoleEnum;
import com.iwhalecloud.bote.doc.module.control.base.permission.NodePermission;

/**
 * <p>
 * Updater role of node.
 * </p>
 *
 * @author liuzijing
 */
public class NodeDownloaderRole extends NodeReaderRole {

  public NodeDownloaderRole() {
    this(false);
  }

  /**
   * constructor.
   *
   * @param inherit inherit from parent
   */
  public NodeDownloaderRole(boolean inherit) {
    super(inherit);

    permissions.add(NodePermission.EXPORT_NODE);
  }

  @Override
  public String getRoleTag() {
    return DocRoleEnum.DOWNLOAD.getCode();
  }
}
