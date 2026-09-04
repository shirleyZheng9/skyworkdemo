package com.iwhalecloud.bote.mcp.consts;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * MCP 听众角色
 *
 * @author bianjp
 * @since 2025-05-22
 */
public enum McpAudienceRole {
  @JsonProperty("user")
  USER,
  @JsonProperty("assistant")
  ASSISTANT
}
