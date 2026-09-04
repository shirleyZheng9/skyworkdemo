package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.util.TenantIdUtil;
import com.iwhalecloud.bote.dto.agent.SimpleAiDefinitionDTO;
import com.iwhalecloud.bote.dto.agent.SimpleAiEnvVariableDTO;
import com.iwhalecloud.bote.dto.bot.SimpleBotDTO;
import com.iwhalecloud.bote.mapper.agent.GeneralAgentQueryMapper;
import com.iwhalecloud.bote.mapper.bot.BotQueryMapper;
import com.iwhalecloud.bss.litchi.cache.helper.BaseLocalCache;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * API 鉴权数据缓存
 *
 * @author chen.linfa
 * @since 2025-01-22
 */
@Component
public class GeneraAgentCache extends BaseLocalCache<SimpleAiDefinitionDTO> implements TenantCacheMarker {
  private final GeneralAgentQueryMapper generalAgentQueryMapper;
  private final BotQueryMapper botQueryMapper;

  public GeneraAgentCache(GeneralAgentQueryMapper generalAgentQueryMapper, BotQueryMapper botQueryMapper) {
    super();
    this.generalAgentQueryMapper = generalAgentQueryMapper;
    this.botQueryMapper = botQueryMapper;
  }

  @Override
  public String getCacheName() {
    return CacheConsts.CACHE_NAME_GENERAL_AGENT;
  }

  public SimpleAiDefinitionDTO getAiDefinition(Long tenantId, Long botId, Long userId, Long spaceId) {
    Long modelId = null;
    boolean isOther = false;
    if (!BaseConsts.BOTE_AI_ID.equals(botId)) {
      SimpleBotDTO bot = botQueryMapper.getBot(botId, tenantId);
      Assert.notNull(bot, () -> "智能体不存在: tenantId=" + tenantId + ", botId=" + botId);
      Long creatorId = bot.getCreatorId();
      if (!Objects.equals(userId, creatorId) || !Objects.equals(tenantId, spaceId)) {
        // 授权类型的 BoteClaw，需要加载提供方的配置
        isOther = true;
        modelId = generalAgentQueryMapper.getModelId(spaceId, botId, userId);
        userId = creatorId;
      }
    }
    String key = tenantId + CacheConsts.COLON + botId + CacheConsts.COLON + userId;
    SimpleAiDefinitionDTO definition = get(key);
    Assert.notNull(definition, () -> "未检查到 BoteClaw 设置，请联系系统管理员");

    // 大模型的处理优先级：用户启用的《 空间启用的《 平台启用的
    Long modelTenantId = spaceId;
    if (!isOther) {
      modelId = definition.getModelId();
    }
    if (modelId == null) {
      // 存量代码，空间级模型，会虚拟出一个 tenantId，而不是 spaceId 当做 tenantId
      modelTenantId = TenantIdUtil.getSpaceTenantId(spaceId);
      if (modelTenantId != null) {
        modelId = generalAgentQueryMapper.getSpaceModelId(modelTenantId);
      }
      if (modelId == null) {
        modelId = generalAgentQueryMapper.getPlatformModelId();
        modelTenantId = BaseConsts.PLATFORM_TENANT_ID;
      }
    }
    else {
      modelTenantId = generalAgentQueryMapper.getModelTenantId(spaceId, modelId);
    }
    Assert.notNull(modelId, () -> "未配置启用的大模型，请检查博特 AI 设置");
    definition.setModelId(modelId);
    definition.setModelTenantId(modelTenantId);
    return definition;
  }

  public Map<String, String> getEnvVariables(Long tenantId, Long botId, Long userId) {
    if (!BaseConsts.BOTE_AI_ID.equals(botId)) {
      userId = botQueryMapper.getBot(botId, tenantId).getCreatorId();
    }
    String key = tenantId + CacheConsts.COLON + botId + CacheConsts.COLON + userId;
    SimpleAiDefinitionDTO definition = get(key);
    return definition == null ? Map.of() : definition.getEnvVariables();
  }

  @Override
  @Nullable
  protected SimpleAiDefinitionDTO load(String key) {
    if (StringUtils.isEmpty(key)) {
      return null;
    }
    String[] parts = key.split(CacheConsts.COLON);
    Long tenantId = Long.valueOf(parts[0]);
    Long botId = Long.valueOf(parts[1]);
    Long userId = Long.valueOf(parts[2]);

    SimpleAiDefinitionDTO definition = new SimpleAiDefinitionDTO();
    definition.setTenantId(tenantId);
    definition.setBotId(botId);
    definition.setUserId(userId);
    definition.setModelId(generalAgentQueryMapper.getModelId(tenantId, botId, userId));
    Map<String, String> envVariables = generalAgentQueryMapper.selectEnvVariableList(tenantId, botId, userId).stream()
      .collect(Collectors.toMap(SimpleAiEnvVariableDTO::getVariableCode, SimpleAiEnvVariableDTO::getVariableValue, (a, b) -> b));
    definition.setEnvVariables(envVariables.isEmpty() ? null : envVariables);
    definition.setPlatformMcpIds(generalAgentQueryMapper.selectEnabledMcpIds(tenantId, botId, userId, true));
    definition.setMcpIds(generalAgentQueryMapper.selectEnabledMcpIds(tenantId, botId, userId, false));
    definition.setPlatformAgentSkillIds(generalAgentQueryMapper.selectEnabledSkillIds(tenantId, botId, userId, true));
    definition.setAgentSkillIds(generalAgentQueryMapper.selectEnabledSkillIds(tenantId, botId, userId, false));
    return definition;
  }

  @Override
  public void refreshLocalCache(List<String> keys) {
    if (CollectionUtils.isEmpty(keys)) {
      return;
    }
    // 删除单个租户的缓存
    if (keys.size() == 1 && StringUtils.isNumeric(keys.getFirst())) {
      Long tenantId = Long.parseLong(keys.getFirst());
      List<String> keysOfTenant = localCache.asMap().entrySet().stream()
        .filter(e -> tenantId.equals(e.getValue().getTenantId()))
        .map(Entry::getKey)
        .collect(Collectors.toList());
      if (!keysOfTenant.isEmpty()) {
        localCache.invalidateAll(keysOfTenant);
      }
    }
    else {
      localCache.invalidateAll(keys);
    }
  }
}
