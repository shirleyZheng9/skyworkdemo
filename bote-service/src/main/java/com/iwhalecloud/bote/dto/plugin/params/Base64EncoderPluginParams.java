package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Base64编码插件参数
 *
 * @author qian.sisheng
 * @since 2025-06-11
 */
@Getter
@Setter
@ToString
public class Base64EncoderPluginParams extends AbstractPluginParams {
  /** 文本 */
  private String text;

  public Base64EncoderPluginParams() {
    super(PluginConsts.PLUGIN_CODE_BASE_64_ENCODER);
  }
}
