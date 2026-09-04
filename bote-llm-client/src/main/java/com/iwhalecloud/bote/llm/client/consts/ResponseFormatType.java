package com.iwhalecloud.bote.llm.client.consts;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 响应格式类型
 *
 * @author bianjp
 * @since 2024-08-01
 */
public enum ResponseFormatType {
  @JsonProperty("text")
  TEXT,
  @JsonProperty("json_object")
  JSON_OBJECT
}
