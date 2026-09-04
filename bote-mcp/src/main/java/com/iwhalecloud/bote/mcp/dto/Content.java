package com.iwhalecloud.bote.mcp.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.iwhalecloud.bote.mcp.consts.McpConsts;
import lombok.Getter;

/**
 * MCP 内容
 *
 * @author bianjp
 * @since 2025-05-22
 */
@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = As.EXISTING_PROPERTY)
@JsonSubTypes({
  @Type(name = McpConsts.CONTENT_TYPE_TEXT, value = TextContent.class),
  @Type(name = McpConsts.CONTENT_TYPE_IMAGE, value = ImageContent.class)
})
public abstract class Content {
  /** 类型 */
  protected final String type;

  protected Content(String type) {
    this.type = type;
  }
}
