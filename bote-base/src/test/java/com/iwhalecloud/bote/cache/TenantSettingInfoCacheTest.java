package com.iwhalecloud.bote.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.common.util.AesUtil;
import com.iwhalecloud.bote.dto.intent.IntentStrategyDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeInfoDTO;
import com.iwhalecloud.bote.dto.portal.TenantSettingInfoDTO;
import com.iwhalecloud.bote.dto.tenant.setting.KnowledgeGraphLoginDTO;
import com.iwhalecloud.bote.dto.tenant.setting.SimpleTenantSettingInfo;
import com.iwhalecloud.bote.dto.tenant.setting.TenantDocChainAccountSettingDTO;
import com.iwhalecloud.bote.dto.tenant.setting.TenantFlowLogSettingDTO;
import com.iwhalecloud.bote.dto.tenant.setting.TenantLargeModelSettingDTO;
import com.iwhalecloud.bote.dto.tenant.setting.TenantPluginHubSettingDTO;
import com.iwhalecloud.bote.dto.tenant.setting.TenantSecurityFlowSettingDTO;
import com.iwhalecloud.bote.dto.tenant.setting.TenantSuggestionSettingDTO;
import com.iwhalecloud.bote.dto.tenant.setting.TenantWatermarkSettingDTO;
import com.iwhalecloud.bote.mapper.portal.TenantSettingInfoManageMapper;
import com.iwhalecloud.bote.service.intent.IIntentManageService;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

/**
 * {@link TenantSettingInfoCache} 单元测试。
 *
 * <p>反射关闭 BaseSecondaryCache 后端标志，使 get()->load()->put() 路径不触碰 Redis/本地缓存，
 * 仅经 mapper + intentManageService 加载，从而以纯单测覆盖 parseSetting 的各 funcType 分支、
 * parseDocChainAccount 的 AES 解密分支以及全部公开 getter。JsonUtil 与
 * BaseSystemParameter.ENCRYPTION_AES.getValueFromDb() 均依赖 SpringUtil，故 @BeforeAll 以
 * mockStatic(SpringUtil) 注入真实 ObjectMapper 与 mock DcParamCache。</p>
 */
class TenantSettingInfoCacheTest {

  private static final String AES_KEY = "1234567890abcdef";

  private static MockedStatic<SpringUtil> spring;

  private TenantSettingInfoManageMapper mapper;
  private IIntentManageService intentManageService;
  private DcParamCache dcParamCache;
  private TenantSettingInfoCache cache;

  @BeforeAll
  static void setUpSpring() {
    spring = mockStatic(SpringUtil.class);
    spring.when(() -> SpringUtil.getBean(org.mockito.ArgumentMatchers.eq(ObjectMapper.class),
        org.mockito.ArgumentMatchers.any())).thenReturn(CacheTestSupport.jsonObjectMapper());
    // 在 mockStatic 生效且无未完成 stubbing 时触发 JsonUtil 静态初始化，
    // 避免后续在 when().thenReturn() 表达式内首次加载导致 UnfinishedStubbing
    assertThat(JsonUtil.toJsonString("init")).isNotNull();
  }

  @AfterAll
  static void tearDownSpring() {
    spring.close();
  }

  @BeforeEach
  void setUp() throws Exception {
    mapper = mock(TenantSettingInfoManageMapper.class);
    intentManageService = mock(IIntentManageService.class);
    dcParamCache = mock(DcParamCache.class);
    spring.when(() -> SpringUtil.getBean(DcParamCache.class)).thenReturn(dcParamCache);
    lenient().when(mapper.selectTenantSettingInfoList(anyLong())).thenReturn(Collections.emptyList());
    lenient().when(intentManageService.findStrategy(anyLong())).thenReturn(null);
    cache = new TenantSettingInfoCache(mapper, intentManageService);
    CacheTestSupport.disableCacheBackends(cache);
  }

  // ==================== load: parseSetting 各分支 ====================

  @Test
  void load_emptyList_returnsDefaults() {
    SimpleTenantSettingInfo info = cache.load("1");
    assertThat(info).isNotNull();
    assertThat(info.getLargeModelId()).isNull();
    assertThat(info.getIntentStrategy()).isNull();
    assertThat(info.getFlowLogEnabled()).isNull();
    verify(intentManageService).findStrategy(1L);
  }

  @Test
  void load_emptyFuncType_skipped() {
    when(mapper.selectTenantSettingInfoList(1L))
      .thenReturn(List.of(setting("", new TenantLargeModelSettingDTO())));
    SimpleTenantSettingInfo info = cache.load("1");
    assertThat(info.getLargeModelId()).isNull();
  }

  @Test
  void load_emptySettingInfo_skipped() {
    TenantSettingInfoDTO s = new TenantSettingInfoDTO();
    s.setFuncType(CommonConsts.FUNC_DEFAULT_TYPE_LARGE_MODEL);
    s.setSettingInfo("");
    when(mapper.selectTenantSettingInfoList(1L)).thenReturn(List.of(s));
    assertThat(cache.load("1").getLargeModelId()).isNull();
  }

  @Test
  void load_unknownFuncType_ignored() {
    when(mapper.selectTenantSettingInfoList(1L)).thenReturn(List.of(setting("chat_theme", new TenantLargeModelSettingDTO())));
    SimpleTenantSettingInfo info = cache.load("1");
    assertThat(info.getLargeModelId()).isNull();
    assertThat(info.getSecurityFlow()).isNull();
  }

  @Test
  void load_defaultLargeModel_setsLargeModelId() {
    TenantLargeModelSettingDTO payload = new TenantLargeModelSettingDTO();
    payload.setLargeModelId(99L);
    when(mapper.selectTenantSettingInfoList(1L))
      .thenReturn(List.of(setting(CommonConsts.FUNC_DEFAULT_TYPE_LARGE_MODEL, payload)));
    assertThat(cache.load("1").getLargeModelId()).isEqualTo(99L);
  }

  @Test
  void load_flowLog_enabledTrue() {
    TenantFlowLogSettingDTO payload = new TenantFlowLogSettingDTO();
    payload.setEnabled(Boolean.TRUE);
    when(mapper.selectTenantSettingInfoList(1L))
      .thenReturn(List.of(setting(CommonConsts.FUNC_TYPE_FLOW_LOG, payload)));
    assertThat(cache.load("1").getFlowLogEnabled()).isTrue();
  }

  @Test
  void load_flowLog_enabledFalse() {
    TenantFlowLogSettingDTO payload = new TenantFlowLogSettingDTO();
    payload.setEnabled(Boolean.FALSE);
    when(mapper.selectTenantSettingInfoList(1L))
      .thenReturn(List.of(setting(CommonConsts.FUNC_TYPE_FLOW_LOG, payload)));
    assertThat(cache.load("1").getFlowLogEnabled()).isFalse();
  }

  @Test
  void load_flowLog_enabledNull() {
    when(mapper.selectTenantSettingInfoList(1L))
      .thenReturn(List.of(setting(CommonConsts.FUNC_TYPE_FLOW_LOG, new TenantFlowLogSettingDTO())));
    assertThat(cache.load("1").getFlowLogEnabled()).isFalse();
  }

  @Test
  void load_suggestion_setsScoreAndLimit() {
    TenantSuggestionSettingDTO payload = new TenantSuggestionSettingDTO();
    payload.setScore(0.8);
    payload.setLimit(20);
    when(mapper.selectTenantSettingInfoList(1L))
      .thenReturn(List.of(setting(CommonConsts.FUNC_TYPE_SUGGESTION, payload)));
    SimpleTenantSettingInfo info = cache.load("1");
    assertThat(info.getSuggestionScore()).isEqualTo(0.8);
    assertThat(info.getSuggestionLimit()).isEqualTo(20);
  }

  @Test
  void load_pluginHub_setsPluginHub() {
    TenantPluginHubSettingDTO payload = new TenantPluginHubSettingDTO();
    payload.setPortalUserApiKey("portal-key");
    when(mapper.selectTenantSettingInfoList(1L))
      .thenReturn(List.of(setting(CommonConsts.FUNC_TYPE_PLUGIN_HUB, payload)));
    assertThat(cache.load("1").getPluginHub().getPortalUserApiKey()).isEqualTo("portal-key");
  }

  @Test
  void load_watermark_setsWatermark() {
    TenantWatermarkSettingDTO payload = new TenantWatermarkSettingDTO();
    when(mapper.selectTenantSettingInfoList(1L))
      .thenReturn(List.of(setting(CommonConsts.FUNC_TYPE_WATERMARK, payload)));
    assertThat(cache.load("1").getWatermark()).isNotNull();
  }

  @Test
  void load_security_setsSecurityFlow() {
    TenantSecurityFlowSettingDTO payload = new TenantSecurityFlowSettingDTO();
    payload.setUserInputFlowId(1L);
    payload.setLlmInputFlowId(2L);
    payload.setLlmOutputFlowId(3L);
    when(mapper.selectTenantSettingInfoList(1L))
      .thenReturn(List.of(setting(CommonConsts.FUNC_TYPE_SECURITY, payload)));
    SimpleTenantSettingInfo info = cache.load("1");
    assertThat(info.getSecurityFlow().getUserInputFlowId()).isEqualTo(1L);
    assertThat(info.getSecurityFlow().getLlmInputFlowId()).isEqualTo(2L);
    assertThat(info.getSecurityFlow().getLlmOutputFlowId()).isEqualTo(3L);
  }

  @Test
  void load_knowledgeOther_setsKnowledgeInfo() {
    KnowledgeInfoDTO payload = new KnowledgeInfoDTO();
    payload.setKnowledgeType("weknora");
    payload.setServiceUrl("http://svc");
    when(mapper.selectTenantSettingInfoList(1L))
      .thenReturn(List.of(setting(CommonConsts.FUNC_TYPE_KNOWLEDGE_OTHER, payload)));
    SimpleTenantSettingInfo info = cache.load("1");
    assertThat(info.getKnowledgeInfoDTO()).isNotNull();
    assertThat(info.getKnowledgeInfoDTO().getKnowledgeType()).isEqualTo("weknora");
  }

  @Test
  void load_knowledgeGraph_setsLoginPayload() {
    KnowledgeGraphLoginDTO payload = new KnowledgeGraphLoginDTO();
    payload.setProjectId("p1");
    when(mapper.selectTenantSettingInfoList(1L))
      .thenReturn(List.of(setting(CommonConsts.FUNC_TYPE_KNOWLEDGE_GRAPH, payload)));
    assertThat(cache.load("1").getKnowledgeGraphLoginPayload().getProjectId()).isEqualTo("p1");
  }

  // ==================== load: parseDocChainAccount ====================

  @Test
  void load_docChain_emptyPassword_skipsAes() {
    TenantDocChainAccountSettingDTO payload = new TenantDocChainAccountSettingDTO();
    payload.setUserName("u");
    payload.setPassword("");
    payload.setApiKey("k");
    when(mapper.selectTenantSettingInfoList(1L))
      .thenReturn(List.of(setting(CommonConsts.FUNC_TYPE_KNOWLEDGE, payload)));
    SimpleTenantSettingInfo info = cache.load("1");
    assertThat(info.getKnowledgeUserName()).isEqualTo("u");
    assertThat(info.getKnowledgeApiKey()).isEqualTo("k");
    assertThat(info.getKnowledgePassword()).isNull();
    verifyNoInteractions(dcParamCache);
  }

  @Test
  void load_docChain_aesPassword_decryptsAndBase64() {
    String encrypted = AesUtil.aesEncrypt("secret", AES_KEY);
    when(dcParamCache.getDcParamValByCode(anyString(), anyString())).thenReturn(AES_KEY);
    TenantDocChainAccountSettingDTO payload = new TenantDocChainAccountSettingDTO();
    payload.setUserName("u");
    payload.setPassword(encrypted);
    payload.setApiKey("k");
    when(mapper.selectTenantSettingInfoList(1L))
      .thenReturn(List.of(setting(CommonConsts.FUNC_TYPE_KNOWLEDGE, payload)));
    SimpleTenantSettingInfo info = cache.load("1");
    String expected = Base64.encodeBase64String("secret".getBytes(StandardCharsets.UTF_8));
    assertThat(info.getKnowledgePassword()).isEqualTo(expected);
    assertThat(info.getKnowledgeUserName()).isEqualTo("u");
    assertThat(info.getKnowledgeApiKey()).isEqualTo("k");
  }

  // ==================== getters ====================

  @Test
  void getModelId_set_returnsId() {
    TenantLargeModelSettingDTO payload = new TenantLargeModelSettingDTO();
    payload.setLargeModelId(7L);
    when(mapper.selectTenantSettingInfoList(1L))
      .thenReturn(List.of(setting(CommonConsts.FUNC_DEFAULT_TYPE_LARGE_MODEL, payload)));
    assertThat(cache.getModelId(1L)).isEqualTo(7L);
  }

  @Test
  void getModelId_unset_throws() {
    assertThatThrownBy(() -> cache.getModelId(1L))
      .isInstanceOf(RuntimeException.class)
      .hasMessageContaining("默认模型");
  }

  @Test
  void getModelIdOrNull_set_returnsId() {
    TenantLargeModelSettingDTO payload = new TenantLargeModelSettingDTO();
    payload.setLargeModelId(7L);
    when(mapper.selectTenantSettingInfoList(1L))
      .thenReturn(List.of(setting(CommonConsts.FUNC_DEFAULT_TYPE_LARGE_MODEL, payload)));
    assertThat(cache.getModelIdOrNull(1L)).isEqualTo(7L);
  }

  @Test
  void getModelIdOrNull_unset_returnsNull() {
    assertThat(cache.getModelIdOrNull(1L)).isNull();
  }

  @Test
  void getIntentStrategy_present_returnsStrategy() {
    IntentStrategyDTO strategy = new IntentStrategyDTO();
    strategy.setEmbeddingModelId(5L);
    when(intentManageService.findStrategy(1L)).thenReturn(strategy);
    assertThat(cache.getIntentStrategy(1L)).isSameAs(strategy);
  }

  @Test
  void getIntentStrategy_absent_returnsDefault() {
    when(intentManageService.findStrategy(1L)).thenReturn(null);
    IntentStrategyDTO strategy = cache.getIntentStrategy(1L);
    assertThat(strategy).isNotNull();
    assertThat(strategy.isEmbedding()).isFalse();
  }

  @Test
  void getRequiredIntentStrategy_enabled_returnsStrategy() {
    IntentStrategyDTO strategy = new IntentStrategyDTO();
    strategy.setEmbeddingEnabled(CommonConsts.TRUE);
    strategy.setEmbeddingModelId(5L);
    when(intentManageService.findStrategy(1L)).thenReturn(strategy);
    assertThat(cache.getRequiredIntentStrategy(1L)).isSameAs(strategy);
  }

  @Test
  void getRequiredIntentStrategy_notEnabled_throws() {
    IntentStrategyDTO strategy = new IntentStrategyDTO();
    strategy.setEmbeddingEnabled(null);
    when(intentManageService.findStrategy(1L)).thenReturn(strategy);
    assertThatThrownBy(() -> cache.getRequiredIntentStrategy(1L))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("意图向量化");
  }

  @Test
  void getRequiredIntentStrategy_noModel_throws() {
    IntentStrategyDTO strategy = new IntentStrategyDTO();
    strategy.setEmbeddingEnabled(CommonConsts.TRUE);
    strategy.setEmbeddingModelId(null);
    when(intentManageService.findStrategy(1L)).thenReturn(strategy);
    assertThatThrownBy(() -> cache.getRequiredIntentStrategy(1L))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("文本嵌入模型");
  }

  @Test
  void getSecurityFlowId_userInput() {
    setSecurityFlow();
    assertThat(cache.getSecurityFlowId(1L, BaseConsts.SECURITY_TYPE_USER_INPUT)).isEqualTo(1L);
  }

  @Test
  void getSecurityFlowId_llmInput() {
    setSecurityFlow();
    assertThat(cache.getSecurityFlowId(1L, BaseConsts.SECURITY_TYPE_LLM_INPUT)).isEqualTo(2L);
  }

  @Test
  void getSecurityFlowId_llmOutput() {
    setSecurityFlow();
    assertThat(cache.getSecurityFlowId(1L, BaseConsts.SECURITY_TYPE_LLM_OUTPUT)).isEqualTo(3L);
  }

  @Test
  void getSecurityFlowId_unknownType_defaultsUserInput() {
    setSecurityFlow();
    assertThat(cache.getSecurityFlowId(1L, "other")).isEqualTo(1L);
  }

  @Test
  void getSecurityFlowId_noFlow_returnsNull() {
    assertThat(cache.getSecurityFlowId(1L, BaseConsts.SECURITY_TYPE_USER_INPUT)).isNull();
  }

  @Test
  void getPluginApiKey_present() {
    TenantPluginHubSettingDTO payload = new TenantPluginHubSettingDTO();
    payload.setPortalUserApiKey("portal-key");
    when(mapper.selectTenantSettingInfoList(1L))
      .thenReturn(List.of(setting(CommonConsts.FUNC_TYPE_PLUGIN_HUB, payload)));
    assertThat(cache.getPluginApiKey(1L)).isEqualTo("portal-key");
  }

  @Test
  void getPluginApiKey_absent_returnsNull() {
    assertThat(cache.getPluginApiKey(1L)).isNull();
  }

  @Test
  void isFlowLogEnabled_true() {
    TenantFlowLogSettingDTO payload = new TenantFlowLogSettingDTO();
    payload.setEnabled(Boolean.TRUE);
    when(mapper.selectTenantSettingInfoList(1L))
      .thenReturn(List.of(setting(CommonConsts.FUNC_TYPE_FLOW_LOG, payload)));
    assertThat(cache.isFlowLogEnabled(1L)).isTrue();
  }

  @Test
  void isFlowLogEnabled_absent_returnsFalse() {
    assertThat(cache.isFlowLogEnabled(1L)).isFalse();
  }

  @Test
  void getSuggestionScore_present() {
    TenantSuggestionSettingDTO payload = new TenantSuggestionSettingDTO();
    payload.setScore(0.9);
    when(mapper.selectTenantSettingInfoList(1L))
      .thenReturn(List.of(setting(CommonConsts.FUNC_TYPE_SUGGESTION, payload)));
    assertThat(cache.getSuggestionScore(1L)).isEqualTo(0.9);
  }

  @Test
  void getSuggestionScore_absent_returnsDefault() {
    assertThat(cache.getSuggestionScore(1L)).isEqualTo(5.0);
  }

  @Test
  void getSuggestionLimit_present() {
    TenantSuggestionSettingDTO payload = new TenantSuggestionSettingDTO();
    payload.setLimit(20);
    when(mapper.selectTenantSettingInfoList(1L))
      .thenReturn(List.of(setting(CommonConsts.FUNC_TYPE_SUGGESTION, payload)));
    assertThat(cache.getSuggestionLimit(1L)).isEqualTo(20);
  }

  @Test
  void getSuggestionLimit_absent_returnsDefault() {
    assertThat(cache.getSuggestionLimit(1L)).isEqualTo(10);
  }

  @Test
  void getKnowledgeInfo_present() {
    KnowledgeInfoDTO payload = new KnowledgeInfoDTO();
    payload.setKnowledgeType("weknora");
    when(mapper.selectTenantSettingInfoList(1L))
      .thenReturn(List.of(setting(CommonConsts.FUNC_TYPE_KNOWLEDGE_OTHER, payload)));
    assertThat(cache.getKnowledgeInfo(1L).getKnowledgeType()).isEqualTo("weknora");
  }

  @Test
  void getKnowledgeInfo_absent_returnsDefault() {
    assertThat(cache.getKnowledgeInfo(1L)).isNotNull();
    assertThat(cache.getKnowledgeInfo(1L).getKnowledgeType()).isNull();
  }

  @Test
  void getKnowledgeGraphLoginPayload_present() {
    KnowledgeGraphLoginDTO payload = new KnowledgeGraphLoginDTO();
    payload.setProjectId("p1");
    when(mapper.selectTenantSettingInfoList(1L))
      .thenReturn(List.of(setting(CommonConsts.FUNC_TYPE_KNOWLEDGE_GRAPH, payload)));
    assertThat(cache.getKnowledgeGraphLoginPayload(1L).getProjectId()).isEqualTo("p1");
  }

  @Test
  void getKnowledgeGraphLoginPayload_absent_returnsDefault() {
    assertThat(cache.getKnowledgeGraphLoginPayload(1L)).isNotNull();
    assertThat(cache.getKnowledgeGraphLoginPayload(1L).getProjectId()).isNull();
  }

  // ==================== refresh / refreshLocalCache ====================

  @Test
  void refresh_emptyKeys_noop() throws Exception {
    enableBothBackends();
    cache.refresh(Collections.emptyList());
    verifyNoInteractions(cacheClient());
  }

  @Test
  void refresh_nonEmpty_deletesDistributedAndLocal() throws Exception {
    Cache<String, SimpleTenantSettingInfo> localCache = enableBothBackends();
    ICacheClient client = cacheClient();
    localCache.put("1", new SimpleTenantSettingInfo());
    assertThat(localCache.getIfPresent("1")).isNotNull();

    cache.refresh(List.of("1"));

    ArgumentCaptor<Collection<String>> captor = ArgumentCaptor.forClass(Collection.class);
    verify(client).delete(captor.capture());
    assertThat(captor.getValue()).containsExactly("1");
    assertThat(localCache.getIfPresent("1")).isNull();
  }

  @Test
  void refreshLocalCache_emptyKeys_noop() throws Exception {
    Cache<String, SimpleTenantSettingInfo> localCache = enableBothBackends();
    localCache.put("1", new SimpleTenantSettingInfo());
    cache.refreshLocalCache(Collections.emptyList());
    assertThat(localCache.getIfPresent("1")).isNotNull();
    verifyNoInteractions(cacheClient());
  }

  @Test
  void refreshLocalCache_nonEmpty_invalidatesLocalOnly() throws Exception {
    Cache<String, SimpleTenantSettingInfo> localCache = enableBothBackends();
    ICacheClient client = cacheClient();
    localCache.put("1", new SimpleTenantSettingInfo());

    cache.refreshLocalCache(List.of("1"));

    assertThat(localCache.getIfPresent("1")).isNull();
    verifyNoInteractions(client);
  }

  // ==================== helpers ====================

  private TenantSettingInfoDTO setting(String funcType, Object payload) {
    TenantSettingInfoDTO s = new TenantSettingInfoDTO();
    s.setFuncType(funcType);
    s.setSettingInfo(JsonUtil.toJsonString(payload));
    return s;
  }

  private void setSecurityFlow() {
    TenantSecurityFlowSettingDTO payload = new TenantSecurityFlowSettingDTO();
    payload.setUserInputFlowId(1L);
    payload.setLlmInputFlowId(2L);
    payload.setLlmOutputFlowId(3L);
    when(mapper.selectTenantSettingInfoList(1L))
      .thenReturn(List.of(setting(CommonConsts.FUNC_TYPE_SECURITY, payload)));
  }

  /** 开启分布式+本地缓存后端，注入 mock cacheClient 与真实 Guava localCache，返回 localCache 引用。 */
  private Cache<String, SimpleTenantSettingInfo> enableBothBackends() throws Exception {
    CacheTestSupport.setField(cache, "useDistributionCache", true);
    CacheTestSupport.setField(cache, "useLocalCache", true);
    CacheTestSupport.setField(cache, "useKeySetManager", false);
    ICacheClient client = mock(ICacheClient.class);
    Cache<String, SimpleTenantSettingInfo> localCache =
      CacheBuilder.newBuilder().maximumSize(100).build();
    CacheTestSupport.setField(cache, "cacheClient", client);
    CacheTestSupport.setField(cache, "localCache", localCache);
    return localCache;
  }

  private ICacheClient cacheClient() throws Exception {
    return (ICacheClient) field("cacheClient");
  }

  private Object field(String name) throws Exception {
    Class<?> cls = cache.getClass();
    while (cls != null) {
      try {
        java.lang.reflect.Field declared = cls.getDeclaredField(name);
        declared.setAccessible(true);
        return declared.get(cache);
      }
      catch (NoSuchFieldException e) {
        cls = cls.getSuperclass();
      }
    }
    throw new NoSuchFieldException(name);
  }
}
