package com.iwhalecloud.bote.common.consts;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.Nullable;

/**
 * 大模型推理策略
 *
 * @author bianjp
 * @since 2025-07-09
 */
public enum ThinkingStrategy {
  /** 默认，由模型决定 */
  @JsonProperty("default")
  DEFAULT,
  /** 开启（模型不支持开启时忽略） */
  @JsonProperty("enabled")
  ENABLED,
  /** 关闭（模型不支持关闭时只做隐藏） */
  @JsonProperty("disabled")
  DISABLED,
  /** 隐藏 */
  @JsonProperty("hidden")
  HIDDEN;

  /**
   * 是否显示思考内容
   */
  public static boolean shouldShowReasoning(@Nullable ThinkingStrategy strategy) {
    return strategy != DISABLED && strategy != HIDDEN;
  }
}
