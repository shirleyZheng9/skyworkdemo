package com.iwhalecloud.bote.doc.module.control.base.role;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 基础角色
 */
@Getter
@Setter
@ToString
public class NodeRole extends AbstractControlRole {

  private boolean ghostNode;

  public NodeRole() {
    this(false);
  }

  public NodeRole(boolean inherit) {
    super(inherit);
  }

  @Override
  public String getRoleTag() {
    return null;
  }
}
