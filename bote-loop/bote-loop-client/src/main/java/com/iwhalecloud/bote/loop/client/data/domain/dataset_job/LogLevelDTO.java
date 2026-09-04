package com.iwhalecloud.bote.loop.client.data.domain.dataset_job;

/**
 * 日志级别枚举
 */
public enum LogLevelDTO {
  INFO("info"),
  ERROR("error"),
  WARNING("warning");

  private final String value;

  LogLevelDTO(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static LogLevelDTO fromValue(String value) {
    for (LogLevelDTO level : values()) {
      if (level.value.equals(value)) {
        return level;
      }
    }
    throw new IllegalArgumentException("Unknown log level: " + value);
  }
}
