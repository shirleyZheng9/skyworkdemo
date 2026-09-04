package com.iwhalecloud.bote.mcp.dto.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.mcp.consts.McpConsts;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * JSON-RPC 响应
 *
 * @author bianjp
 * @since 2025-05-22
 */
@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class JsonRpcResponse implements JsonRpcMessage {
  /** JSON-RPC 协议版本 */
  private String jsonrpc;
  /** 请求 ID */
  private Object id;
  /** 结果 */
  private Object result;
  /** 错误 */
  private JsonRpcError error;

  public JsonRpcResponse() {
  }

  public JsonRpcResponse(Object id, Object result, JsonRpcError error) {
    this.jsonrpc = McpConsts.JSONRPC_VERSION;
    this.id = id;
    this.result = result;
    this.error = error;
  }

  /**
   * 构造失败响应
   */
  public JsonRpcResponse(Object id, JsonRpcError error) {
    this(id, null, error);
  }

  /**
   * 构造成功响应
   */
  public JsonRpcResponse(Object id, Object result) {
    this(id, result, null);
  }

  @Override
  public String toString() {
    return JsonUtil.toJsonString(this);
  }
}
