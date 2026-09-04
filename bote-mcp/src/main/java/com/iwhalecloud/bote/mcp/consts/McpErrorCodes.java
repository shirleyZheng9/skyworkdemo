package com.iwhalecloud.bote.mcp.consts;

/**
 * MCP 标准错误编码
 *
 * @author bianjp
 * @since 2025-05-22
 */
public final class McpErrorCodes {
  private McpErrorCodes() {
  }

  /** JSON 不合法 */
  public static final int PARSE_ERROR = -32700;
  /** 请求不合法 */
  public static final int INVALID_REQUEST = -32600;
  /** 方法不存在 */
  public static final int METHOD_NOT_FOUND = -32601;
  /** 方法参数不合法 */
  public static final int INVALID_PARAMS = -32602;
  /** 内部错误 */
  public static final int INTERNAL_ERROR = -32603;

  /** session 失效（用于 streamable 协议） */
  public static final String SESSION_EXPIRED = "SESSION_EXPIRED";

}
