package com.iwhalecloud.bote.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

/**
 * {@link TenantCacheMarker} 默认方法单元测试。
 *
 * <p>以 CALLS_REAL_METHODS 的 mock 触发接口默认方法实现。覆盖 buildTenantCacheKey 直返、
 * invalidateLocalCache 的单租户（按 left 过滤）与多 key（按 "tenantId:id" 精确失效）及格式非法短路。</p>
 */
class TenantCacheMarkerTest {

  private final TenantCacheMarker marker = mock(TenantCacheMarker.class, Answers.CALLS_REAL_METHODS);

  private Cache<Pair<Long, Long>, String> newCache() {
    Cache<Pair<Long, Long>, String> cache = CacheBuilder.newBuilder().build();
    cache.put(Pair.of(10L, 1L), "a");
    cache.put(Pair.of(10L, 2L), "b");
    cache.put(Pair.of(20L, 1L), "c");
    return cache;
  }

  @Test
  void buildTenantCacheKey_returnsTenantIdAsIs() {
    assertThat(marker.buildTenantCacheKey("42")).isEqualTo("42");
  }

  @Test
  void invalidateLocalCache_singleTenant_invalidatesAllPairsOfTenant() {
    Cache<Pair<Long, Long>, String> cache = newCache();
    marker.invalidateLocalCache(cache, List.of("10"));
    assertThat(cache.asMap().keySet()).containsExactly(Pair.of(20L, 1L));
  }

  @Test
  void invalidateLocalCache_singleTenantNoMatch_isNoop() {
    Cache<Pair<Long, Long>, String> cache = newCache();
    marker.invalidateLocalCache(cache, List.of("99"));
    assertThat(cache.asMap()).hasSize(3);
  }

  @Test
  void invalidateLocalCache_multiKeys_invalidatesExactPairs() {
    Cache<Pair<Long, Long>, String> cache = newCache();
    marker.invalidateLocalCache(cache, List.of("10:1", "20:1"));
    assertThat(cache.asMap().keySet()).containsExactly(Pair.of(10L, 2L));
  }

  @Test
  void invalidateLocalCache_malformedKey_isSkipped() {
    Cache<Pair<Long, Long>, String> cache = newCache();
    // "abc" 非数字、单段：走 else 分支，split 后 len!=1，跳过
    marker.invalidateLocalCache(cache, List.of("abc"));
    assertThat(cache.asMap()).hasSize(3);
  }

  @Test
  void invalidateLocalCache_threePieceKey_isSkipped() {
    Cache<Pair<Long, Long>, String> cache = newCache();
    marker.invalidateLocalCache(cache, List.of("10:1:2"));
    assertThat(cache.asMap()).hasSize(3);
  }

  @Test
  void invalidateLocalCache_nonNumericPair_isSkipped() {
    Cache<Pair<Long, Long>, String> cache = newCache();
    marker.invalidateLocalCache(cache, List.of("10:x"));
    assertThat(cache.asMap()).hasSize(3);
  }

  @Test
  void invalidateLocalCache_emptyKeys_isNoop() {
    Cache<Pair<Long, Long>, String> cache = newCache();
    marker.invalidateLocalCache(cache, List.of());
    assertThat(cache.asMap()).hasSize(3);
  }
}
