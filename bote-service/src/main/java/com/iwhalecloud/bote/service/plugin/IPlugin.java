package com.iwhalecloud.bote.service.plugin;

import com.iwhalecloud.bote.dto.plugin.PluginExecuteParams;

/**
 * 插件接口
 *
 * @author qian.sisheng
 * @since 2025-04-23
 */
public interface IPlugin {

  /** 获取插件编码
   *
   * @return 插件编码
   */
  String getPluginCode();

  /**
   * 执行插件
   *
   * @param requestParams 参数
   * @return 执行结果
   */
  Object execute(PluginExecuteParams requestParams);
}
