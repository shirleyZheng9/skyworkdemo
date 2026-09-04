package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 时区转换工具插件参数
 *
 * @author lizuyin
 * @since 2025-11-18
 */
@Getter
@Setter
@ToString
public class TimeZoneConverterPluginParams extends AbstractPluginParams {
  /** 原始时区（可选，字符串类型），格式为UTC、UTC+1、UTC-1、UTC+08:00等，不填则使用当前环境的默认时区 */
  private String fromTimezone;
  /** 需要转换的时间（可选，字符串类型），不填则默认使用fromTimezone代表时区的当前时间，只支持yyyy-MM-dd HH:mm:ss */
  private String time;
  /** 目标时区（可选，字符串类型），格式为UTC、UTC+1、UTC-1、UTC+08:00等，不填则使用当前环境的默认时区 */
  private String toTimezone;

  public TimeZoneConverterPluginParams() {
    super(PluginConsts.PLUGIN_CODE_TIME_ZONE_CONVERTER);
  }
}

