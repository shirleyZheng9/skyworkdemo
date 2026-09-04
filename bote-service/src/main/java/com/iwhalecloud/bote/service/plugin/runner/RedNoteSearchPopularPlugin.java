package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import org.springframework.stereotype.Component;

/**
 * 小红书搜索爆款笔记
 *
 * @author qian.sisheng
 * @since 2025-07-22
 */
@Component
public class RedNoteSearchPopularPlugin extends AbstractRedNoteSearchPlugin {
  @Override
  protected String getSortType() {
    return "popularity_descending";
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_RED_NOTE_SEARCH_POPULAR;
  }
}
