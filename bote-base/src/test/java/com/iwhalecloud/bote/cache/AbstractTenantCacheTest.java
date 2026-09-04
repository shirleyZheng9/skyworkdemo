package com.iwhalecloud.bote.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.iwhalecloud.bss.litchi.cache.helper.keyset.IKeySetManager;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * {@link AbstractTenantCache} 单元测试。
 *
 * <p>以 TestTenantCache 子类为被测，反射注入真实 Guava localCache（预置 10:1/10:2/20:1）、mock
 * cacheClient 与 keySetManager，开启三个后端标志。覆盖 refresh/refreshLocalCache 的空、单租户
 * （numeric）走分布式+本地删除、非 numeric/多 key 走整批删除，以及关闭分布式/本地缓存的短路分支。</p>
 */
class AbstractTenantCacheTest {

  private TestTenantCache cache;
  private Cache<String, String> localCache;
  private ICacheClient cacheClient;
  private IKeySetManager keySetManager;

  @BeforeEach
  void setUp() throws Exception {
    cache = new TestTenantCache();
    localCache = CacheBuilder.newBuilder().build();
    localCache.put("10:1", "a");
    localCache.put("10:2", "b");
    localCache.put("20:1", "c");
    cacheClient = mock(ICacheClient.class);
    keySetManager = mock(IKeySetManager.class);
    CacheTestSupport.setField(cache, "localCache", localCache);
    CacheTestSupport.setField(cache, "cacheClient", cacheClient);
    CacheTestSupport.setField(cache, "keySetManager", keySetManager);
    CacheTestSupport.setField(cache, "useDistributionCache", true);
    CacheTestSupport.setField(cache, "useLocalCache", true);
    CacheTestSupport.setField(cache, "useKeySetManager", true);
  }

  private void assertLocalCacheKeys(String... expected) {
    assertThat(localCache.asMap().keySet()).containsExactlyInAnyOrder(expected);
  }

  // ==================== refresh ====================

  @Test
  void refresh_empty_isNoop() {
    cache.refresh(List.of());
    verifyNoInteractions(cacheClient, keySetManager);
    assertLocalCacheKeys("10:1", "10:2", "20:1");
  }

  @Test
  void refresh_null_isNoop() {
    cache.refresh(null);
    verifyNoInteractions(cacheClient, keySetManager);
    assertLocalCacheKeys("10:1", "10:2", "20:1");
  }

  @Test
  void refresh_singleTenant_deletesTenantKeysFromDistributedAndLocal() {
    Set<String> allKeys = new LinkedHashSet<>(List.of("10:1", "10:2", "20:1", "other"));
    when(keySetManager.getKeyList()).thenReturn(allKeys);
    when(cacheClient.delete(any(Collection.class))).thenReturn(2L);

    cache.refresh(List.of("10"));

    // 分布式：仅删除前缀为 10: 的 key
    org.mockito.ArgumentCaptor<Collection<String>> captor =
      org.mockito.ArgumentCaptor.forClass(Collection.class);
    verify(keySetManager).remove(captor.capture());
    assertThat(captor.getValue()).containsExactlyInAnyOrder("10:1", "10:2");
    verify(cacheClient).delete(any(Collection.class));
    // 本地：10:1/10:2 已失效，仅剩 20:1
    assertLocalCacheKeys("20:1");
  }

  @Test
  void refresh_singleTenant_noDistributedKeys_skipsDistributedDelete() {
    when(keySetManager.getKeyList()).thenReturn(new LinkedHashSet<>());

    cache.refresh(List.of("10"));

    verify(keySetManager, never()).remove(any(Collection.class));
    verify(cacheClient, never()).delete(any(Collection.class));
    // 本地删除仍执行
    assertLocalCacheKeys("20:1");
  }

  @Test
  void refresh_nonNumericKey_deletesExactKeys() {
    when(cacheClient.delete(any(Collection.class))).thenReturn(0L);

    cache.refresh(List.of("abc"));

    org.mockito.ArgumentCaptor<Collection<String>> captor =
      org.mockito.ArgumentCaptor.forClass(Collection.class);
    verify(cacheClient).delete(captor.capture());
    assertThat(captor.getValue()).containsExactly("abc");
    verify(keySetManager).remove(any(Collection.class));
    // 本地无 abc key，invalidateAll 不影响其余
    assertLocalCacheKeys("10:1", "10:2", "20:1");
  }

  @Test
  void refresh_multiKeys_deletesExactKeys() {
    when(cacheClient.delete(any(Collection.class))).thenReturn(0L);

    cache.refresh(List.of("10:1", "20:1"));

    org.mockito.ArgumentCaptor<Collection<String>> captor =
      org.mockito.ArgumentCaptor.forClass(Collection.class);
    verify(cacheClient).delete(captor.capture());
    assertThat(captor.getValue()).containsExactlyInAnyOrder("10:1", "20:1");
    // 本地 10:1/20:1 失效，仅剩 10:2
    assertLocalCacheKeys("10:2");
  }

  @Test
  void refresh_singleTenant_distDisabled_skipsDistributed() throws Exception {
    CacheTestSupport.setField(cache, "useDistributionCache", false);

    cache.refresh(List.of("10"));

    verifyNoInteractions(cacheClient, keySetManager);
    // 本地删除仍执行
    assertLocalCacheKeys("20:1");
  }

  // ==================== refreshLocalCache ====================

  @Test
  void refreshLocalCache_empty_isNoop() {
    cache.refreshLocalCache(List.of());
    verifyNoInteractions(cacheClient, keySetManager);
    assertLocalCacheKeys("10:1", "10:2", "20:1");
  }

  @Test
  void refreshLocalCache_singleTenant_invalidatesTenantLocalKeys() {
    cache.refreshLocalCache(List.of("10"));
    verifyNoInteractions(cacheClient, keySetManager);
    assertLocalCacheKeys("20:1");
  }

  @Test
  void refreshLocalCache_multiKeys_invalidatesExactLocalKeys() {
    cache.refreshLocalCache(List.of("10:1", "20:1"));
    assertLocalCacheKeys("10:2");
  }

  @Test
  void refreshLocalCache_singleTenant_localDisabled_isNoop() throws Exception {
    CacheTestSupport.setField(cache, "useLocalCache", false);

    cache.refreshLocalCache(List.of("10"));

    assertLocalCacheKeys("10:1", "10:2", "20:1");
  }

  /** 测试用租户缓存。 */
  static class TestTenantCache extends AbstractTenantCache<String> {
    TestTenantCache() {
      super("test-tenant:");
    }

    @Override
    public String getCacheName() {
      return "test-tenant";
    }

    @Override
    protected String load(String key) {
      return null;
    }
  }
}
