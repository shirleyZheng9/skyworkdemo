package com.iwhalecloud.bote.agent.tools;

import com.iwhalecloud.bote.agent.annotation.Tool;
import com.iwhalecloud.bss.litchi.util.DateUtil;

/**
 * 日期时间工具
 *
 * @author bianjp
 * @since 2026-03-05
 */
public final class DateTimeTools {
  private DateTimeTools() {
  }

  /**
   * 获取当前时间
   */
  @Tool(name = "get_current_time", description = "Get current time in ISO 8601 format")
  public static String getCurrentTime() {
    return DateUtil.format("yyyy-MM-dd'T'HH:mm:ssZ");
  }

}
