package com.iwhalecloud.bote.mcp.dto.message;

/**
 * JSON-RPC 消息
 *
 * @author bianjp
 * @since 2025-05-22
 */
public interface JsonRpcMessage {
  /**
   * 获取 JSON-RPC 协议版本
   *
   * @return JSON-RPC 协议版本
   */
  String getJsonrpc();
}
