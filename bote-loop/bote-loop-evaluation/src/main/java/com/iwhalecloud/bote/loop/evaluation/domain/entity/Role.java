package com.iwhalecloud.bote.loop.evaluation.domain.entity;

/**
 * 角色实体枚举
 * 对应Go: entity.Role (int64类型)
 */
public enum Role {

  /**
   * 未定义角色
   * 对应Go: RoleUndefined = 0
   */
  UNDEFINED(0, "Undefined"),

  /**
   * 系统角色
   * 对应Go: RoleSystem = 1
   */
  SYSTEM(1, "System"),

  /**
   * 用户角色
   * 对应Go: RoleUser = 2
   */
  USER(2, "User"),

  /**
   * 助手角色
   * 对应Go: RoleAssistant = 3
   */
  ASSISTANT(3, "Assistant"),

  /**
   * 工具角色
   * 对应Go: RoleTool = 4
   */
  TOOL(4, "Tool");

  private final int value;
  private final String description;

  Role(int value, String description) {
    this.value = value;
    this.description = description;
  }

  public int getValue() {
    return value;
  }

  public String getDescription() {
    return description;
  }

  public static Role fromValue(int value) {
    for (Role role : values()) {
      if (role.value == value) {
        return role;
      }
    }
    throw new IllegalArgumentException("Invalid Role value: " + value);
  }

  public static Role fromString(String str) {
    for (Role role : values()) {
      if (role.description.equals(str)) {
        return role;
      }
    }
    throw new IllegalArgumentException("Invalid Role string: " + str);
  }
}
