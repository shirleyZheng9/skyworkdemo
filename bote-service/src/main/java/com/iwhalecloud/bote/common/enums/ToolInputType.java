package com.iwhalecloud.bote.common.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 插件入参类型
 *
 * @author bianjp
 * @since 2025-11-17
 */
public enum ToolInputType {
  /** JSON 请求体 */
  @JsonProperty("json")
  JSON,
  /** 文件表单 */
  @JsonProperty("multipart")
  MULTIPART
}
