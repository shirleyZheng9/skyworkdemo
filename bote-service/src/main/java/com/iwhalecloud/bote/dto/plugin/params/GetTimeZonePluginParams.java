package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 获取指定时区时间插件参数
 *
 * @author lizuyin
 * @since 2025-11-17
 */
@Getter
@Setter
@ToString
public class GetTimeZonePluginParams extends AbstractPluginParams {
  /** 时区标识（可选，字符串类型），格式为UTC、UTC+1、UTC-1、UTC+08:00等，不填则使用当前环境的默认时区 */
  private String timeZone;

  public GetTimeZonePluginParams() {
    super(PluginConsts.PLUGIN_CODE_GET_TIME_ZONE);
  }
}

