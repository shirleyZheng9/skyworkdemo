package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.service.plugin.impl.helper.PluginHubHelper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 插件图标缓存
 *
 * @author wangtingyun
 * @since 2025-12-25
 */
@Component
public class PluginIconCache extends AbstractIconCache {

  private final PluginHubHelper pluginHubHelper;

  public PluginIconCache(PluginHubHelper pluginHubHelper) {
    super("插件图标", null);
    this.pluginHubHelper = pluginHubHelper;
  }

  @Override
  public String getCacheName() {
    return CacheConsts.CACHE_NAME_PLUGIN_ICON;
  }

  @Override
  protected String loadIconContent(Long tenantId, Long id) {
    String pluginIcon = pluginHubHelper.queryPluginIcon(tenantId, id);
    if (StringUtils.isBlank(pluginIcon)) {
      throw new BssException("插件未配置图标");
    }
    return pluginIcon;
  }
}
