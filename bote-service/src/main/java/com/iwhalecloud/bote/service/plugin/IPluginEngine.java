package com.iwhalecloud.bote.service.plugin;

import com.iwhalecloud.bote.dto.plugin.PluginExecuteParams;

/**
 * 插件引擎
 *
 * @author qian.sisheng
 * @since 2025-04-09
 */
public interface IPluginEngine {

  /**
   * 执行插件
   *
   * @param params 请求参数
   */
  Object execute(PluginExecuteParams params);
}
