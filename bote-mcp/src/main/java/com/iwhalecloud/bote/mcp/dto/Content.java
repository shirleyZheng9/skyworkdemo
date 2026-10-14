package com.iwhalecloud.bote.mcp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = As.EXISTING_PROPERTY, visible = true, defaultImpl = UnknownContent.class)
@JsonSubTypes({
  @Type(name = McpConsts.CONTENT_TYPE_TEXT, value = TextContent.class),
  @Type(name = McpConsts.CONTENT_TYPE_IMAGE, value = ImageContent.class),
  @Type(name = McpConsts.CONTENT_TYPE_AUDIO, value = AudioContent.class),
  @Type(name = McpConsts.CONTENT_TYPE_RESOURCE, value = EmbeddedResourceContent.class),
  @Type(name = McpConsts.CONTENT_TYPE_RESOURCE_LINK, value = ResourceLinkContent.class)
})
public abstract class Content {
  /** 类型 */
  protected String type;

  protected Content(String type) {
    this.type = type;
  }

  /**
   * 反序列化时由 Jackson 回填类型字段（EXISTING_PROPERTY 模式下类型属性会回放给 bean）
   */
  @JsonProperty
  protected void setType(String type) {
    this.type = type;
  }
}
