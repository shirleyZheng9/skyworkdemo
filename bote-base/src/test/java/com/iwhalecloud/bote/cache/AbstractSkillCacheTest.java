package com.iwhalecloud.bote.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.cache.CacheBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * {@link AbstractSkillCache} 单元测试。
 *
 * <p>以可控行为的 TestSkillCache 子类为被测。反射注入真实 Guava localCache、关闭分布式缓存与
 * keySetManager、开启本地缓存，使 get()->load()->putLocalCache() 路径完整可测。覆盖 load 的格式
 * 校验、get(Long,Long)、batchGet 的单 id/多 id/未命中/批量未实现各分支。</p>
 */
class AbstractSkillCacheTest {

  private TestSkillCache cache;

  @BeforeEach
  void setUp() throws Exception {
    cache = new TestSkillCache();
    CacheTestSupport.setField(cache, "useDistributionCache", false);
    CacheTestSupport.setField(cache, "useKeySetManager", false);
    CacheTestSupport.setField(cache, "useLocalCache", true);
    CacheTestSupport.setField(cache, "localCache", CacheBuilder.newBuilder().build());
  }

  // ==================== load(String) ====================

  @Test
  void load_validKey_delegatesToLoadById() {
    cache.byId.put(20L, "skill-20");
    assertThat(cache.load("10:20")).isEqualTo("skill-20");
  }

  @Test
  void load_nullKey_returnsNull() {
    assertThat(cache.load(null)).isNull();
  }

  @Test
  void load_singlePiece_returnsNull() {
    assertThat(cache.load("10")).isNull();
  }

  @Test
  void load_threePieces_returnsNull() {
    assertThat(cache.load("10:20:30")).isNull();
  }

  @Test
  void load_nonNumericTenant_returnsNull() {
    assertThat(cache.load("abc:20")).isNull();
  }

  @Test
  void load_nonNumericId_returnsNull() {
    assertThat(cache.load("10:abc")).isNull();
  }

  @Test
  void load_loadByIdMisses_returnsNull() {
    assertThat(cache.load("10:99")).isNull();
  }

  // ==================== get(Long, Long) ====================

  @Test
  void get_hit_returnsValue() {
    cache.byId.put(20L, "skill-20");
    assertThat(cache.get(10L, 20L)).isEqualTo("skill-20");
  }

  @Test
  void get_miss_returnsNull() {
    assertThat(cache.get(10L, 99L)).isNull();
  }

  // ==================== batchGet ====================

  @Test
  void batchGet_emptyIds_throws() {
    assertThatThrownBy(() -> cache.batchGet(10L, List.of()))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void batchGet_nullIds_throws() {
    assertThatThrownBy(() -> cache.batchGet(10L, null))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void batchGet_singleIdHit_returnsSingleton() {
    cache.byId.put(20L, "skill-20");
    assertThat(cache.batchGet(10L, List.of(20L))).containsExactly("skill-20");
  }

  @Test
  void batchGet_singleIdMiss_throws() {
    assertThatThrownBy(() -> cache.batchGet(10L, List.of(99L)))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("技能不存在");
  }

  @Test
  void batchGet_multiIdAllLoaded_returnsInOrder() {
    cache.byId.put(20L, "s20");
    cache.byId.put(30L, "s30");
    assertThat(cache.batchGet(10L, List.of(30L, 20L))).containsExactly("s30", "s20");
  }

  @Test
  void batchGet_multiIdPartialLoad_throws() {
    cache.byId.put(20L, "s20");
    // 30L 未提供，loadByIds 返回部分
    assertThatThrownBy(() -> cache.batchGet(10L, List.of(20L, 30L)))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("技能不存在");
  }

  @Test
  void batchGet_multiIdUsesLocalCacheOnSecondCall() {
    cache.byId.put(20L, "s20");
    cache.byId.put(30L, "s30");
    cache.batchGet(10L, List.of(20L, 30L));
    // 第二次调用前清空 byId，迫使走 localCache 命中而非 loadByIds
    cache.byId.clear();
    assertThat(cache.batchGet(10L, List.of(20L, 30L))).containsExactly("s20", "s30");
  }

  @Test
  void batchGet_multiIdBatchUnsupported_throws() throws Exception {
    NoBatchSkillCache noBatch = new NoBatchSkillCache();
    CacheTestSupport.setField(noBatch, "useDistributionCache", false);
    CacheTestSupport.setField(noBatch, "useKeySetManager", false);
    CacheTestSupport.setField(noBatch, "useLocalCache", true);
    CacheTestSupport.setField(noBatch, "localCache", CacheBuilder.newBuilder().build());

    assertThatThrownBy(() -> noBatch.batchGet(10L, List.of(20L, 30L)))
      .isInstanceOf(UnsupportedOperationException.class)
      .hasMessageContaining("不支持批量加载");
  }

  /** 可控 loadById/loadByIds 的测试用技能缓存。 */
  static class TestSkillCache extends AbstractSkillCache<String> {
    final Map<Long, String> byId = new HashMap<>();

    TestSkillCache() {
      super("test-skill:");
    }

    @Override
    protected String loadById(Long tenantId, Long id) {
      return byId.get(id);
    }

    @Override
    protected Map<Long, String> loadByIds(Long tenantId, List<Long> ids) {
      Map<Long, String> m = new HashMap<>();
      for (Long id : ids) {
        String v = byId.get(id);
        if (v != null) {
          m.put(id, v);
        }
      }
      return m;
    }

    @Override
    public String getCacheName() {
      return "test-skill";
    }
  }

  /** 不实现 loadByIds，使用默认抛 UnsupportedOperationException 的子类。 */
  static class NoBatchSkillCache extends AbstractSkillCache<String> {
    NoBatchSkillCache() {
      super("no-batch:");
    }

    @Override
    protected String loadById(Long tenantId, Long id) {
      return null;
    }

    @Override
    public String getCacheName() {
      return "no-batch";
    }
  }
}
