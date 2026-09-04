package com.iwhalecloud.bote.mcp.consts;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 日志级别
 *
 * @author bianjp
 * @since 2025-05-22
 */
@Getter
@RequiredArgsConstructor
public enum LoggingLevel {
  @JsonProperty("debug")
  DEBUG(0),
  @JsonProperty("info")
  INFO(1),
  @JsonProperty("notice")
  NOTICE(2),
  @JsonProperty("warning")
  WARNING(3),
  @JsonProperty("error")
  ERROR(4),
  @JsonProperty("critical")
  CRITICAL(5),
  @JsonProperty("alert")
  ALERT(6),
  @JsonProperty("emergency")
  EMERGENCY(7);

  /** 级别 */
  private final int level;
}
