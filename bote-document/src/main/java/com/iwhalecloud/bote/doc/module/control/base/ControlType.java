package com.iwhalecloud.bote.doc.module.control.base;

import lombok.Getter;

/**
 * 权限的控制主体
 */
@Getter
public enum ControlType {

  NODE(0);

  private final int val;

  ControlType(int val) {
    this.val = val;
  }
}
