package com.iwhalecloud.bote.llm.client.consts;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JSON Schema 数据类型
 *
 * @author bianjp
 * @see <a href="https://json-schema.org/understanding-json-schema/reference/type">Basic Types</a>
 * @since 2024-08-08
 */
public enum JsonSchemaDataType {
  /** 对象 */
  @JsonProperty("object")
  OBJECT,
  /** 数组 */
  @JsonProperty("array")
  ARRAY,
  /** 字符串 */
  @JsonProperty("string")
  STRING,
  /** 数值（支持整数、浮点数） */
  @JsonProperty("number")
  NUMBER,
  /** 整数 */
  @JsonProperty("integer")
  INTEGER,
  /** 布尔 */
  @JsonProperty("boolean")
  BOOLEAN,
  /** 空 */
  @JsonProperty("null")
  NULL
}
