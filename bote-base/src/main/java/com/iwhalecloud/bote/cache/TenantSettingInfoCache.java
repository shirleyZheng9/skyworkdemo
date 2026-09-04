package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.BaseSystemParameter;
import com.iwhalecloud.bote.common.util.AesUtil;
import com.iwhalecloud.bote.dto.intent.IntentStrategyDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeInfoDTO;
import com.iwhalecloud.bote.dto.portal.TenantSettingInfoDTO;
import com.iwhalecloud.bote.dto.tenant.setting.SimpleTenantSettingInfo;
import com.iwhalecloud.bote.dto.tenant.setting.KnowledgeGraphLoginDTO;
import com.iwhalecloud.bote.dto.tenant.setting.TenantDocChainAccountSettingDTO;
import com.iwhalecloud.bote.dto.tenant.setting.TenantFlowLogSettingDTO;
import com.iwhalecloud.bote.dto.tenant.setting.TenantLargeModelSettingDTO;
import com.iwhalecloud.bote.dto.tenant.setting.TenantPluginHubSettingDTO;
import com.iwhalecloud.bote.dto.tenant.setting.TenantSecurityFlowSettingDTO;
import com.iwhalecloud.bote.dto.tenant.setting.TenantSuggestionSettingDTO;
import com.iwhalecloud.bote.dto.tenant.setting.TenantWatermarkSettingDTO;
import com.iwhalecloud.bote.mapper.portal.TenantSettingInfoManageMapper;
import com.iwhalecloud.bote.service.intent.IIntentManageService;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 租户设置定义缓存
 *
 * @author chen.linfa
 * @since 2025-01-09
 */
@Component
public class TenantSettingInfoCache extends AbstractTenantCache<SimpleTenantSettingInfo> {

  private final TenantSettingInfoManageMapper mapper;
  private final IIntentManageService intentManageService;

  public TenantSettingInfoCache(TenantSettingInfoManageMapper mapper, IIntentManageService intentManageService) {
    super(CacheConsts.KEY_PREFIX_TENANT_SETTING);
    this.mapper = mapper;
    this.intentManageService = intentManageService;
  }

  @Override
  public String getCacheName() {
    return CacheConsts.CACHE_NAME_TENANT_SETTING;
  }

  /**
   * 获取租户设置
   */
  @Nullable
  public SimpleTenantSettingInfo get(Long tenantId) {
    return get(tenantId.toString());
  }

  /**
   * 获取租户的意图设置
   */
  public IntentStrategyDTO getIntentStrategy(Long tenantId) {
    SimpleTenantSettingInfo info = get(tenantId);
    if (info != null && info.getIntentStrategy() != null) {
      return info.getIntentStrategy();
    }
    return new IntentStrategyDTO();
  }

  /**
   * 获取租户的意图向量化配置，未开启时报错
   */
  public IntentStrategyDTO getRequiredIntentStrategy(Long tenantId) {
    IntentStrategyDTO strategy = getIntentStrategy(tenantId);
    Assert.isTrue(strategy.isEmbedding(), "租户未开启意图向量化");
    Assert.notNull(strategy.getEmbeddingModelId(), "未配置意图向量化的文本嵌入模型");
    return strategy;
  }

  /**
   * 获取默认大模型 ID
   *
   * @param tenantId 租户 ID
   * @return 大模型 ID
   */
  public Long getModelId(Long tenantId) {
    SimpleTenantSettingInfo info = get(tenantId);
    if (info != null && info.getLargeModelId() != null) {
      return info.getLargeModelId();
    }
    // 租户未配置默认大模型时报错
    throw BaseErrorConstant.NO_DEFAULT_MODEL.toException();
  }

  /**
   * 安全围栏工作流ID
   *
   * @param tenantId 租户 ID
   * @param type 类型
   * @return 安全围栏工作流ID
   */
  @Nullable
  public Long getSecurityFlowId(Long tenantId, String type) {
    SimpleTenantSettingInfo info = get(tenantId);
    if (info != null && info.getSecurityFlow() != null) {
      Long flowId = info.getSecurityFlow().getUserInputFlowId();
      if (BaseConsts.SECURITY_TYPE_LLM_INPUT.equals(type)) {
        flowId = info.getSecurityFlow().getLlmInputFlowId();
      }
      else if (BaseConsts.SECURITY_TYPE_LLM_OUTPUT.equals(type)) {
        flowId = info.getSecurityFlow().getLlmOutputFlowId();
      }
      return flowId;
    }
    return null;
  }

  /**
   * 获取默认大模型 ID，未配置时返回 null
   *
   * @param tenantId 租户 ID
   * @return 大模型 ID
   */
  @Nullable
  public Long getModelIdOrNull(Long tenantId) {
    SimpleTenantSettingInfo info = get(tenantId);
    if (info != null && info.getLargeModelId() != null) {
      return info.getLargeModelId();
    }
    return null;
  }

  /**
   * 获取插件市场对应的门户开发者密钥
   *
   * @param tenantId 租户 ID
   * @return 门户开发者密钥
   */
  @Nullable
  public String getPluginApiKey(Long tenantId) {
    SimpleTenantSettingInfo info = get(tenantId);
    if (info != null && info.getPluginHub() != null) {
      return info.getPluginHub().getPortalUserApiKey();
    }
    return null;
  }

  /**
   * 检查租户是否开启了流程日志
   */
  public boolean isFlowLogEnabled(Long tenantId) {
    SimpleTenantSettingInfo info = get(tenantId);
    return info != null && Boolean.TRUE.equals(info.getFlowLogEnabled());
  }

  /**
   * 获取联想术语匹配阈值
   *
   * @param tenantId 租户 ID
   * @return 匹配阈值，默认 0.7
   */
  public double getSuggestionScore(Long tenantId) {
    SimpleTenantSettingInfo info = get(tenantId);
    if (info != null && info.getSuggestionScore() != null) {
      return info.getSuggestionScore();
    }
    return 5.0; // 默认值
  }

  /**
   * 获取联想术语匹配条数
   *
   * @param tenantId 租户 ID
   * @return 匹配条数，默认 10
   */
  public int getSuggestionLimit(Long tenantId) {
    SimpleTenantSettingInfo info = get(tenantId);
    if (info != null && info.getSuggestionLimit() != null) {
      return info.getSuggestionLimit();
    }
    return 10; // 默认值
  }

  /**
   * 获取租户的知识库设置
   */
  public KnowledgeInfoDTO getKnowledgeInfo(Long tenantId) {
    SimpleTenantSettingInfo info = get(tenantId);
    if (info != null && info.getKnowledgeInfoDTO() != null) {
      return info.getKnowledgeInfoDTO();
    }
    return new KnowledgeInfoDTO();
  }

  /**
   * 获取 knowledgeGraph 租户侧 SSO 载荷（project / 对接用户）
   */
  public KnowledgeGraphLoginDTO getKnowledgeGraphLoginPayload(Long tenantId) {
    SimpleTenantSettingInfo info = get(tenantId);
    if (info != null && info.getKnowledgeGraphLoginPayload() != null) {
      return info.getKnowledgeGraphLoginPayload();
    }
    return new KnowledgeGraphLoginDTO();
  }

  @Override
  public void refresh(List<String> keys) {
    // 缓存 key 只有 tenantId, 不能复用 AbstractTenantCache 中的刷新逻辑
    if (logger.isDebugEnabled()) {
      logger.debug("Refresh by keys: cacheName={}, keys={}", getCacheName(), keys);
    }
    if (CollectionUtils.isNotEmpty(keys)) {
      deleteDistributedCache(keys);
      deleteLocalCache(keys);
    }
  }

  @Override
  public void refreshLocalCache(List<String> keys) {
    // 缓存 key 只有 tenantId, 不能复用 AbstractTenantCache 中的刷新逻辑
    if (logger.isDebugEnabled()) {
      logger.debug("Refresh local by keys: cacheName={}, keys={}", getCacheName(), keys);
    }
    if (CollectionUtils.isNotEmpty(keys)) {
      deleteLocalCache(keys);
    }
  }

  @Nullable
  @Override
  protected SimpleTenantSettingInfo load(String tenantId) {
    SimpleTenantSettingInfo info = new SimpleTenantSettingInfo();
    List<TenantSettingInfoDTO> list = mapper.selectTenantSettingInfoList(Long.valueOf(tenantId));
    for (TenantSettingInfoDTO setting : list) {
      if (StringUtils.isNotEmpty(setting.getFuncType()) && StringUtils.isNotEmpty(setting.getSettingInfo())) {
        parseSetting(setting.getFuncType(), setting.getSettingInfo(), info);
      }
    }
    // 意图设置
    IntentStrategyDTO strategy = intentManageService.findStrategy(Long.valueOf(tenantId));
    info.setIntentStrategy(strategy);
    return info;
  }

  /**
   * 解析设置
   */
  private void parseSetting(String funcType, String settingInfo, SimpleTenantSettingInfo info) {
    switch (funcType) {
      // DocChain 账号
      case CommonConsts.FUNC_TYPE_KNOWLEDGE:
        parseDocChainAccount(settingInfo, info);
        break;
      // 知识库
      case CommonConsts.FUNC_TYPE_KNOWLEDGE_OTHER:
        KnowledgeInfoDTO knowledgeInfo = JsonUtil.parseJsonRequired(settingInfo, KnowledgeInfoDTO.class);
        info.setKnowledgeInfoDTO(knowledgeInfo);
        break;
      case CommonConsts.FUNC_TYPE_KNOWLEDGE_GRAPH:
        KnowledgeGraphLoginDTO kgLogin = JsonUtil.parseJsonRequired(settingInfo, KnowledgeGraphLoginDTO.class);
        info.setKnowledgeGraphLoginPayload(kgLogin);
        break;
      // 默认大模型
      case CommonConsts.FUNC_DEFAULT_TYPE_LARGE_MODEL:
        TenantLargeModelSettingDTO largeModelSetting = JsonUtil.parseJsonRequired(settingInfo, TenantLargeModelSettingDTO.class);
        info.setLargeModelId(largeModelSetting.getLargeModelId());
        break;
      // 流程日志
      case CommonConsts.FUNC_TYPE_FLOW_LOG:
        TenantFlowLogSettingDTO flowLogSetting = JsonUtil.parseJsonRequired(settingInfo, TenantFlowLogSettingDTO.class);
        info.setFlowLogEnabled(Boolean.TRUE.equals(flowLogSetting.getEnabled()));
        break;
      // 联想术语
      case CommonConsts.FUNC_TYPE_SUGGESTION:
        TenantSuggestionSettingDTO suggestionSetting = JsonUtil.parseJsonRequired(settingInfo, TenantSuggestionSettingDTO.class);
        info.setSuggestionScore(suggestionSetting.getScore());
        info.setSuggestionLimit(suggestionSetting.getLimit());
        break;
      // 插件市场
      case CommonConsts.FUNC_TYPE_PLUGIN_HUB:
        TenantPluginHubSettingDTO pluginHubSetting = JsonUtil.parseJsonRequired(settingInfo, TenantPluginHubSettingDTO.class);
        info.setPluginHub(pluginHubSetting);
        break;
      // 水印设置
      case CommonConsts.FUNC_TYPE_WATERMARK:
        TenantWatermarkSettingDTO watermarkSetting = JsonUtil.parseJsonRequired(settingInfo, TenantWatermarkSettingDTO.class);
        info.setWatermark(watermarkSetting);
        break;
      // 安全围栏
      case CommonConsts.FUNC_TYPE_SECURITY:
        TenantSecurityFlowSettingDTO securityFlowSetting = JsonUtil.parseJsonRequired(settingInfo, TenantSecurityFlowSettingDTO.class);
        info.setSecurityFlow(securityFlowSetting);
        break;
      default:
        // 忽略不关注的配置
        break;
    }
  }

  /**
   * 解析 DocChain 账号设置
   */
  private void parseDocChainAccount(String settingInfo, SimpleTenantSettingInfo info) {
    TenantDocChainAccountSettingDTO map = JsonUtil.parseJsonRequired(settingInfo, TenantDocChainAccountSettingDTO.class);
    info.setKnowledgeUserName(map.getUserName());
    // 兼容旧字段 password
    String token = StringUtils.defaultIfEmpty(map.getToken(), map.getPassword());
    if (StringUtils.isNotEmpty(token)) {
      // AES 解密
      token = StringUtils.defaultIfEmpty(AesUtil.aesDecrypt(token, BaseSystemParameter.ENCRYPTION_AES.getValueFromDb()), token);
      // base64 加密
      token = Base64.encodeBase64String(token.getBytes(StandardCharsets.UTF_8));
      info.setKnowledgePassword(token);
    }
    info.setKnowledgeApiKey(map.getApiKey());
  }

}
