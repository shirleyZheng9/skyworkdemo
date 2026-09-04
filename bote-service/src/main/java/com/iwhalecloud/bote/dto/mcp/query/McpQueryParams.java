package com.iwhalecloud.bote.dto.mcp.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * MCP 入参
 *
 * @author chen.linfa
 * @since 2025-08-05
 */
@Getter
@Setter
@ToString
@Schema(description = "MCP 入参")
public class McpQueryParams {
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "服务 ID")
  private Long serverId;
  @Schema(description = "参数返回方式(none: 不返回; spec: 返回 parameters; schema: 返回 inputSchema)")
  private String params;
}
