package com.iwhalecloud.bote.loop.prompt.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * 角色枚举
 */
@Getter
public enum Role {
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

  Role(String value) {
    this.value = value;
  }

  public static Role fromValue(String value) {
    for (Role role : values()) {
      if (role.value.equals(value)) {
        return role;
      }
    }
    throw new IllegalArgumentException("Unknown Role: " + value);
  }
}
