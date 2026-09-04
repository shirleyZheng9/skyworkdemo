package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 今日头条热榜插件参数
 *
 * @author qian.sisheng
 * @since 2025-07-23
 */
@Getter
@Setter
@ToString
public class TouTiaoHotListPluginParams extends AbstractPluginParams {

  public TouTiaoHotListPluginParams() {
    super(PluginConsts.PLUGIN_CODE_TOU_TIAO_HOT_LIST);
  }
}
