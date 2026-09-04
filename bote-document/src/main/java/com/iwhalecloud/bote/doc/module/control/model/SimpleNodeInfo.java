package com.iwhalecloud.bote.doc.module.control.model;

import com.iwhalecloud.bote.doc.consts.PermissionModeEnum;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * simple node info.
 */
@Getter
@Setter
@ToString
public class SimpleNodeInfo {

  /**
   * 文档节点ID
   */
  private String nodeId;
  /**
   * 父节点ID
   */
  private String parentId;
  /**
   * 文档节点类型
   */
  private String nodeType;

  /**
   * 权限模式
   */
  private Integer permissionMode;

  /**
   * 创建人
   */
  private Long creatorId;


  public boolean isExtend() {
    return Objects.equals(this.permissionMode, PermissionModeEnum.INHERIT.getCode());
  }
}
