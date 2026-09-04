package com.iwhalecloud.bote.doc.module.control.base.role;

import com.iwhalecloud.bote.doc.consts.PermissionTypeConstant.DocRoleEnum;
import com.iwhalecloud.bote.doc.module.control.base.permission.NodePermission;

/**
 * 可修订角色
 *
 * @author Aiqing
 * @since 2025/10/16
 */
public class NodeCorrectionRole extends NodeDownloaderRole {

  public NodeCorrectionRole() {
    this(false);
  }

  public NodeCorrectionRole(boolean inherit) {
    super(inherit);

    permissions.add(NodePermission.CONTENT_CORRECTION);
  }

  @Override
  public String getRoleTag() {
    return DocRoleEnum.DOC_CORRECTION.getCode();
  }
}
