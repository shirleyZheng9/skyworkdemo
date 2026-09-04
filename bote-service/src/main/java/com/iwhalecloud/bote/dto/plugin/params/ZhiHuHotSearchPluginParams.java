package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;

/**
 * 知乎热搜参数
 *
 * @author qian.sisheng
 * @since 2025-07-23
 */
@Getter
@Setter
public class ZhiHuHotSearchPluginParams extends AbstractPluginParams {
  /** cookie */
  private String cookie;

  public ZhiHuHotSearchPluginParams() {
    super(PluginConsts.PLUGIN_CODE_ZHIHU_HOT_SEARCH);
  }
}
