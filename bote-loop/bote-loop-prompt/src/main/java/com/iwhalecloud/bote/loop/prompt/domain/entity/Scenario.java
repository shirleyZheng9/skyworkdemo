package com.iwhalecloud.bote.loop.prompt.domain.entity;

/**
 * 场景枚举
 * 迁移对应关系: Go语言entity.Scenario
 * - 功能: 定义Prompt执行的场景类型
 * - 常量:
 * * DEFAULT - 默认场景
 * * PROMPT_DEBUG - Prompt调试场景
 * * EVAL_TARGET - 评估目标场景
 * <p>
 * Java实现说明:
 * - 对应Go的Scenario类型别名
 * - 使用Java枚举定义
 * - 提供字符串值映射
 * <p>
 * 技术栈迁移:
 * - Go类型别名 -> Java枚举
 * - Go常量 -> Java枚举常量
 */
public enum Scenario {
  /**
   * 默认场景
   * 迁移对应关系: Go语言ScenarioDefault
   * - 功能: 默认的执行场景
   * - 值: "default"
   */
  DEFAULT("default"),

  /**
   * Prompt调试场景
   * 迁移对应关系: Go语言ScenarioPromptDebug
   * - 功能: Prompt调试专用场景
   * - 值: "prompt_debug"
   */
  PROMPT_DEBUG("prompt_debug"),

  /**
   * 评估目标场景
   * 迁移对应关系: Go语言ScenarioEvalTarget
   * - 功能: 评估目标专用场景
   * - 值: "eval_target"
   */
  EVAL_TARGET("eval_target");

  private final String value;

  Scenario(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static Scenario fromValue(String value) {
    for (Scenario scenario : values()) {
      if (scenario.value.equals(value)) {
        return scenario;
      }
    }
    throw new IllegalArgumentException("Unknown Scenario: " + value);
  }
}
