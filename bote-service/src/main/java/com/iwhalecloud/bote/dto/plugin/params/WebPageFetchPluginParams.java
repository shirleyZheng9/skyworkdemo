package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 网页内容提取参数
 *
 * @author zhangJun
 * @since 2025-07-18
 */

@Getter
@Setter
@ToString
public class WebPageFetchPluginParams extends AbstractPluginParams {
  /** 网站地址 */
  private String webPageUrl;

  public WebPageFetchPluginParams() {
    super(PluginConsts.PLUGIN_CODE_WEB_PAGE_FETCH);
  }
}
