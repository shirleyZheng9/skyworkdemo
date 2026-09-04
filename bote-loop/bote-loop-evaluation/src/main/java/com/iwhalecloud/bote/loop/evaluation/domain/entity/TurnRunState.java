package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * 轮次运行状态枚举
 * 对应Go: entity.TurnRunState
 */
@Getter
public enum TurnRunState {

  /**
   * 队列中（未开始）
   * 对应Go: TurnRunState_Queueing = 0
   */
  @JsonProperty("0")
  QUEUEING(0, "Queueing"),

  /**
   * 执行成功
   * 对应Go: TurnRunState_Success = 1
   */
  @JsonProperty("1")
  SUCCESS(1, "Success"),

  /**
   * 执行失败
   * 对应Go: TurnRunState_Fail = 2
   */
  @JsonProperty("2")
  FAIL(2, "Fail"),

  /**
   * 进行中
   * 对应Go: TurnRunState_Processing = 3
   */
  @JsonProperty("3")
  PROCESSING(3, "Processing"),

  /**
   * 终止
   * 对应Go: TurnRunState_Terminal = 4
   */
  @JsonProperty("4")
  TERMINAL(4, "Terminal");

  private final int value;
  private final String description;

  TurnRunState(int value, String description) {
    this.value = value;
    this.description = description;
  }

  /**
   * 根据值获取枚举
   */
  public static TurnRunState fromValue(int value) {
    for (TurnRunState state : values()) {
      if (state.value == value) {
        return state;
      }
    }
    throw new IllegalArgumentException("Unknown TurnRunState value: " + value);
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
   * 对应Go: IsTurnRunFinished方法
   */
  public boolean isFinished() {
    return this == SUCCESS || this == FAIL || this == TERMINAL;
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
