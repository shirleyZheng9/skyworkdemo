package com.iwhalecloud.bote.common.consts;

import lombok.Getter;

/**
 * 编辑锁枚举
 *
 * @author qian.sisheng
 * @since 2025-05-09
 */
@Getter
public enum EditLockEnum {
  EDIT_LOCK_SCENE("scene", "智能体"),
  EDIT_LOCK_FLOW("page", "页面"),
  EDIT_LOCK_SCENE_INTENT("flow", "工作流");

  private final String type;

  private final String typeName;

  EditLockEnum(String type, String typeName) {
    this.type = type;
    this.typeName = typeName;
  }

  public static String getTypeName(String type) {
    for (EditLockEnum value : values()) {
      if (value.type.equals(type)) {
        return value.typeName;
      }
    }
    return null;
  }
}
