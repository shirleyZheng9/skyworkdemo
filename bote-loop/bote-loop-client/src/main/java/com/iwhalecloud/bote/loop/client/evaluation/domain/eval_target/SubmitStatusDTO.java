package com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 提交状态DTO枚举
 * 对应Go: eval_target.SubmitStatus
 */
@Getter
public enum SubmitStatusDTO {

  /**
   * 草稿状态
   * 对应Go: SubmitStatus_Draft = 1
   */
  DRAFT(1, "Draft"),

  /**
   * 已提交状态
   * 对应Go: SubmitStatus_Submitted = 2
   */
  SUBMITTED(2, "Submitted"),

  /**
   * 已发布状态
   * 对应Go: SubmitStatus_Published = 3
   */
  PUBLISHED(3, "Published"),

  /**
   * 已归档状态
   * 对应Go: SubmitStatus_Archived = 4
   */
  ARCHIVED(4, "Archived");

  @JsonValue
  private final int value;
  private final String description;

  SubmitStatusDTO(int value, String description) {
    this.value = value;
    this.description = description;
  }

  @JsonCreator
  public static SubmitStatusDTO fromValue(int value) {
    for (SubmitStatusDTO status : values()) {
      if (status.value == value) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown SubmitStatusDTO value: " + value);
  }

  @Override
  public String toString() {
    return this.description;
  }
}
