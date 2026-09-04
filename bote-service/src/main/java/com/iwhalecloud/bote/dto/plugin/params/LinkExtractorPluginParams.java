package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 链接提取插件参数
 *
 * @author zhang.jun
 * @since 2025-08-20
 */

@Getter
@Setter
@ToString
public class LinkExtractorPluginParams extends AbstractPluginParams {
  /** 待处理的文本内容 */
  private String text;

  public LinkExtractorPluginParams() {
    super(PluginConsts.PLUGIN_CODE_LINK_EXTRACTOR);
  }
}
