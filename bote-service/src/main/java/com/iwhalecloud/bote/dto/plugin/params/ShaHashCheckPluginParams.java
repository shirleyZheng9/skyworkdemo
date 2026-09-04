package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * SHA哈希算法加密验证插件参数
 *
 * @author lizuyin
 * @since 2025-11-18
 */
@Getter
@Setter
@ToString
public class ShaHashCheckPluginParams extends AbstractPluginParams {
  /** 原始输入字符串 */
  private String input;
  /** 已加密的SHA密文（40位十六进制字符串） */
  private String hash;

  public ShaHashCheckPluginParams() {
    super(PluginConsts.PLUGIN_CODE_SHA_HASH_CHECK);
  }
}

