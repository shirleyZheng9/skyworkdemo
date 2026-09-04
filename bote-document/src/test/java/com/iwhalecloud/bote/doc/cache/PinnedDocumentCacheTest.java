package com.iwhalecloud.bote.doc.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.tenant.TenantContextHolder;
import com.iwhalecloud.bote.doc.consts.DocCacheConsts;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.PinnedDocumentDTO;
import com.iwhalecloud.bote.doc.module.person.mapper.HomepageMapper;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * {@link PinnedDocumentCache} 单元测试。
 *
 * <p>继承 AbstractTenantCache&lt;List&lt;PinnedDocumentDTO&gt;&gt;，依赖 HomepageMapper（构造注入，mock 之）。
 * loadByKey 解析 "userId:spaceId" 并按租户查询；loadAll 全量加载返回空 Map。基类行为由
 * AbstractTenantCacheTest 覆盖。disableCacheBackends 使 get->load->loadByKey。TenantContextHolder 为
 * ThreadLocal，@BeforeEach 置入 / @AfterEach 清理。</p>
 */
class PinnedDocumentCacheTest {

  private PinnedDocumentCache cache;
  private HomepageMapper homepageMapper;

  @BeforeEach
  void setUp() throws Exception {
    homepageMapper = mock(HomepageMapper.class);
    cache = new PinnedDocumentCache(homepageMapper);
    ICacheClient cacheClient = mock(ICacheClient.class);
    CacheTestSupport.setField(cache, "cacheClient", cacheClient);
    CacheTestSupport.disableCacheBackends(cache);
    TenantContextHolder.setTenantId(10L);
  }

  @AfterEach
  void tearDown() {
    TenantContextHolder.clear();
  }

  @Test
  void getCacheName() {
    assertThat(cache.getCacheName()).isEqualTo(DocCacheConsts.CACHE_NAME_PINNED_DOCUMENT);
  }

  @Test
  void loadByKey_emptyKey_null() {
    assertThat(cache.loadByKey("")).isNull();
  }

  @Test
  void loadByKey_onePart_null() {
    assertThat(cache.loadByKey("abc")).isNull();
  }

  @Test
  void loadByKey_threeParts_null() {
    assertThat(cache.loadByKey("a:b:c")).isNull();
  }

  @Test
  void loadByKey_valid() {
    List<PinnedDocumentDTO> expected = new ArrayList<>();
    when(homepageMapper.selectPinnedDocuments(eq(1L), eq(2L), eq(DocBaseConsts.STATUS_CD_VALID), eq(10L)))
      .thenReturn(expected);
    assertThat(cache.loadByKey("1:2")).isSameAs(expected);
  }

  @Test
  void getPinnedDocuments_valid() {
    List<PinnedDocumentDTO> expected = new ArrayList<>();
    when(homepageMapper.selectPinnedDocuments(eq(1L), eq(2L), eq(DocBaseConsts.STATUS_CD_VALID), eq(10L)))
      .thenReturn(expected);
    assertThat(cache.getPinnedDocuments(1L, 2L)).isSameAs(expected);
  }

  @Test
  void getPinnedDocuments_nullResult() {
    when(homepageMapper.selectPinnedDocuments(eq(1L), eq(2L), eq(DocBaseConsts.STATUS_CD_VALID), eq(10L)))
      .thenReturn(null);
    assertThat(cache.getPinnedDocuments(1L, 2L)).isNull();
  }

  @Test
  void loadAll_nullConsumer_throws() {
    assertThatThrownBy(() -> cache.loadAll(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void loadAll_valid_acceptsEmptyMap() {
    AtomicBoolean called = new AtomicBoolean(false);
    cache.loadAll((Map<String, List<PinnedDocumentDTO>> m) -> called.set(true));
    assertThat(called).isTrue();
  }
}
