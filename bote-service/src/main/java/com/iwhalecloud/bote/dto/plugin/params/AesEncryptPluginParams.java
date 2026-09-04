package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * AES 加密插件参数
 *
 * @author qian.sisheng
 * @since 2025-06-11
 */

@Getter
@Setter
@ToString
public class AesEncryptPluginParams extends AbstractPluginParams {
  /** 待加密文本 */
  private String text;
  /** AES 密钥 */
  private String aesKey;

  public AesEncryptPluginParams() {
    super(PluginConsts.PLUGIN_CODE_AES_ENCRYPT);
  }
}
