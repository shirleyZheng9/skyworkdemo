package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Base64解码参数
 *
 * @author qian.sisheng
 * @since 2025-06-11
 */
@Getter
@Setter
@ToString
public class Base64DecoderPluginParams extends AbstractPluginParams {
  /** 编码文本 */
  private String encoderText;

  public Base64DecoderPluginParams() {
    super(PluginConsts.PLUGIN_CODE_BASE_64_DECODER);
  }
}
