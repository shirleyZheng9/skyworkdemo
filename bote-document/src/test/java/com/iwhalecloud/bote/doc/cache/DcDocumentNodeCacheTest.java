package com.iwhalecloud.bote.doc.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.iwhalecloud.bote.doc.common.tenant.TenantContextHolder;
import com.iwhalecloud.bote.doc.module.control.model.NodeBaseInfoDTO;
import com.iwhalecloud.bote.doc.module.control.service.DocumentNodeService;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * {@link DcDocumentNodeCache} 单元测试。
 *
 * <p>继承 AbstractTenantCache&lt;NodeBaseInfoDTO&gt;，依赖 DocumentNodeService（构造注入，mock 之）。
 * 基类行为（租户键构建/get/put/delete）由 AbstractTenantCacheTest 覆盖，此处聚焦 loadByKey 委派与
 * getDocumentNodeInfo/getLibraryIdByDocument。disableCacheBackends 使 get->load->loadByKey。
 * TenantContextHolder 为 ThreadLocal，@BeforeEach 置入 / @AfterEach 清理。</p>
 */
class DcDocumentNodeCacheTest {

  private DcDocumentNodeCache cache;
  private DocumentNodeService documentNodeService;

  @BeforeEach
  void setUp() throws Exception {
    documentNodeService = mock(DocumentNodeService.class);
    cache = new DcDocumentNodeCache(documentNodeService);
    ICacheClient cacheClient = mock(ICacheClient.class);
    CacheTestSupport.setField(cache, "cacheClient", cacheClient);
    CacheTestSupport.disableCacheBackends(cache);
    TenantContextHolder.setTenantId(10L);
  }

  @AfterEach
  void tearDown() {
    TenantContextHolder.clear();
  }

  private NodeBaseInfoDTO nodeInfo(String libraryId) {
    NodeBaseInfoDTO n = new NodeBaseInfoDTO();
    n.setLibraryId(libraryId);
    return n;
  }

  @Test
  void getDocumentNodeInfo_valid() {
    when(documentNodeService.queryBaseInfo("doc1")).thenReturn(nodeInfo("lib1"));
    assertThat(cache.getDocumentNodeInfo("doc1").getLibraryId()).isEqualTo("lib1");
  }

  @Test
  void getDocumentNodeInfo_null() {
    when(documentNodeService.queryBaseInfo("doc1")).thenReturn(null);
    assertThat(cache.getDocumentNodeInfo("doc1")).isNull();
  }

  @Test
  void loadByKey_delegatesToQueryBaseInfo() {
    when(documentNodeService.queryBaseInfo("doc1")).thenReturn(nodeInfo("lib1"));
    assertThat(cache.loadByKey("doc1").getLibraryId()).isEqualTo("lib1");
    verify(documentNodeService).queryBaseInfo("doc1");
  }

  @Test
  void getLibraryIdByDocument_nullNodeInfo() {
    when(documentNodeService.queryBaseInfo("doc1")).thenReturn(null);
    assertThat(cache.getLibraryIdByDocument("doc1")).isNull();
  }

  @Test
  void getLibraryIdByDocument_valid() {
    when(documentNodeService.queryBaseInfo("doc1")).thenReturn(nodeInfo("lib1"));
    assertThat(cache.getLibraryIdByDocument("doc1")).isEqualTo("lib1");
  }
}
