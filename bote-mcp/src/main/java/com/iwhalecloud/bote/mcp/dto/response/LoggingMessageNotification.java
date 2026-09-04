package com.iwhalecloud.bote.mcp.dto.response;

import com.iwhalecloud.bote.mcp.consts.LoggingLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 日志通知
 *
 * @author bianjp
 * @since 2025-05-22
 */
@Getter
@Setter
@ToString
public class LoggingMessageNotification {
  /** 日志级别 */
  private LoggingLevel level;
  /** 日志名称 */
  private String logger;
  /** 日志数据 */
  private String data;
}
