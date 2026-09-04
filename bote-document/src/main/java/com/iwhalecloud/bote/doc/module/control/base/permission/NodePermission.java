package com.iwhalecloud.bote.doc.module.control.base.permission;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * node permission definition.
 */
@Getter
@AllArgsConstructor
public enum NodePermission implements PermissionDefinition {

  MANAGE_NODE("manageable", 0, 1L),

  EDIT_NODE("editable", 0, 1L << 1),

  READ_NODE("readable", 0, 1L << 2),

  CREATE_NODE("childCreatable", 0, 1L << 3),

  RENAME_NODE("renameAble", 0, 1L << 4),

  EDIT_NODE_ICON("iconEditable", 0, 1L << 5),

  MOVE_NODE("movable", 0, 1L << 6),

  COPY_NODE("copyable", 0, 1L << 7),

  EXPORT_NODE("exportable", 0, 1L << 8),

  REMOVE_NODE("removable", 0, 1L << 9),

  COMMENT_NODE("commentEditable", 0, 1L << 10),

  VIEW_HISTORY("historyReadable", 0, 1L << 11),

  /**
   * 分享文档
   */
  SHARE_NODE("sharable", 0, 1L << 12),


  /**
   * 设置是否分享节点是否能够编辑
   */
  SET_NODE_SHARE_ALLOW_EDIT("allowEditConfigurable", 0, 1L << 13),

  /**
   * 设置权限
   */
  ASSIGN_NODE_ROLE("nodeAssignable", 0, 1L << 14),

  /**
   * 修订文档内容
   */
  CONTENT_CORRECTION("contentCorrection", 0, 1L << 15),

  /**
   * 审核文档修订建议
   */
  APPROVAL_CORRECTION("approvalCorrection", 0, 1L << 16),

  /**
   * 重新上传的权限
   */
  REUPLOAD_NODE("reuploadable", 0, 1L << 17);

  /**
   * unique code.
   */
  private final String code;

  /**
   * permission group.
   */
  private final int group;

  /**
   * unique memory value.
   */
  private final long value;

}
