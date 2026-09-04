package com.iwhalecloud.bote.llm.client.consts;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.Nullable;

/**
 * 函数调用模式
 *
 * @author bianjp
 * @since 2024-10-16
 */
public enum FunctionCallMode {
  /** tool 模式(入参使用 tools 参数) */
  @JsonProperty("tool")
  TOOL,
  /** function 模式（入参使用 functions 参数, OpenAI 废弃的协议格式，但一些国产模型的旧版本仍然只支持这种模式） */
  @JsonProperty("function")
  FUNCTION,
  /** 不支持 */
  @JsonProperty("none")
  NONE;

  /**
   * 根据字符串获取函数调用模式枚举值
   */
  public static FunctionCallMode ofMode(@Nullable String mode) {
    if (mode == null) {
      return TOOL;
    }
    return valueOf(mode.toUpperCase());
  }
}
