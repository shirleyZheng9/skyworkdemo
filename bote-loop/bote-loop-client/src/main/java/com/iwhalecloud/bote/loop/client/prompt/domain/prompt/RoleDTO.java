package com.iwhalecloud.bote.loop.client.prompt.domain.prompt;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 角色类型枚举
 * 迁移对应关系: Thrift Role
 */
public enum RoleDTO {
  @JsonProperty("system")
  SYSTEM("system"),
  @JsonProperty("user")
  USER("user"),
  @JsonProperty("assistant")
  ASSISTANT("assistant"),
  @JsonProperty("tool")
  TOOL("tool"),
  @JsonProperty("placeholder")
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
