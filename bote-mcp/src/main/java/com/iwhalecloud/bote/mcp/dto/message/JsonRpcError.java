package com.iwhalecloud.bote.mcp.dto.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * JSON-RPC 错误
 *
 * @author bianjp
 * @since 2025-05-22
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class JsonRpcError {
  /** 错误编码 */
  private int code;
  /** 错误信息 */
  private String message;
  /** 数据 */
  private Object data;

  public JsonRpcError() {
  }

  public JsonRpcError(int code, String message) {
    this.code = code;
    this.message = message;
  }

  public JsonRpcError(int code, String message, Object data) {
    this.code = code;
    this.message = message;
    this.data = data;
  }
}
