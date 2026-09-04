package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.dto.skill.SimpleLlmPluginDTO;
import com.iwhalecloud.bote.dto.skill.SkillPluginDTO;
import com.iwhalecloud.bote.entity.skill.SkillPluginEntity;
import com.iwhalecloud.bote.mapper.skill.SkillPluginManageMapper;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 大模型插件缓存
 * <p>缓存 key 为 tenantId:pluginId</p>
 *
 * @author bianjp
 * @since 2024-12-18
 */
@Component
public final class LlmPluginCache extends AbstractSkillCache<SimpleLlmPluginDTO> {
  private final SkillPluginManageMapper pluginManageMapper;

  public LlmPluginCache(SkillPluginManageMapper pluginManageMapper) {
    super(CacheConsts.KEY_PREFIX_LLM_PLUGIN);
    this.pluginManageMapper = pluginManageMapper;
    disableDistributionCache();
  }

  @Override
  public String getCacheName() {
    return CacheConsts.CACHE_NAME_LLM_PLUGIN;
  }

  /**
   * 获取插件，支持获取平台级数据
   */
  @Nullable
  public SimpleLlmPluginDTO getPlugin(Long tenantId, Long id) {
    SimpleLlmPluginDTO plugin = get(tenantId, id);
    if (plugin == null) {
      plugin = get(BaseConsts.PLATFORM_TENANT_ID, id);
    }
    return plugin;
  }

  @Override
  @Nullable
  protected SimpleLlmPluginDTO loadById(Long tenantId, Long id) {
    SkillPluginDTO plugin = pluginManageMapper.selectSimplePlugin(tenantId, id);
    return plugin == null ? null : SimpleLlmPluginDTO.from(plugin);
  }

  @Override
  protected Map<Long, SimpleLlmPluginDTO> loadByIds(Long tenantId, List<Long> ids) {
    return pluginManageMapper.selectSimplePlugins(tenantId, ids).stream()
      .collect(Collectors.toMap(SkillPluginEntity::getApiId, SimpleLlmPluginDTO::from));
  }
}
