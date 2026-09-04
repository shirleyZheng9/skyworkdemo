package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import lombok.Getter;

/**
 * 源类型枚举
 * 对应Go: SourceType
 */
@Getter
public enum SourceType {

  /**
   * 未知源
   * 对应Go: SourceType_Unknown = 0
   */
  UNKNOWN(0, "Unknown"),

  /**
   * Bot
   * 对应Go: SourceType_CozeBot = 1
   */
  BOT(1, "Bot"),

  /**
   * Loop Prompt
   * 对应Go: SourceType_LoopPrompt = 2
   */
  LOOP_PROMPT(2, "LoopPrompt"),

  /**
   * Workflow
   * 对应Go: SourceType_CozeWorkflow = 3
   */
  WORKFLOW(3, "Workflow");

  private final int value;
  private final String description;

  SourceType(int value, String description) {
    this.value = value;
    this.description = description;
  }

  /**
   * 根据值获取枚举
   */
  public static SourceType fromValue(int value) {
    for (SourceType type : values()) {
      if (type.value == value) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown SourceType value: " + value);
  }

  /**
   * 是否是Bot
   */
  public boolean isBot() {
    return this == BOT;
  }

  /**
   * 是否是LoopPrompt
   */
  public boolean isLoopPrompt() {
    return this == LOOP_PROMPT;
  }

  /**
   * 是否是Workflow
   */
  public boolean isWorkflow() {
    return this == WORKFLOW;
  }

  @Override
  public String toString() {
    return this.description;
  }
}
