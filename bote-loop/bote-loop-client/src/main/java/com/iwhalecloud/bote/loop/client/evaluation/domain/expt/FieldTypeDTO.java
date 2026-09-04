package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * 字段类型枚举
 */
@Getter
public enum FieldTypeDTO {
  @JsonProperty("0")
  UNKNOWN(0, "Unknown"),
  @JsonProperty("1")
  EVALUATOR_SCORE(1, "EvaluatorScore"),
  @JsonProperty("2")
  CREATOR_BY(2, "CreatorBy"),
  @JsonProperty("3")
  EXPT_STATUS(3, "ExptStatus"),
  @JsonProperty("4")
  TURN_RUN_STATE(4, "TurnRunState"),
  @JsonProperty("5")
  TARGET_ID(5, "TargetID"),
  @JsonProperty("6")
  EVAL_SET_ID(6, "EvalSetID"),
  @JsonProperty("7")
  EVALUATOR_ID(7, "EvaluatorID"),
  @JsonProperty("8")
  TARGET_TYPE(8, "TargetType"),
  @JsonProperty("9")
  SOURCE_TARGET(9, "SourceTarget"),
  @JsonProperty("20")
  EVALUATOR_VERSION_ID(20, "EvaluatorVersionID"),
  @JsonProperty("21")
  TARGET_VERSION_ID(21, "TargetVersionID"),
  @JsonProperty("22")
  EVAL_SET_VERSION_ID(22, "EvalSetVersionID"),
  @JsonProperty("30")
  EXPT_TYPE(30, "ExptType"),
  @JsonProperty("31")
  SOURCE_TYPE(31, "SourceType"),
  @JsonProperty("32")
  SOURCE_ID(32, "SourceID"),
  @JsonProperty("41")
  KEYWORD_SEARCH(41, "KeywordSearch"),
  @JsonProperty("42")
  EVAL_SET_COLUMN(42, "EvalSetColumn"),
  @JsonProperty("43")
  ANNOTATION(43, "Annotation"),
  @JsonProperty("44")
  ACTUAL_OUTPUT(44, "ActualOutput"),
  @JsonProperty("45")
  EVALUATOR_SCORE_CORRECTED(45, "EvaluatorScoreCorrected"),
  @JsonProperty("46")
  EVALUATOR(46, "Evaluator"),
  @JsonProperty("47")
  ITEM_ID(47, "ItemID"),
  @JsonProperty("48")
  ITEM_RUN_STATE(48, "ItemRunState");

  private final int value;
  private final String name;

  FieldTypeDTO(int value, String name) {
    this.value = value;
    this.name = name;
  }

  public static FieldTypeDTO fromValue(int value) {
    for (FieldTypeDTO type : values()) {
      if (type.value == value) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown field type: " + value);
  }
}
