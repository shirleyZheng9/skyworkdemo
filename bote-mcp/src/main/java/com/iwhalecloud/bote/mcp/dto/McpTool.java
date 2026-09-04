package com.iwhalecloud.bote.mcp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.llm.client.dto.schema.JsonSchemaNode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * MCP 工具
 *
 * @author bianjp
 * @since 2025-05-22
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class McpTool {
  /** 工具名称 */
  private String name;
  /** 工具描述 */
  private String description;
  /** 工具入参 */
  private JsonSchemaNode inputSchema;
}
