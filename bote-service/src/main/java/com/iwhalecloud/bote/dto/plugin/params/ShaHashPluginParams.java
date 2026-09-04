package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * SHA哈希算法加密插件参数
 *
 * @author lizuyin
 * @since 2025-11-17
 */
@Getter
@Setter
@ToString
public class ShaHashPluginParams extends AbstractPluginParams {
  /** 待加密内容 */
  private String src;

  public ShaHashPluginParams() {
    super(PluginConsts.PLUGIN_CODE_SHA_HASH);
  }
}

