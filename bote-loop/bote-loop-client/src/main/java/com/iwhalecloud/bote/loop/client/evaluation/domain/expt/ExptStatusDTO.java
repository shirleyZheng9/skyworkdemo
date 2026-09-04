package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 实验状态枚举
 */
public enum ExptStatusDTO {
  /** 0 未知 */
  UNKNOWN(0, "Unknown"),
  /** 2 待处理 */
  PENDING(2, "Pending"),
  /** 3 处理中 */
  PROCESSING(3, "Processing"),
  /** 11 成功 */
  SUCCESS(11, "Success"),
  /** 12 失败 */
  FAILED(12, "Failed"),
  /** 13 已终止 */
  TERMINATED(13, "Terminated"),
  /** 14 系统终止 */
  SYSTEM_TERMINATED(14, "SystemTerminated"),
  /** 21 流式完成 */
  DRAINING(21, "Draining");

  @JsonValue
  private final int value;
  private final String name;

  ExptStatusDTO(int value, String name) {
    this.value = value;
    this.name = name;
  }

  public int getValue() {
    return value;
  }

  public String getName() {
    return name;
  }

  @JsonCreator
  public static ExptStatusDTO fromValue(int value) {
    for (ExptStatusDTO status : values()) {
      if (status.value == value) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown expt status: " + value);
  }
}
