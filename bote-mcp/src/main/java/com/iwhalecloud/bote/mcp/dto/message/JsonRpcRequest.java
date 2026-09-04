package com.iwhalecloud.bote.mcp.dto.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.mcp.consts.McpConsts;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * JSON-RPC 请求
 *
 * @author bianjp
 * @since 2025-05-22
 */
@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class JsonRpcRequest implements JsonRpcMessage {
  /** JSON-RPC 协议版本 */
  private String jsonrpc;
  /** 方法名称 */
  private String method;
  /** 请求 ID */
  private Object id;
  /** 参数 */
  private Object params;

  public JsonRpcRequest() {
  }

  public JsonRpcRequest(String method, Object id, Object params) {
    this.jsonrpc = McpConsts.JSONRPC_VERSION;
    this.method = method;
    this.id = id;
    this.params = params;
  }

  @Override
  public String toString() {
    return JsonUtil.toJsonString(this);
  }
}
