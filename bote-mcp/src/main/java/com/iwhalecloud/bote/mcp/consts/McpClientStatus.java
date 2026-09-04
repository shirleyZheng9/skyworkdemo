package com.iwhalecloud.bote.mcp.consts;

/**
 * MCP 客户端状态
 *
 * @author bianjp
 * @since 2025-05-27
 */
public enum McpClientStatus {
  /** 初始状态（未初始化） */
  UNSET,
  /** 正在初始化 */
  INITIALIZING,
  /** 初始化完成 */
  INITIALIZED,
  /** 初始化失败 */
  INITIALIZE_FAILED,
  /** 已关闭 */
  CLOSED
}
