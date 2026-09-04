package com.iwhalecloud.bote.llm.client.consts;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 消息角色
 *
 * @author bianjp
 * @since 2024-08-01
 */
@Getter
@RequiredArgsConstructor
public enum MessageRole {
  /** 系统 */
  @JsonProperty("system")
  SYSTEM("system"),
  /** 用户 */
  @JsonProperty("user")
  USER("user"),
  /** 助手 */
  @JsonProperty("assistant")
  ASSISTANT("assistant"),
  /** 工具 */
  @JsonProperty("tool")
  TOOL("tool"),
  /** 函数。已废弃，请使用 tool。一些国产模型仍需使用 */
  @JsonProperty("function")
  FUNCTION("function");

  /** 角色编码 */
  private final String code;
}
