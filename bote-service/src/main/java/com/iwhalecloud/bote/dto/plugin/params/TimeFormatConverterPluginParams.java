package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 时间格式转换插件参数
 *
 * @author lizuyin
 * @since 2025-11-17
 */
@Getter
@Setter
@ToString
public class TimeFormatConverterPluginParams extends AbstractPluginParams {
  /** 需要转换的时间（可选，字符串类型），不填则默认使用当前环境时区的时间 */
  private String time;
  /** 目标格式（可选，字符串类型），不填则默认使用yyyy-MM-dd HH:mm:ss格式 */
  private String toFormat;
  /** 原始格式（可选，字符串类型），不填则默认使用yyyy-MM-dd HH:mm:ss格式 */
  private String fromFormat;

  public TimeFormatConverterPluginParams() {
    super(PluginConsts.PLUGIN_CODE_TIME_FORMAT_CONVERTER);
  }
}

