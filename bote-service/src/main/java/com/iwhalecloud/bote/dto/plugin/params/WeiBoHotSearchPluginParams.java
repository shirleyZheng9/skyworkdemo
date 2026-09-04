package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 微博热搜参数
 *
 * @author qian.sisheng
 * @since 2025-07-23
 */
@Getter
@Setter
@ToString
public class WeiBoHotSearchPluginParams extends AbstractPluginParams {

  public WeiBoHotSearchPluginParams() {
    super(PluginConsts.PLUGIN_CODE_WEI_BO_HOT_SEARCH);
  }
}
