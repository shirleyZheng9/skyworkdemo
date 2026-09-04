package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 实验聚合计算状态DTO枚举
 * 对应Go: domain_expt.ExptAggregateCalculateStatus
 */
@Getter
public enum ExptAggregateCalculateStatusDTO {

  /**
   * 计算中
   * 对应Go: ExptAggregateCalculateStatus_Calculating = 1
   */
  CALCULATING(1, "Calculating"),

  /**
   * 已完成
   * 对应Go: ExptAggregateCalculateStatus_Completed = 2
   */
  COMPLETED(2, "Completed"),

  /**
   * 失败
   * 对应Go: ExptAggregateCalculateStatus_Failed = 3
   */
  FAILED(3, "Failed");

  @JsonValue
  private final int value;
  private final String description;

  ExptAggregateCalculateStatusDTO(int value, String description) {
    this.value = value;
    this.description = description;
  }

  @JsonCreator
  public static ExptAggregateCalculateStatusDTO fromValue(int value) {
    for (ExptAggregateCalculateStatusDTO status : values()) {
      if (status.value == value) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown ExptAggregateCalculateStatusDTO value: " + value);
  }

  @Override
  public String toString() {
    return this.description;
  }
}
