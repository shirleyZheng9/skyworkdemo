package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

import lombok.Getter;

/**
 * 实验重试模式枚举
 */
@Getter
public enum ExptRetryModeDTO {
  UNKNOWN(0, "Unknown"),
  RETRY_ALL(1, "RetryAll"),
  RETRY_FAILURE(2, "RetryFailure"),
  RETRY_TARGET_ITEMS(3, "RetryTargetItems"),
  RETRY_CATALOG(4, "RetryCatalog");

  private final int value;
  private final String name;

  ExptRetryModeDTO(int value, String name) {
    this.value = value;
    this.name = name;
  }

  public static ExptRetryModeDTO fromValue(int value) {
    for (ExptRetryModeDTO mode : values()) {
      if (mode.value == value) {
        return mode;
      }
    }
    throw new IllegalArgumentException("Unknown retry mode: " + value);
  }
}
