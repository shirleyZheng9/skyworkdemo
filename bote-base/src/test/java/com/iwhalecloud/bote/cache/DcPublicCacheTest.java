package com.iwhalecloud.bote.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.entity.base.DcPublicEntity;
import com.iwhalecloud.bote.mapper.base.DcPublicQueryMapper;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * {@link DcPublicCache} 单元测试。
 *
 * <p>反射关闭 BaseSecondaryCache 后端，使 get()->load()->put() 仅经 mapper。覆盖 load 的空/非数字/
 * 合法分支、loadAll 的空/非空分组、getDcPublicList/getDcPublic/getCodea 的命中与未命中。</p>
 */
class DcPublicCacheTest {

  private DcPublicQueryMapper mapper;
  private DcPublicCache cache;

  @BeforeEach
  void setUp() throws Exception {
    mapper = mock(DcPublicQueryMapper.class);
    cache = new DcPublicCache(mapper);
    CacheTestSupport.disableCacheBackends(cache);
  }

  @Test
  void getCacheName_returnsConstant() {
    assertThat(cache.getCacheName()).isEqualTo(CacheConsts.CACHE_NAME_DC_PUBLIC);
  }

  // ==================== load ====================

  @Test
  void load_emptyStype_returnsNull() {
    assertThat(cache.load("")).isNull();
  }

  @Test
  void load_nonNumericStype_returnsNull() {
    assertThat(cache.load("abc")).isNull();
  }

  @Test
  void load_validStype_delegatesToMapper() {
    List<DcPublicEntity> list = List.of(mock(DcPublicEntity.class));
    when(mapper.selectDcPublicListByStype(1L)).thenReturn(list);
    assertThat(cache.load("1")).isSameAs(list);
  }

  // ==================== loadAll ====================

  @Test
  void loadAll_emptyList_acceptsEmptyMap() {
    when(mapper.selectDcPublicList()).thenReturn(Collections.emptyList());
    Map<String, List<DcPublicEntity>> captured = new HashMap<>();
    cache.loadAll((Consumer<Map<String, List<DcPublicEntity>>>) captured::putAll);
    assertThat(captured).isEmpty();
  }

  @Test
  void loadAll_nullList_acceptsEmptyMap() {
    when(mapper.selectDcPublicList()).thenReturn(null);
    Map<String, List<DcPublicEntity>> captured = new HashMap<>();
    cache.loadAll((Consumer<Map<String, List<DcPublicEntity>>>) captured::putAll);
    assertThat(captured).isEmpty();
  }

  @Test
  void loadAll_nonEmptyList_groupedByStype() {
    DcPublicEntity a = mock(DcPublicEntity.class);
    DcPublicEntity b = mock(DcPublicEntity.class);
    DcPublicEntity c = mock(DcPublicEntity.class);
    when(a.getStype()).thenReturn(1L);
    when(b.getStype()).thenReturn(2L);
    when(c.getStype()).thenReturn(1L);
    when(mapper.selectDcPublicList()).thenReturn(List.of(a, b, c));

    Map<String, List<DcPublicEntity>> captured = new HashMap<>();
    cache.loadAll((Consumer<Map<String, List<DcPublicEntity>>>) captured::putAll);

    assertThat(captured).hasSize(2).containsOnlyKeys("1", "2");
    assertThat(captured.get("1")).containsExactly(a, c);
    assertThat(captured.get("2")).containsExactly(b);
  }

  @Test
  void loadAll_nullConsumer_throwsNpe() {
    assertThatThrownBy(() -> cache.loadAll(null)).isInstanceOf(NullPointerException.class);
  }

  // ==================== getDcPublicList / getDcPublic / getCodea ====================

  @Test
  void getDcPublicList_emptyStype_returnsNull() {
    assertThat(cache.getDcPublicList("")).isNull();
  }

  @Test
  void getDcPublicList_hit_returnsList() {
    List<DcPublicEntity> list = List.of(mock(DcPublicEntity.class));
    when(mapper.selectDcPublicListByStype(1L)).thenReturn(list);
    assertThat(cache.getDcPublicList("1")).isSameAs(list);
  }

  @Test
  void getDcPublic_hit_returnsFirst() {
    DcPublicEntity first = mock(DcPublicEntity.class);
    DcPublicEntity second = mock(DcPublicEntity.class);
    when(mapper.selectDcPublicListByStype(1L)).thenReturn(List.of(first, second));
    assertThat(cache.getDcPublic("1")).isSameAs(first);
  }

  @Test
  void getDcPublic_emptyList_returnsNull() {
    when(mapper.selectDcPublicListByStype(1L)).thenReturn(Collections.emptyList());
    assertThat(cache.getDcPublic("1")).isNull();
  }

  @Test
  void getCodea_hit_returnsCodea() {
    DcPublicEntity entity = mock(DcPublicEntity.class);
    when(entity.getCodea()).thenReturn("CA");
    when(mapper.selectDcPublicListByStype(1L)).thenReturn(List.of(entity));
    assertThat(cache.getCodea("1")).isEqualTo("CA");
  }

  @Test
  void getCodea_miss_returnsNull() {
    when(mapper.selectDcPublicListByStype(1L)).thenReturn(Collections.emptyList());
    assertThat(cache.getCodea("1")).isNull();
  }
}
