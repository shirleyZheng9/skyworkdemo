package com.iwhalecloud.bote.loop.client.data.domain.dataset_job;

/**
 * 任务状态枚举
 */
public enum JobStatusDTO {
  UNDEFINED(0, "Undefined"),
  PENDING(1, "Pending"),
  RUNNING(2, "Running"),
  COMPLETED(3, "Completed"),
  FAILED(4, "Failed"),
  CANCELLED(5, "Cancelled");

  private final int value;
  private final String name;

  JobStatusDTO(int value, String name) {
    this.value = value;
    this.name = name;
  }

  public int getValue() {
    return value;
  }

  public String getName() {
    return name;
  }

  public static JobStatusDTO fromValue(int value) {
    for (JobStatusDTO status : values()) {
      if (status.value == value) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown job status: " + value);
  }
}
