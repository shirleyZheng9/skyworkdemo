package com.iwhalecloud.bote.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.dto.base.SimpleDcParamDTO;
import com.iwhalecloud.bote.mapper.base.DcParamQueryMapper;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * {@link DcParamCache} 单元测试。
 *
 * <p>反射关闭 BaseSecondaryCache 的 useDistributionCache/useLocalCache/useKeySetManager，
 * 使 get()->load()->put() 路径不触碰 Redis/本地缓存/keySetManager，仅经 mapper 加载，
 * 从而以纯单测覆盖 load/loadAll/getDcParamValByCode 各分支。</p>
 */
class DcParamCacheTest {

  private DcParamQueryMapper mapper;
  private DcParamCache cache;

  @BeforeEach
  void setUp() throws Exception {
    mapper = mock(DcParamQueryMapper.class);
    cache = new DcParamCache(mapper);
    CacheTestSupport.disableCacheBackends(cache);
  }

  @Test
  void getCacheName_returnsConstant() {
    assertThat(cache.getCacheName()).isEqualTo(CacheConsts.CACHE_NAME_DC_PARAM);
  }

  // ==================== load ====================

  @Test
  void load_emptyKey_returnsNull() {
    assertThat(cache.load("")).isNull();
  }

  @Test
  void load_nullKey_returnsNull() {
    assertThat(cache.load(null)).isNull();
  }

  @Test
  void load_delegatesToMapper() {
    SimpleDcParamDTO dto = mock(SimpleDcParamDTO.class);
    when(mapper.findDcParamByCode("CODE")).thenReturn(dto);
    assertThat(cache.load("CODE")).isSameAs(dto);
  }

  @Test
  void load_mapperReturnsNull_returnsNull() {
    when(mapper.findDcParamByCode("CODE")).thenReturn(null);
    assertThat(cache.load("CODE")).isNull();
  }

  // ==================== loadAll ====================

  @Test
  void loadAll_emptyList_acceptsEmptyMap() {
    when(mapper.selectDcParam()).thenReturn(Collections.emptyList());
    Map<String, SimpleDcParamDTO> captured = new HashMap<>();
    cache.loadAll((Consumer<Map<String, SimpleDcParamDTO>>) captured::putAll);
    assertThat(captured).isEmpty();
  }

  @Test
  void loadAll_nullList_acceptsEmptyMap() {
    when(mapper.selectDcParam()).thenReturn(null);
    Map<String, SimpleDcParamDTO> captured = new HashMap<>();
    cache.loadAll((Consumer<Map<String, SimpleDcParamDTO>>) captured::putAll);
    assertThat(captured).isEmpty();
  }

  @Test
  void loadAll_nonEmptyList_groupedByParamCode() {
    SimpleDcParamDTO a = mock(SimpleDcParamDTO.class);
    SimpleDcParamDTO b = mock(SimpleDcParamDTO.class);
    when(a.getParamCode()).thenReturn("A");
    when(b.getParamCode()).thenReturn("B");
    when(mapper.selectDcParam()).thenReturn(List.of(a, b));

    Map<String, SimpleDcParamDTO> captured = new HashMap<>();
    cache.loadAll((Consumer<Map<String, SimpleDcParamDTO>>) captured::putAll);

    assertThat(captured).hasSize(2).containsEntry("A", a).containsEntry("B", b);
  }

  @Test
  void loadAll_nullConsumer_throwsNpe() {
    assertThatThrownBy(() -> cache.loadAll(null)).isInstanceOf(NullPointerException.class);
  }

  // ==================== getDcParamValByCode ====================

  @Test
  void getDcParamValByCode_hit_returnsParamVal() {
    SimpleDcParamDTO dto = mock(SimpleDcParamDTO.class);
    when(dto.getParamVal()).thenReturn("v1");
    when(mapper.findDcParamByCode("CODE")).thenReturn(dto);
    assertThat(cache.getDcParamValByCode("CODE")).isEqualTo("v1");
  }

  @Test
  void getDcParamValByCode_miss_returnsNull() {
    when(mapper.findDcParamByCode("CODE")).thenReturn(null);
    assertThat(cache.getDcParamValByCode("CODE")).isNull();
  }

  @Test
  void getDcParamValByCode_withDefault_hit_returnsParamVal() {
    SimpleDcParamDTO dto = mock(SimpleDcParamDTO.class);
    when(dto.getParamVal()).thenReturn("v1");
    when(mapper.findDcParamByCode("CODE")).thenReturn(dto);
    assertThat(cache.getDcParamValByCode("CODE", "default")).isEqualTo("v1");
  }

  @Test
  void getDcParamValByCode_withDefault_miss_returnsDefault() {
    when(mapper.findDcParamByCode("CODE")).thenReturn(null);
    assertThat(cache.getDcParamValByCode("CODE", "default")).isEqualTo("default");
  }

  @Test
  void getDcParamValByCode_withDefault_emptyVal_returnsDefault() {
    SimpleDcParamDTO dto = mock(SimpleDcParamDTO.class);
    when(dto.getParamVal()).thenReturn("");
    when(mapper.findDcParamByCode("CODE")).thenReturn(dto);
    assertThat(cache.getDcParamValByCode("CODE", "default")).isEqualTo("default");
  }
}
