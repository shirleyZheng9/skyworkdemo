package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 字段类型枚举
 * 迁移对应关系: Go语言FieldType
 * - 功能: 字段类型枚举
 * - 常量: Unknown, EvaluatorScore, CreatorBy, ExptStatus, TurnRunState, TargetID, EvalSetID, EvaluatorID, TargetType, SourceTarget, EvaluatorVersionID, TargetVersionID, EvalSetVersionID
 * <p>
 * Java实现说明:
 * - 对应Go的FieldType枚举
 * - 使用Java枚举定义
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go枚举 -> Java枚举
 * - Go int64 -> Java int
 */
public enum FieldType {
  @JsonProperty("0")
  UNKNOWN(0),
  @JsonProperty("1")
  EVALUATOR_SCORE(1),
  @JsonProperty("2")
  CREATOR_BY(2),
  @JsonProperty("3")
  EXPT_STATUS(3),
  @JsonProperty("4")
  TURN_RUN_STATE(4),
  @JsonProperty("5")
  TARGET_ID(5),
  @JsonProperty("6")
  EVAL_SET_ID(6),
  @JsonProperty("7")
  EVALUATOR_ID(7),
  @JsonProperty("8")
  TARGET_TYPE(8),
  @JsonProperty("9")
  SOURCE_TARGET(9),
  @JsonProperty("20")
  EVALUATOR_VERSION_ID(20),
  @JsonProperty("21")
  TARGET_VERSION_ID(21),
  @JsonProperty("22")
  EVAL_SET_VERSION_ID(22);

  private final int value;

  FieldType(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
