package com.iwhalecloud.bote.loop.client.evaluation.domain.common;

/**
 * 角色DTO枚举
 * 对应Go: common.Role (int64类型)
 */
public enum RoleDTO {

  /**
   * 系统角色
   * 对应Go: Role_System = 1
   */
  SYSTEM(1, "System"),

  /**
   * 用户角色
   * 对应Go: Role_User = 2
   */
  USER(2, "User"),

  /**
   * 助手角色
   * 对应Go: Role_Assistant = 3
   */
  ASSISTANT(3, "Assistant"),

  /**
   * 工具角色
   * 对应Go: Role_Tool = 4
   */
  TOOL(4, "Tool");

  private final int value;
  private final String description;

  RoleDTO(int value, String description) {
    this.value = value;
    this.description = description;
  }

  public int getValue() {
    return value;
  }

  public String getDescription() {
    return description;
  }

  public static RoleDTO fromValue(int value) {
    for (RoleDTO role : values()) {
      if (role.value == value) {
        return role;
      }
    }
    throw new IllegalArgumentException("Invalid Role value: " + value);
  }

  public static RoleDTO fromString(String str) {
    for (RoleDTO role : values()) {
      if (role.description.equals(str)) {
        return role;
      }
    }
    throw new IllegalArgumentException("Invalid Role string: " + str);
  }
}
