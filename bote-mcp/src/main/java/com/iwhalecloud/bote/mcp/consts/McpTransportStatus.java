package com.iwhalecloud.bote.mcp.consts;

/**
 * MCP 传输协议的连接状态
 *
 * @author bianjp
 * @since 2025-05-27
 */
public enum McpTransportStatus {
  /** 初始状态（未连接） */
  UNSET,
  /** 连接中 */
  CONNECTING,
  /** 已连接 */
  CONNECTED,
  /** 连接失败 */
  CONNECT_FAILED,
  /** 已关闭 */
  CLOSED
}
