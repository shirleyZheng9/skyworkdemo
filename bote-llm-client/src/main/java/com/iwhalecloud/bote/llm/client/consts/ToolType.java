package com.iwhalecloud.bote.llm.client.consts;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 工具类型
 *
 * @author bianjp
 * @since 2024-08-01
 */
public enum ToolType {
  /** 函数 */
  @JsonProperty("function")
  FUNCTION
}
