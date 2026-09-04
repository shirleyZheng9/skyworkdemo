package com.iwhalecloud.bote.doc.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.iwhalecloud.bote.doc.common.tenant.TenantContextHolder;
import com.iwhalecloud.bote.doc.module.control.base.ControlTemplate;
import com.iwhalecloud.bote.doc.module.control.base.permission.NodePermission;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentAttachmentDTO;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentAttachmentService;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * {@link DocumentAttachmentCache} 单元测试。
 *
 * <p>直接继承 BaseSecondaryCache&lt;DocumentAttachmentDTO&gt;，依赖 IDocumentAttachmentService +
 * ControlTemplate + DcDocumentNodeCache（构造注入，mock 之）。disableCacheBackends 使 get->load（默认
 * null，即缓存未命中）、put 为 noop。缓存命中分支用 spy 桩 get() 返回非空 DTO。TenantContextHolder 为
 * ThreadLocal，@AfterEach 清理避免泄漏。</p>
 */
class DocumentAttachmentCacheTest {

  private DocumentAttachmentCache cache;
  private IDocumentAttachmentService attachmentService;
  private ControlTemplate controlTemplate;
  private DcDocumentNodeCache dcDocumentNodeCache;

  @BeforeEach
  void setUp() throws Exception {
    attachmentService = mock(IDocumentAttachmentService.class);
    controlTemplate = mock(ControlTemplate.class);
    dcDocumentNodeCache = mock(DcDocumentNodeCache.class);
    cache = new DocumentAttachmentCache(attachmentService, controlTemplate, dcDocumentNodeCache);
    ICacheClient cacheClient = mock(ICacheClient.class);
    CacheTestSupport.setField(cache, "cacheClient", cacheClient);
    CacheTestSupport.disableCacheBackends(cache);
  }

  @AfterEach
  void tearDown() {
    TenantContextHolder.clear();
  }

  private DocumentAttachmentDTO attachmentInfo(Long tenantId, String documentId) {
    DocumentAttachmentDTO dto = new DocumentAttachmentDTO();
    dto.setAttachmentId(1L);
    dto.setTenantId(tenantId);
    dto.setDocumentId(documentId);
    return dto;
  }

  // ==================== cache hit ====================

  @Test
  void getAttachmentWithCheck_cacheHit_returnsCached() {
    DocumentAttachmentCache spy = spy(cache);
    DocumentAttachmentDTO cached = attachmentInfo(10L, "cached-doc");
    doReturn(cached).when(spy).get("1_1");
    DocumentAttachmentDTO result = spy.getAttachmentWithCheck(1L, 1L);
    assertThat(result).isSameAs(cached);
    verify(attachmentService, never()).getAttachmentInfo(anyLong());
  }

  // ==================== cache miss: attachment info null ====================

  @Test
  void getAttachmentWithCheck_attachmentInfoNull_returnsEmpty() {
    when(attachmentService.getAttachmentInfo(1L)).thenReturn(null);
    DocumentAttachmentDTO result = cache.getAttachmentWithCheck(1L, 1L, false);
    assertThat(result).isNotNull();
    assertThat(result.getDocumentId()).isNull();
  }

  // ==================== cache miss: blank libraryId ====================

  @Test
  void getAttachmentWithCheck_blankLibraryId_returnsEmpty() {
    when(attachmentService.getAttachmentInfo(1L)).thenReturn(attachmentInfo(10L, "doc1"));
    when(dcDocumentNodeCache.getLibraryIdByDocument("doc1")).thenReturn("");
    DocumentAttachmentDTO result = cache.getAttachmentWithCheck(1L, 1L, false);
    assertThat(result.getDocumentId()).isNull();
  }

  // ==================== cache miss: no permission (backend=false) ====================

  @Test
  void getAttachmentWithCheck_noPermission_returnsEmpty() {
    when(attachmentService.getAttachmentInfo(1L)).thenReturn(attachmentInfo(10L, "doc1"));
    when(dcDocumentNodeCache.getLibraryIdByDocument("doc1")).thenReturn("lib1");
    when(controlTemplate.hasNodePermission(eq("lib1"), eq(1L), eq("doc1"), eq(NodePermission.READ_NODE)))
      .thenReturn(false);
    DocumentAttachmentDTO result = cache.getAttachmentWithCheck(1L, 1L, false);
    assertThat(result.getDocumentId()).isNull();
  }

  // ==================== cache miss: has permission (backend=false) ====================

  @Test
  void getAttachmentWithCheck_hasPermission_returnsAttachmentInfo() {
    when(attachmentService.getAttachmentInfo(1L)).thenReturn(attachmentInfo(10L, "doc1"));
    when(dcDocumentNodeCache.getLibraryIdByDocument("doc1")).thenReturn("lib1");
    when(controlTemplate.hasNodePermission(eq("lib1"), eq(1L), eq("doc1"), eq(NodePermission.READ_NODE)))
      .thenReturn(true);
    DocumentAttachmentDTO result = cache.getAttachmentWithCheck(1L, 1L, false);
    assertThat(result.getDocumentId()).isEqualTo("doc1");
  }

  // ==================== backend=true skips permission ====================

  @Test
  void getAttachmentWithCheck_backendTrue_skipsPermission() {
    when(attachmentService.getAttachmentInfo(1L)).thenReturn(attachmentInfo(10L, "doc1"));
    when(dcDocumentNodeCache.getLibraryIdByDocument("doc1")).thenReturn("lib1");
    DocumentAttachmentDTO result = cache.getAttachmentWithCheck(1L, 1L, true);
    assertThat(result.getDocumentId()).isEqualTo("doc1");
    verify(controlTemplate, never()).hasNodePermission(any(), any(), any(), any());
  }

  // ==================== two-arg overload delegates ====================

  @Test
  void getAttachmentWithCheck_twoArg_delegatesToThreeArgWithBackendFalse() {
    when(attachmentService.getAttachmentInfo(1L)).thenReturn(attachmentInfo(10L, "doc1"));
    when(dcDocumentNodeCache.getLibraryIdByDocument("doc1")).thenReturn("lib1");
    when(controlTemplate.hasNodePermission(eq("lib1"), eq(1L), eq("doc1"), eq(NodePermission.READ_NODE)))
      .thenReturn(true);
    DocumentAttachmentDTO result = cache.getAttachmentWithCheck(1L, 1L);
    assertThat(result.getDocumentId()).isEqualTo("doc1");
    // 2 参重载走 backend=false，会校验权限
    verify(controlTemplate).hasNodePermission(eq("lib1"), eq(1L), eq("doc1"), eq(NodePermission.READ_NODE));
  }
}
