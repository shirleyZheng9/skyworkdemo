package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * 数据项运行状态枚举
 * 对应Go: entity.ItemRunState
 */
@Getter
public enum ItemRunState {

  /**
   * 未知状态
   * 对应Go: ItemRunState_Unknown = -1
   */
  @JsonProperty("-1")
  UNKNOWN(-1, "Unknown"),

  /**
   * 队列中
   * 对应Go: ItemRunState_Queueing = 0
   */
  @JsonProperty("0")
  QUEUEING(0, "Queueing"),

  /**
   * 处理中
   * 对应Go: ItemRunState_Processing = 1
   */
  @JsonProperty("1")
  PROCESSING(1, "Processing"),

  /**
   * 成功
   * 对应Go: ItemRunState_Success = 2
   */
  @JsonProperty("2")
  SUCCESS(2, "Success"),

  /**
   * 失败
   * 对应Go: ItemRunState_Fail = 3
   */
  @JsonProperty("3")
  FAIL(3, "Fail"),

  /**
   * 终止
   * 对应Go: ItemRunState_Terminal = 5
   */
  @JsonProperty("5")
  TERMINAL(5, "Terminal");

  private final int value;
  private final String description;

  ItemRunState(int value, String description) {
    this.value = value;
    this.description = description;
  }

  /**
   * 根据值获取枚举
   */
  public static ItemRunState fromValue(int value) {
    for (ItemRunState state : values()) {
      if (state.value == value) {
        return state;
      }
    }
    throw new IllegalArgumentException("Unknown ItemRunState value: " + value);
  }

  public boolean isItemRunFinished() {
    return this == FAIL || this == TERMINAL || this == SUCCESS;
  }

  /**
   * 是否是成功状态
   */
  public boolean isSuccess() {
    return this == SUCCESS;
  }

  /**
   * 是否是失败状态
   */
  public boolean isFail() {
    return this == FAIL;
  }

  /**
   * 是否是处理中状态
   */
  public boolean isProcessing() {
    return this == PROCESSING;
  }

  /**
   * 是否是队列中状态
   */
  public boolean isQueueing() {
    return this == QUEUEING;
  }

  /**
   * 是否是终止状态
   */
  public boolean isTerminal() {
    return this == TERMINAL;
  }

  /**
   * 是否是完成状态（成功、失败或终止）
   * 对应Go: IsFinished方法
   */
  public boolean isFinished() {
    return this == FAIL || this == TERMINAL || this == SUCCESS;
  }

  /**
   * 是否是运行中状态（队列中或处理中）
   */
  public boolean isRunning() {
    return this == QUEUEING || this == PROCESSING;
  }

  @Override
  public String toString() {
    return this.description;
  }
}
