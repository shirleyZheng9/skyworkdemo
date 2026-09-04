package com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 评估目标类型DTO枚举
 * 对应Go: eval_target.EvalTargetType
 */
@Getter
public enum EvalTargetTypeDTO {

  /**
   * Bot
   * 对应Go: EvalTargetType_CozeBot = 1
   */
  BOT(1, "Bot"),

  /**
   * Prompt
   * 对应Go: EvalTargetType_LoopPrompt = 2
   */
  LOOP_PROMPT(2, "LoopPrompt"),

  /**
   * Workflow
   * 对应Go: EvalTargetType_CozeWorkflow = 3
   */
  WORKFLOW(3, "Workflow");

  /**
   * 枚举值
   * 对应Go: int64
   */
  @JsonValue
  private final int value;

  /**
   * 枚举描述
   */
  private final String description;

  EvalTargetTypeDTO(int value, String description) {
    this.value = value;
    this.description = description;
  }

  /**
   * 根据值获取枚举
   */
  @JsonCreator
  public static EvalTargetTypeDTO fromValue(int value) {
    for (EvalTargetTypeDTO type : values()) {
      if (type.value == value) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown EvalTargetTypeDTO value: " + value);
  }

  /**
   * 根据描述获取枚举
   */
  public static EvalTargetTypeDTO fromDescription(String description) {
    for (EvalTargetTypeDTO type : values()) {
      if (type.description.equals(description)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown EvalTargetTypeDTO description: " + description);
  }

  /**
   * 是否是Bot类型
   */
  public boolean isBot() {
    return this == BOT;
  }

  /**
   * 是否是Prompt类型
   */
  public boolean isLoopPrompt() {
    return this == LOOP_PROMPT;
  }

  /**
   * 是否是Workflow类型
   */
  public boolean isWorkflow() {
    return this == WORKFLOW;
  }

  @Override
  public String toString() {
    return this.description;
  }
}
