package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * 实验运行模式枚举
 * 迁移对应关系: Go语言ExptRunMode
 * - 功能: 实验运行模式枚举
 * - 常量: Submit, FailRetry, Append
 * <p>
 * Java实现说明:
 * - 对应Go的ExptRunMode枚举
 * - 使用Java枚举定义
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go枚举 -> Java枚举
 * - Go int32 -> Java int
 */
@Getter
public enum ExptRunMode {
  @JsonProperty("1")
  SUBMIT(1),
  @JsonProperty("2")
  FAIL_RETRY(2),
  @JsonProperty("3")
  APPEND(3),
  @JsonProperty("4")
  ITEM_RETRY(4),
  @JsonProperty("5")
  ALL_RETRY(5),;

  private final int value;

  ExptRunMode(int value) {
    this.value = value;
  }

}
