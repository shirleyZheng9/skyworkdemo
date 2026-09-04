package com.iwhalecloud.bote.service.plugin;

import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.util.Assert;

/**
 * 插件注册器
 *
 * @author qian.sisheng
 * @since 2025-04-09
 */
public final class PluginFactory {

  /** 插件执行器列表 */
  private static final List<IPlugin> plugins = new ArrayList<>();
  /** 插件执行器 map */
  private static final Map<String, IPlugin> runnerMap = new HashMap<>();

  private PluginFactory() {
  }

  static {
    register();
  }

  /**
   * 注册插件执行器
   */
  private static void register() {
    plugins.addAll(SpringUtil.getBeansOfType(IPlugin.class).values());
    for (IPlugin plugin : plugins) {
      runnerMap.put(plugin.getPluginCode(), plugin);
    }
  }

  /**
   * 获取插件执行器
   */
  public static IPlugin getPlugin(String pluginCode) {
    IPlugin plugin = runnerMap.get(pluginCode);
    Assert.notNull(plugin, () -> "未知插件: " + pluginCode);
    return plugin;
  }
}
