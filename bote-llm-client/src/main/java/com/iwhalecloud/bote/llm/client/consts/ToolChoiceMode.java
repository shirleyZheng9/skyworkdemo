package com.iwhalecloud.bote.llm.client.consts;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 工具选择模式
 *
 * @author bianjp
 * @since 2024-08-01
 */
public enum ToolChoiceMode {
  /** 不调用工具 */
  @JsonProperty("none")
  NONE,
  /** 自动判断是否调用工具 */
  @JsonProperty("auto")
  AUTO,
  /** 强制调用工具 */
  @JsonProperty("required")
  REQUIRED
}
