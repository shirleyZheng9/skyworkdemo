package com.iwhalecloud.bote.doc.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.iwhalecloud.bote.doc.common.tenant.TenantContextHolder;
import com.iwhalecloud.bote.doc.module.library.dto.DocumentLibraryDTO;
import com.iwhalecloud.bote.doc.module.library.service.DocumentLibraryService;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * {@link DocumentLibraryCache} 单元测试。
 *
 * <p>继承 AbstractTenantCache&lt;DocumentLibraryDTO&gt;，依赖 DocumentLibraryService（构造注入，mock 之）。
 * 基类行为由 AbstractTenantCacheTest 覆盖，此处聚焦 loadByKey 委派、getByLibraryId/
 * getLibraryName/deleteLibraryCache。disableCacheBackends 使 get->load->loadByKey、delete 为 noop。
 * TenantContextHolder 为 ThreadLocal，@BeforeEach 置入 / @AfterEach 清理。</p>
 */
class DocumentLibraryCacheTest {

  private DocumentLibraryCache cache;
  private DocumentLibraryService documentLibraryService;

  @BeforeEach
  void setUp() throws Exception {
    documentLibraryService = mock(DocumentLibraryService.class);
    cache = new DocumentLibraryCache(documentLibraryService);
    ICacheClient cacheClient = mock(ICacheClient.class);
    CacheTestSupport.setField(cache, "cacheClient", cacheClient);
    CacheTestSupport.disableCacheBackends(cache);
    TenantContextHolder.setTenantId(10L);
  }

  @AfterEach
  void tearDown() {
    TenantContextHolder.clear();
  }

  private DocumentLibraryDTO library(String libraryName) {
    DocumentLibraryDTO dto = new DocumentLibraryDTO();
    dto.setLibraryName(libraryName);
    return dto;
  }

  @Test
  void getByLibraryId_valid() {
    DocumentLibraryDTO dto = library("MyLib");
    when(documentLibraryService.findByLibraryId("lib1")).thenReturn(dto);
    assertThat(cache.getByLibraryId("lib1")).isSameAs(dto);
  }

  @Test
  void getByLibraryId_null() {
    when(documentLibraryService.findByLibraryId("lib1")).thenReturn(null);
    assertThat(cache.getByLibraryId("lib1")).isNull();
  }

  @Test
  void loadByKey_delegatesToFindByLibraryId() {
    when(documentLibraryService.findByLibraryId("lib1")).thenReturn(library("MyLib"));
    assertThat(cache.loadByKey("lib1").getLibraryName()).isEqualTo("MyLib");
    verify(documentLibraryService).findByLibraryId("lib1");
  }

  @Test
  void deleteLibraryCache_delegatesToDelete() {
    DocumentLibraryCache spy = spy(cache);
    spy.deleteLibraryCache("lib1");
    verify(spy).delete(anyString());
  }

  @Test
  void getLibraryName_null() {
    when(documentLibraryService.findByLibraryId("lib1")).thenReturn(null);
    assertThat(cache.getLibraryName("lib1")).isNull();
  }

  @Test
  void getLibraryName_valid() {
    when(documentLibraryService.findByLibraryId("lib1")).thenReturn(library("MyLib"));
    assertThat(cache.getLibraryName("lib1")).isEqualTo("MyLib");
  }
}
