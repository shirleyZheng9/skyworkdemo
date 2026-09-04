package com.iwhalecloud.bote.doc.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwhalecloud.bote.doc.common.tenant.TenantContextHolder;
import com.iwhalecloud.bote.doc.consts.DocCacheConsts;
import com.iwhalecloud.bss.litchi.cache.helper.keyset.IKeySetManager;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshBroadcastService;
import com.iwhalecloud.bss.litchi.cache.refresh.command.RefreshCommand;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import com.google.common.cache.CacheBuilder;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.data.redis.core.ValueOperations;

/**
 * {@link AbstractTenantCache} 单元测试。
 *
 * <p>以 TestDocTenantCache 子类为被测，反射关闭 BaseSecondaryCache 后端，覆盖 key 构造/提取/校验、
 * get/put/delete/mget/exists 的 load 路径与分布式分支、getKeys/clearTenantCache 的租户过滤、
 * broadcastLocalCacheRefresh 的空/禁用/正常分支。TenantContextHolder 经 ThreadLocal 直接设置，
 * 无需 mockStatic。JsonUtil 静态初始化依赖 SpringUtil，@BeforeAll 注入真实 ObjectMapper。</p>
 */
class AbstractTenantCacheTest {

  private static MockedStatic<SpringUtil> spring;

  private TestDocTenantCache cache;
  private IRefreshBroadcastService refreshBroadcastService;

  @BeforeAll
  static void setUpSpring() {
    spring = mockStatic(SpringUtil.class);
    spring.when(() -> SpringUtil.getBean(eq(ObjectMapper.class), any()))
      .thenReturn(CacheTestSupport.jsonObjectMapper());
    // 在 mockStatic 生效且无未完成 stubbing 时触发 JsonUtil 静态初始化
    assertThat(JsonUtil.toJsonString("init")).isNotNull();
  }

  @AfterAll
  static void tearDownSpring() {
    spring.close();
  }

  @BeforeEach
  void setUp() throws Exception {
    cache = new TestDocTenantCache();
    TenantContextHolder.setTenantId(10L);
    refreshBroadcastService = mock(IRefreshBroadcastService.class);
    CacheTestSupport.setField(cache, "refreshBroadcastService", refreshBroadcastService);
    CacheTestSupport.disableCacheBackends(cache);
  }

  @AfterEach
  void clearTenant() {
    TenantContextHolder.clear();
  }

  // ==================== buildTenantCacheKey ====================

  @Test
  void buildTenantCacheKey_nullTenant_throwsIllegalArgument() {
    assertThatThrownBy(() -> cache.buildTenantCacheKey(null, "k"))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void buildTenantCacheKey_blankKey_throwsIllegalArgument() {
    assertThatThrownBy(() -> cache.buildTenantCacheKey(10L, "  "))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void buildTenantCacheKey_normal_prependsTenantPrefix() {
    assertThat(cache.buildTenantCacheKey(10L, "mykey")).isEqualTo("tenant:10:mykey");
  }

  @Test
  void buildTenantCacheKey_alreadyPrefixed_returnsAsIs() {
    String prefixed = "tenant:10:mykey";
    assertThat(cache.buildTenantCacheKey(10L, prefixed)).isSameAs(prefixed);
  }

  // ==================== extractOriginalKey ====================

  @Test
  void extractOriginalKey_blank_returnsAsIs() {
    assertThat(cache.extractOriginalKey("")).isEqualTo("");
    assertThat(cache.extractOriginalKey(null)).isNull();
  }

  @Test
  void extractOriginalKey_threeParts_returnsThird() {
    assertThat(cache.extractOriginalKey("tenant:10:mykey")).isEqualTo("mykey");
  }

  @Test
  void extractOriginalKey_moreParts_returnsRestAfterSecondSplit() {
    assertThat(cache.extractOriginalKey("tenant:10:a:b:c")).isEqualTo("a:b:c");
  }

  @Test
  void extractOriginalKey_lessThanThreeParts_returnsAsIs() {
    assertThat(cache.extractOriginalKey("tenant:10")).isEqualTo("tenant:10");
  }

  // ==================== buildTenantCacheKeys ====================

  @Test
  void buildTenantCacheKeys_empty_throwsIllegalArgument() {
    assertThatThrownBy(() -> cache.buildTenantCacheKeys(10L, List.of()))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void buildTenantCacheKeys_normal_mapsAll() {
    assertThat(cache.buildTenantCacheKeys(10L, List.of("a", "b")))
      .containsExactly("tenant:10:a", "tenant:10:b");
  }

  // ==================== validate* ====================

  @Test
  void validateTenantId_null_throwsIllegalArgument() {
    assertThatThrownBy(() -> cache.validateTenantId(null))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void validateKey_blank_throwsIllegalArgument() {
    assertThatThrownBy(() -> cache.validateKey(""))
      .isInstanceOf(IllegalArgumentException.class);
  }

  // ==================== getCacheName / load ====================

  @Test
  void getCacheName_returnsConstant() {
    assertThat(cache.getCacheName()).isEqualTo("test-cache");
  }

  @Test
  void load_extractsOriginalKeyAndDelegatesToLoadByKey() {
    cache.loaderResults.put("mykey", "V");
    assertThat(cache.load("tenant:10:mykey")).isEqualTo("V");
  }

  // ==================== get ====================

  @Test
  void get_noTenant_throwsNpe() {
    TenantContextHolder.clear();
    assertThatThrownBy(() -> cache.get("k")).isInstanceOf(NullPointerException.class);
  }

  @Test
  void get_blankKey_throwsIllegalArgument() {
    assertThatThrownBy(() -> cache.get("  ")).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void get_loadHit_returnsValue() {
    cache.loaderResults.put("k", "V");
    assertThat(cache.get("k")).isEqualTo("V");
  }

  @Test
  void get_loadMiss_returnsNull() {
    assertThat(cache.get("missing")).isNull();
  }

  // ==================== mget ====================

  @Test
  void mget_empty_throwsIllegalArgument() {
    assertThatThrownBy(() -> cache.mget(List.of())).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void mget_singleKey_returnsMappedValue() {
    cache.loaderResults.put("a", "VA");
    Map<String, String> result = cache.mget(List.of("a"));
    assertThat(result).containsEntry("a", "VA");
  }

  // ==================== put ====================

  @Test
  void put_nullValue_throwsIllegalArgument() {
    assertThatThrownBy(() -> cache.put("k", null))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void put_distributionDisabled_skipsRedisAndBroadcast() {
    assertThat(cache.put("k", "v")).isTrue();
    verifyNoInteractions(refreshBroadcastService);
  }

  @Test
  void put_distributionEnabled_writesRedisAndKeySet() throws Exception {
    ICacheClient client = mock(ICacheClient.class);
    ValueOperations<String, String> valueOps = mock(ValueOperations.class);
    IKeySetManager keySetManager = mock(IKeySetManager.class);
    when(client.opsForValue()).thenReturn(valueOps);
    CacheTestSupport.setField(cache, "cacheClient", client);
    CacheTestSupport.setField(cache, "keySetManager", keySetManager);
    CacheTestSupport.setField(cache, "useDistributionCache", true);
    CacheTestSupport.setField(cache, "useKeySetManager", true);

    assertThat(cache.put("k", "v")).isTrue();

    verify(valueOps).set(eq("k"), anyString());
    verify(keySetManager).addIfAbsent("k");
  }

  @Test
  void putWithExpire_distributionEnabled_writesRedisWithTtl() throws Exception {
    ICacheClient client = mock(ICacheClient.class);
    ValueOperations<String, String> valueOps = mock(ValueOperations.class);
    IKeySetManager keySetManager = mock(IKeySetManager.class);
    when(client.opsForValue()).thenReturn(valueOps);
    CacheTestSupport.setField(cache, "cacheClient", client);
    CacheTestSupport.setField(cache, "keySetManager", keySetManager);
    CacheTestSupport.setField(cache, "useDistributionCache", true);
    CacheTestSupport.setField(cache, "useKeySetManager", true);

    assertThat(cache.put("k", "v", 30)).isTrue();

    verify(valueOps).set(eq("k"), anyString(), eq(30L), eq(TimeUnit.SECONDS));
    verify(keySetManager).addIfAbsent("k");
  }

  // ==================== delete ====================

  @Test
  void delete_distributionDisabled_returnsTrue() {
    assertThat(cache.delete("k")).isTrue();
    verifyNoInteractions(refreshBroadcastService);
  }

  @Test
  void delete_localEnabled_broadcasts() throws Exception {
    CacheTestSupport.setField(cache, "useLocalCache", true);
    CacheTestSupport.setField(cache, "localCache", CacheBuilder.newBuilder().build());
    cache.delete("k");
    verify(refreshBroadcastService).broadcast(any(RefreshCommand.class));
  }

  // ==================== broadcastLocalCacheRefresh ====================

  @Test
  void broadcastLocalCacheRefresh_emptyKeys_noop() throws Exception {
    CacheTestSupport.setField(cache, "useLocalCache", true);
    cache.broadcastLocalCacheRefresh("test-cache", List.of());
    verifyNoInteractions(refreshBroadcastService);
  }

  @Test
  void broadcastLocalCacheRefresh_localDisabled_noop() {
    cache.broadcastLocalCacheRefresh("test-cache", List.of("tenant:10:k"));
    verifyNoInteractions(refreshBroadcastService);
  }

  @Test
  void broadcastLocalCacheRefresh_localEnabled_broadcasts() throws Exception {
    CacheTestSupport.setField(cache, "useLocalCache", true);
    cache.broadcastLocalCacheRefresh("test-cache", List.of("tenant:10:k"));
    verify(refreshBroadcastService).broadcast(any(RefreshCommand.class));
  }

  // ==================== getKeys ====================

  @Test
  void getKeys_distributionDisabled_returnsEmpty() {
    assertThat(cache.getKeys(10L)).isEmpty();
  }

  @Test
  void getKeys_distributionEnabled_filtersTenantKeys() throws Exception {
    IKeySetManager keySetManager = mock(IKeySetManager.class);
    CacheTestSupport.setField(cache, "keySetManager", keySetManager);
    CacheTestSupport.setField(cache, "useDistributionCache", true);
    CacheTestSupport.setField(cache, "useKeySetManager", true);
    when(keySetManager.getKeyList()).thenReturn(new LinkedHashSet<>(
      Set.of("tenant:10:a", "tenant:10:b", "tenant:20:c")));

    assertThat(cache.getKeys(10L)).containsExactlyInAnyOrder("tenant:10:a", "tenant:10:b");
  }

  // ==================== clearTenantCache ====================

  @Test
  void clearTenantCache_nullSpaceId_returnsZero() {
    assertThat(cache.clearTenantCache(10L, null)).isZero();
  }

  @Test
  void clearTenantCache_emptyKeys_returnsZero() {
    assertThat(cache.clearTenantCache(10L, 100L)).isZero();
  }

  @Test
  void clearTenantCache_matchingSpaceId_deletesAndBroadcasts() throws Exception {
    IKeySetManager keySetManager = mock(IKeySetManager.class);
    ICacheClient client = mock(ICacheClient.class);
    CacheTestSupport.setField(cache, "keySetManager", keySetManager);
    CacheTestSupport.setField(cache, "cacheClient", client);
    CacheTestSupport.setField(cache, "useDistributionCache", true);
    CacheTestSupport.setField(cache, "useKeySetManager", true);
    CacheTestSupport.setField(cache, "useLocalCache", true);
    CacheTestSupport.setField(cache, "localCache", CacheBuilder.newBuilder().build());
    when(keySetManager.getKeyList()).thenReturn(new LinkedHashSet<>(
      Set.of("tenant:10:u1:100", "tenant:10:u2:200", "tenant:10:u3:100")));
    when(client.delete("tenant:10:u1:100")).thenReturn(true);
    when(client.delete("tenant:10:u3:100")).thenReturn(true);

    assertThat(cache.clearTenantCache(10L, 100L)).isEqualTo(2);

    verify(client).delete("tenant:10:u1:100");
    verify(client).delete("tenant:10:u3:100");
    verify(refreshBroadcastService).broadcast(any(RefreshCommand.class));
  }

  @Test
  void clearTenantCache_noMatchingSpaceId_returnsZero() throws Exception {
    IKeySetManager keySetManager = mock(IKeySetManager.class);
    CacheTestSupport.setField(cache, "keySetManager", keySetManager);
    CacheTestSupport.setField(cache, "useDistributionCache", true);
    CacheTestSupport.setField(cache, "useKeySetManager", true);
    when(keySetManager.getKeyList()).thenReturn(new LinkedHashSet<>(Set.of("tenant:10:u1:200")));

    assertThat(cache.clearTenantCache(10L, 100L)).isZero();
    verifyNoInteractions(refreshBroadcastService);
  }

  @Test
  void clearTenantCache_unparseableSpaceId_filteredOut() throws Exception {
    IKeySetManager keySetManager = mock(IKeySetManager.class);
    CacheTestSupport.setField(cache, "keySetManager", keySetManager);
    CacheTestSupport.setField(cache, "useDistributionCache", true);
    CacheTestSupport.setField(cache, "useKeySetManager", true);
    when(keySetManager.getKeyList()).thenReturn(new LinkedHashSet<>(Set.of("tenant:10:u1:abc")));

    assertThat(cache.clearTenantCache(10L, 100L)).isZero();
  }

  // ==================== exists ====================

  @Test
  void exists_loadHit_returnsTrue() {
    cache.loaderResults.put("k", "V");
    assertThat(cache.exists(10L, "k")).isTrue();
  }

  @Test
  void exists_loadMiss_returnsFalse() {
    assertThat(cache.exists(10L, "missing")).isFalse();
  }

  /** 测试用租户缓存。 */
  static class TestDocTenantCache extends AbstractTenantCache<String> {
    final Map<String, String> loaderResults = new java.util.HashMap<>();

    TestDocTenantCache() {
      super(DocCacheConsts.GROUP_DOC, "test:");
    }

    @Override
    public String getCacheName() {
      return "test-cache";
    }

    @Override
    protected String loadByKey(String key) {
      return loaderResults.get(key);
    }
  }
}
