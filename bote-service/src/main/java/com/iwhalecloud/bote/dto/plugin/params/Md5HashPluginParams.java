package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * MD5哈希算法加密插件参数
 *
 * @author lizuyin
 * @since 2025-11-18
 */
@Getter
@Setter
@ToString
public class Md5HashPluginParams extends AbstractPluginParams {
  /** 待加密内容 */
  private String src;

  public Md5HashPluginParams() {
    super(PluginConsts.PLUGIN_CODE_MD5_HASH);
  }
}

