package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * AES 解密插件参数
 *
 * @author qian.sisheng
 * @since 2025-06-11
 */

@Getter
@Setter
@ToString
public class AesDecryptPluginParams extends AbstractPluginParams {
  /** 待解密文本 */
  private String text;
  /** AES 密钥 */
  private String aesKey;

  public AesDecryptPluginParams() {
    super(PluginConsts.PLUGIN_CODE_AES_DECRYPT);
  }
}
