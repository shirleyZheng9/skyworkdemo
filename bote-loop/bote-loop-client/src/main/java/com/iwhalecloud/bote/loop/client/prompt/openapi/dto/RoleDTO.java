package com.iwhalecloud.bote.loop.client.prompt.openapi.dto;

/**
 * 角色类型枚举
 * 对应Thrift: Role
 */
public enum RoleDTO {
  SYSTEM("system"),
  USER("user"),
  ASSISTANT("assistant"),
  TOOL("tool"),
  PLACEHOLDER("placeholder");

  private final String value;

  RoleDTO(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static RoleDTO fromValue(String value) {
    for (RoleDTO role : values()) {
      if (role.value.equals(value)) {
        return role;
      }
    }
    throw new IllegalArgumentException("Unknown Role: " + value);
  }
}
