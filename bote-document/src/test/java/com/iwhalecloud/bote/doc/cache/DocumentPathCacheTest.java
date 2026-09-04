package com.iwhalecloud.bote.doc.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwhalecloud.bote.doc.module.control.model.NodeBaseInfoDTO;
import com.iwhalecloud.bote.doc.module.control.service.DocumentNodeService;
import com.iwhalecloud.bote.doc.module.document.dto.NodePathDTO;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import com.google.common.cache.CacheBuilder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/**
 * {@link DocumentPathCache} 单元测试。
 *
 * <p>直接继承 BaseSecondaryCache&lt;List&lt;NodePathDTO&gt;&gt;，依赖 DocumentNodeService +
 * DcDocumentNodeCache（构造注入，mock 之）。getPath/load/mget 经 super.get()/super.mget()：默认
 * disableCacheBackends 使 get->load、mget(单键)->get->load。clearCacheWithFullKeys 分别开启
 * useDistributionCache / useLocalCache 覆盖两个分支。clearCacheByLibraryId 用 spy 桩 getKeys()。
 * JsonUtil 经 mockStatic(SpringUtil) 初始化。</p>
 */
class DocumentPathCacheTest {

  private static MockedStatic<SpringUtil> spring;

  private DocumentPathCache cache;
  private DocumentNodeService documentNodeService;
  private DcDocumentNodeCache dcDocumentNodeCache;
  private ICacheClient cacheClient;

  @BeforeAll
  static void setUpSpring() {
    spring = mockStatic(SpringUtil.class);
    spring.when(() -> SpringUtil.getBean(eq(ObjectMapper.class), any()))
      .thenReturn(CacheTestSupport.jsonObjectMapper());
    assertThat(JsonUtil.toJsonString("init")).isNotNull();
  }

  @AfterAll
  static void tearDownSpring() {
    spring.close();
  }

  @BeforeEach
  void setUp() throws Exception {
    documentNodeService = mock(DocumentNodeService.class);
    dcDocumentNodeCache = mock(DcDocumentNodeCache.class);
    cache = new DocumentPathCache(documentNodeService, dcDocumentNodeCache);
    cacheClient = mock(ICacheClient.class);
    CacheTestSupport.setField(cache, "cacheClient", cacheClient);
    CacheTestSupport.disableCacheBackends(cache);
  }

  private NodeBaseInfoDTO nodeInfo(String libraryId) {
    NodeBaseInfoDTO n = new NodeBaseInfoDTO();
    n.setLibraryId(libraryId);
    return n;
  }

  private List<NodePathDTO> path(String... nodeIds) {
    return Arrays.stream(nodeIds).map(id -> new NodePathDTO(id, id + "-name")).toList();
  }

  // ==================== buildPathKey ====================

  @Test
  void buildPathKey_normal() {
    assertThat(cache.buildPathKey("lib1", "doc1")).isEqualTo("library:lib1:doc1");
  }

  // ==================== extractOriginalKey ====================

  @Test
  void extractOriginalKey_blank_returnsBlank() {
    assertThat(cache.extractOriginalKey("")).isEqualTo("");
  }

  @Test
  void extractOriginalKey_threeParts_returnsThird() {
    assertThat(cache.extractOriginalKey("library:lib1:doc1")).isEqualTo("doc1");
  }

  @Test
  void extractOriginalKey_lessThanThree_returnsOriginal() {
    assertThat(cache.extractOriginalKey("lib1:doc1")).isEqualTo("lib1:doc1");
  }

  @Test
  void extractOriginalKey_moreThanThree_returnsRestAfterSecond() {
    // split(":", 3) -> ["library", "lib1", "doc1:extra"] -> parts[2]
    assertThat(cache.extractOriginalKey("library:lib1:doc1:extra")).isEqualTo("doc1:extra");
  }

  // ==================== getPath ====================

  @Test
  void getPath_blankDocumentId_empty() {
    assertThat(cache.getPath("")).isEmpty();
  }

  @Test
  void getPath_nodeInfoNull_empty() {
    when(dcDocumentNodeCache.getDocumentNodeInfo("doc1")).thenReturn(null);
    assertThat(cache.getPath("doc1")).isEmpty();
  }

  @Test
  void getPath_valid_returnsFilteredReversedPath() {
    when(dcDocumentNodeCache.getDocumentNodeInfo("doc1")).thenReturn(nodeInfo("lib1"));
    when(dcDocumentNodeCache.getLibraryIdByDocument("doc1")).thenReturn("lib1");
    // load 会过滤掉 documentId 自身并反转
    when(documentNodeService.getParentPathByNodeId("lib1", "doc1"))
      .thenReturn(path("doc1", "parent", "root"));
    List<NodePathDTO> result = cache.getPath("doc1");
    assertThat(result).hasSize(2);
    // 原列表 [doc1, parent, root] -> 过滤 doc1 -> [parent, root] -> 反转 -> [root, parent]
    assertThat(result.get(0).getNodeId()).isEqualTo("root");
    assertThat(result.get(1).getNodeId()).isEqualTo("parent");
  }

  // ==================== load ====================

  @Test
  void load_blankKey_empty() {
    assertThat(cache.load("")).isEmpty();
  }

  @Test
  void load_blankLibraryId_empty() {
    when(dcDocumentNodeCache.getLibraryIdByDocument("doc1")).thenReturn("");
    assertThat(cache.load("library:lib1:doc1")).isEmpty();
  }

  // ==================== clearCacheByKeys ====================

  @Test
  void clearCacheByKeys_emptyDocumentIds_noop() {
    cache.clearCacheByKeys("lib1", Collections.emptyList());
    // 无异常即通过
  }

  @Test
  void clearCacheByKeys_nonEmpty_delegatesToClearCacheWithFullKeys() {
    DocumentPathCache spy = spy(cache);
    spy.clearCacheByKeys("lib1", List.of("doc1"));
    verify(spy).clearCacheWithFullKeys(eq(List.of("library:lib1:doc1")));
  }

  // ==================== mget ====================

  @Test
  void mget_emptyKeys_emptyMap() {
    assertThat(cache.mget(Collections.emptyList())).isEmpty();
  }

  @Test
  void mget_allBlankLibraryId_emptyMap() {
    when(dcDocumentNodeCache.getLibraryIdByDocument("doc1")).thenReturn("");
    assertThat(cache.mget(List.of("doc1"))).isEmpty();
  }

  @Test
  void mget_valid_mapsBackToOriginalKey() {
    when(dcDocumentNodeCache.getLibraryIdByDocument("doc1")).thenReturn("lib1");
    when(documentNodeService.getParentPathByNodeId("lib1", "doc1")).thenReturn(path("root"));
    // super.mget(单键) -> get -> load -> [root] -> singletonMap("library:lib1:doc1", [root])
    // DocumentPathCache.mget 将 key 映射回原始 key
    Map<String, List<NodePathDTO>> result = cache.mget(List.of("doc1"));
    assertThat(result).containsKey("doc1");
    assertThat(result.get("doc1")).hasSize(1);
    assertThat(result.get("doc1").get(0).getNodeId()).isEqualTo("root");
  }

  // ==================== clearCacheWithFullKeys ====================

  @Test
  void clearCacheWithFullKeys_distributionEnabled_deletesDistributed() throws Exception {
    CacheTestSupport.setField(cache, "useDistributionCache", true);
    // useKeySetManager 保持 false，避免 keySetManager NPE
    cache.clearCacheWithFullKeys(List.of("library:lib1:doc1"));
    verify(cacheClient).delete(any(java.util.Collection.class));
  }

  @Test
  void clearCacheWithFullKeys_localEnabled_invalidatesLocal() throws Exception {
    CacheTestSupport.setField(cache, "useLocalCache", true);
    CacheTestSupport.setField(cache, "localCache", CacheBuilder.newBuilder().build());
    // SpringUtil.getBeanOptional 默认返回 null -> 跳过 refresh，仅本地失效
    cache.clearCacheWithFullKeys(List.of("library:lib1:doc1"));
    // 无异常即通过
  }

  @Test
  void clearCacheWithFullKeys_allDisabled_noop() {
    cache.clearCacheWithFullKeys(List.of("library:lib1:doc1"));
    verify(cacheClient, org.mockito.Mockito.never()).delete(anyString());
  }

  // ==================== clearCacheByLibraryId ====================

  @Test
  void clearCacheByLibraryId_blankLibraryId_noop() {
    cache.clearCacheByLibraryId("");
    // 无异常即通过
  }

  @Test
  void clearCacheByLibraryId_noMatchingKeys_noop() {
    // dist off -> getKeys() 返回空集
    cache.clearCacheByLibraryId("lib1");
    // 无异常即通过
  }

  @Test
  void clearCacheByLibraryId_matchingKeys_clearsFiltered() {
    DocumentPathCache spy = spy(cache);
    doReturn(Set.of("library:lib1:doc1", "library:lib2:doc2", "other:doc3"))
      .when(spy).getKeys();
    spy.clearCacheByLibraryId("lib1");
    // 仅保留 "library:lib1:*" 前缀的键
    verify(spy).clearCacheWithFullKeys(eq(List.of("library:lib1:doc1")));
  }
}
