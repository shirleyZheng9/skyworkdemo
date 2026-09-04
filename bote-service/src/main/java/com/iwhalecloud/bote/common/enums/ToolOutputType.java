package com.iwhalecloud.bote.common.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 插件出参类型
 *
 * @author bianjp
 * @since 2025-11-17
 */
public enum ToolOutputType {
  /** JSON 输出 */
  @JsonProperty("json")
  JSON,
  /** 流式输出(遵循 OpenAI 会话补全接口的流式输出格式) */
  @JsonProperty("stream")
  STREAM,
  /** 文件输出 */
  @JsonProperty("file")
  FILE,
  /** multipart 输出，适用于同时返回文件和其它出参 */
  @JsonProperty("multipart")
  MULTIPART
}
