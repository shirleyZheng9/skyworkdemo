package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import lombok.Getter;

/**
 * 提交状态枚举
 * 对应Go: SubmitStatus
 */
@Getter
public enum SubmitStatus {

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

  private final int value;
  private final String description;

  SubmitStatus(int value, String description) {
    this.value = value;
    this.description = description;
  }

  /**
   * 根据值获取枚举
   */
  public static SubmitStatus fromValue(int value) {
    for (SubmitStatus status : values()) {
      if (status.value == value) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown SubmitStatus value: " + value);
  }

  /**
   * 根据描述获取枚举
   */
  public static SubmitStatus fromDescription(String description) {
    for (SubmitStatus status : values()) {
      if (status.description.equals(description)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown SubmitStatus description: " + description);
  }

  /**
   * 是否是草稿状态
   */
  public boolean isDraft() {
    return this == DRAFT;
  }

  /**
   * 是否是已提交状态
   */
  public boolean isSubmitted() {
    return this == SUBMITTED;
  }

  /**
   * 是否是已发布状态
   */
  public boolean isPublished() {
    return this == PUBLISHED;
  }

  /**
   * 是否是已归档状态
   */
  public boolean isArchived() {
    return this == ARCHIVED;
  }

  /**
   * 是否可以编辑（草稿状态）
   */
  public boolean isEditable() {
    return this == DRAFT;
  }

  /**
   * 是否是最终状态（已发布或已归档）
   */
  public boolean isFinalState() {
    return this == PUBLISHED || this == ARCHIVED;
  }

  @Override
  public String toString() {
    return this.description;
  }
}
