package com.iwhalecloud.bote.loop.client.prompt.domain.prompt;

/**
 * 场景类型枚举
 * 迁移对应关系: Thrift Scenario
 */
public enum ScenarioDTO {
  DEFAULT("default"),
  EVAL_TARGET("eval_target");

  private final String value;

  ScenarioDTO(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static ScenarioDTO fromValue(String value) {
    for (ScenarioDTO scenario : values()) {
      if (scenario.value.equals(value)) {
        return scenario;
      }
    }
    throw new IllegalArgumentException("Unknown Scenario: " + value);
  }
}
