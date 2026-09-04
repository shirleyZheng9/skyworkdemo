package com.iwhalecloud.bote.dto.plugin;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 网易新闻热榜插件参数
 *
 * @author qian.sisheng
 * @since 2025-11-28
 */
@Getter
@Setter
@ToString
public class NetEaseNewsPluginParams extends AbstractPluginParams {

  public NetEaseNewsPluginParams() {
    super(PluginConsts.PLUGIN_CODE_NET_EASE_NEWS_HOT);
  }
}
