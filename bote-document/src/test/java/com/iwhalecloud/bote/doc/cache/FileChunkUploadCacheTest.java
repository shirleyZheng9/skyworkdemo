package com.iwhalecloud.bote.doc.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwhalecloud.bote.doc.module.base.dto.FileChunkInfo;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import com.google.common.cache.CacheBuilder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.data.redis.core.HashOperations;

/**
 * {@link FileChunkUploadCache} 单元测试。
 *
 * <p>直接继承 BaseSecondaryCache，构造无参、useLocalCache=false。saveChunkInfoWithDetails/
 * getUploadedChunkIndexes/getAllChunkDetails/hasChunk/getChunkCount/deleteChunkBatchStatus 直接使用
 * cacheClient.opsForHash()/expire()/delete()，故反射注入 mock ICacheClient + mock HashOperations。
 * isChunkBatchCompleted/markChunkBatchCompleted/getFileUrl 经 get()->load()（默认 null）；为测往返，
 * 部分用例开启本地缓存（真实 Guava localCache）。JsonUtil 经 mockStatic(SpringUtil) 初始化。</p>
 */
@SuppressWarnings("unchecked")
class FileChunkUploadCacheTest {

  private static MockedStatic<SpringUtil> spring;

  private FileChunkUploadCache cache;
  private ICacheClient cacheClient;
  private HashOperations<String, Object, Object> hashOps;

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
    cache = new FileChunkUploadCache();
    cacheClient = mock(ICacheClient.class);
    hashOps = mock(HashOperations.class);
    when(cacheClient.opsForHash()).thenReturn(hashOps);
    CacheTestSupport.setField(cache, "cacheClient", cacheClient);
    CacheTestSupport.disableCacheBackends(cache);
  }

  private void enableLocal() throws Exception {
    CacheTestSupport.setField(cache, "useLocalCache", true);
    CacheTestSupport.setField(cache, "localCache", CacheBuilder.newBuilder().build());
  }

  private FileChunkInfo chunk(Integer index, String fileHash) {
    FileChunkInfo c = new FileChunkInfo();
    c.setChunkIndex(index);
    c.setFileHash(fileHash);
    c.setFilePath("/path/" + index);
    return c;
  }

  private static final String BATCH_KEY = "chunk_batch:10:1:h1";
  private static final String CHUNK_HASH_KEY = BATCH_KEY + ":chunks";

  // ==================== saveChunkInfoWithDetails ====================

  @Test
  void saveChunkInfo_nullChunkIndex_earlyReturn() {
    cache.saveChunkInfoWithDetails(10L, 1L, chunk(null, "h1"), 2);
    verify(cacheClient, never()).opsForHash();
  }

  @Test
  void saveChunkInfo_valid_putsAndExpires() {
    cache.saveChunkInfoWithDetails(10L, 1L, chunk(1, "h1"), 2);
    verify(hashOps).put(eq(CHUNK_HASH_KEY), eq("1"), anyString());
    verify(cacheClient).expire(eq(CHUNK_HASH_KEY), eq(7200L), eq(TimeUnit.SECONDS));
  }

  @Test
  void saveChunkInfo_putThrows_bssException() {
    doThrow(new RuntimeException("redis down")).when(hashOps).put(anyString(), any(), any());
    assertThatThrownBy(() -> cache.saveChunkInfoWithDetails(10L, 1L, chunk(1, "h1"), 2))
      .isInstanceOf(BssException.class);
  }

  // ==================== isChunkBatchCompleted ====================

  @Test
  void isChunkBatchCompleted_blankFileHash_false() {
    assertThat(cache.isChunkBatchCompleted(10L, 1L, "")).isFalse();
    verifyNoInteractions(cacheClient);
  }

  @Test
  void isChunkBatchCompleted_noEntity_false() {
    assertThat(cache.isChunkBatchCompleted(10L, 1L, "h1")).isFalse();
  }

  // ==================== markChunkBatchCompleted ====================

  @Test
  void markChunkBatchCompleted_blankFileHash_earlyReturn() throws Exception {
    enableLocal();
    cache.markChunkBatchCompleted(10L, 1L, "", "http://url", 2);
    // 未写入本地缓存
    assertThat(cache.getLocalCache(BATCH_KEY)).isNull();
  }

  @Test
  void markChunkBatchCompleted_thenCompletedAndFileUrl() throws Exception {
    enableLocal();
    cache.markChunkBatchCompleted(10L, 1L, "h1", "http://url", 2);
    assertThat(cache.isChunkBatchCompleted(10L, 1L, "h1")).isTrue();
    assertThat(cache.getFileUrl(10L, 1L, "h1")).isEqualTo("http://url");
  }

  // ==================== getUploadedChunkIndexes ====================

  @Test
  void getUploadedChunkIndexes_blankFileHash_empty() {
    assertThat(cache.getUploadedChunkIndexes(10L, 1L, "")).isEmpty();
    verifyNoInteractions(cacheClient);
  }

  @Test
  void getUploadedChunkIndexes_emptyMap_empty() {
    when(hashOps.entries(CHUNK_HASH_KEY)).thenReturn(new LinkedHashMap<>());
    assertThat(cache.getUploadedChunkIndexes(10L, 1L, "h1")).isEmpty();
  }

  @Test
  void getUploadedChunkIndexes_validIndexes_sortedAndFiltered() {
    Map<Object, Object> map = new LinkedHashMap<>();
    map.put("2", "x");
    map.put("1", "x");
    map.put("bad", "x");
    when(hashOps.entries(CHUNK_HASH_KEY)).thenReturn(map);
    assertThat(cache.getUploadedChunkIndexes(10L, 1L, "h1")).containsExactly(1, 2);
  }

  @Test
  void getUploadedChunkIndexes_entriesThrows_empty() {
    when(hashOps.entries(CHUNK_HASH_KEY)).thenThrow(new RuntimeException("redis down"));
    assertThat(cache.getUploadedChunkIndexes(10L, 1L, "h1")).isEmpty();
  }

  // ==================== deleteChunkBatchStatus ====================

  @Test
  void deleteChunkBatchStatus_blankFileHash_earlyReturn() {
    cache.deleteChunkBatchStatus(10L, 1L, "");
    verify(cacheClient, never()).delete(anyString());
  }

  @Test
  void deleteChunkBatchStatus_valid_deletesHashAndBatch() {
    cache.deleteChunkBatchStatus(10L, 1L, "h1");
    verify(cacheClient).delete(CHUNK_HASH_KEY);
  }

  @Test
  void deleteChunkBatchStatus_deleteThrows_noRethrow() {
    when(cacheClient.delete(anyString())).thenThrow(new RuntimeException("redis down"));
    // 异常被吞掉，不向外抛出
    cache.deleteChunkBatchStatus(10L, 1L, "h1");
  }

  // ==================== getFileUrl ====================

  @Test
  void getFileUrl_blankFileHash_null() {
    assertThat(cache.getFileUrl(10L, 1L, "")).isNull();
    verifyNoInteractions(cacheClient);
  }

  @Test
  void getFileUrl_noEntity_null() {
    assertThat(cache.getFileUrl(10L, 1L, "h1")).isNull();
  }

  // ==================== getAllChunkDetails ====================

  @Test
  void getAllChunkDetails_blankFileHash_empty() {
    assertThat(cache.getAllChunkDetails(10L, 1L, "")).isEmpty();
    verifyNoInteractions(cacheClient);
  }

  @Test
  void getAllChunkDetails_emptyMap_empty() {
    when(hashOps.entries(CHUNK_HASH_KEY)).thenReturn(new LinkedHashMap<>());
    assertThat(cache.getAllChunkDetails(10L, 1L, "h1")).isEmpty();
  }

  @Test
  void getAllChunkDetails_valid_sortedByChunkIndex() {
    Map<Object, Object> map = new LinkedHashMap<>();
    map.put("a", JsonUtil.toJsonString(chunk(2, "h1")));
    map.put("b", JsonUtil.toJsonString(chunk(1, "h1")));
    when(hashOps.entries(CHUNK_HASH_KEY)).thenReturn(map);
    List<FileChunkInfo> result = cache.getAllChunkDetails(10L, 1L, "h1");
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getChunkIndex()).isEqualTo(1);
    assertThat(result.get(1).getChunkIndex()).isEqualTo(2);
  }

  @Test
  void getAllChunkDetails_malformedJson_filteredOut() {
    Map<Object, Object> map = new LinkedHashMap<>();
    map.put("a", "not-json");
    when(hashOps.entries(CHUNK_HASH_KEY)).thenReturn(map);
    assertThat(cache.getAllChunkDetails(10L, 1L, "h1")).isEmpty();
  }

  @Test
  void getAllChunkDetails_entriesThrows_empty() {
    when(hashOps.entries(CHUNK_HASH_KEY)).thenThrow(new RuntimeException("redis down"));
    assertThat(cache.getAllChunkDetails(10L, 1L, "h1")).isEmpty();
  }

  // ==================== hasChunk ====================

  @Test
  void hasChunk_blankFileHashOrNullIndex_false() {
    assertThat(cache.hasChunk(10L, 1L, "", 1)).isFalse();
    assertThat(cache.hasChunk(10L, 1L, "h1", null)).isFalse();
    verifyNoInteractions(cacheClient);
  }

  @Test
  void hasChunk_true() {
    when(hashOps.hasKey(CHUNK_HASH_KEY, "1")).thenReturn(true);
    assertThat(cache.hasChunk(10L, 1L, "h1", 1)).isTrue();
  }

  @Test
  void hasChunk_throws_false() {
    when(hashOps.hasKey(anyString(), any())).thenThrow(new RuntimeException("redis down"));
    assertThat(cache.hasChunk(10L, 1L, "h1", 1)).isFalse();
  }

  // ==================== getChunkCount ====================

  @Test
  void getChunkCount_blankFileHash_zero() {
    assertThat(cache.getChunkCount(10L, 1L, "")).isZero();
    verifyNoInteractions(cacheClient);
  }

  @Test
  void getChunkCount_valid_returnsSize() {
    when(hashOps.size(CHUNK_HASH_KEY)).thenReturn(3L);
    assertThat(cache.getChunkCount(10L, 1L, "h1")).isEqualTo(3L);
  }

  @Test
  void getChunkCount_throws_zero() {
    when(hashOps.size(anyString())).thenThrow(new RuntimeException("redis down"));
    assertThat(cache.getChunkCount(10L, 1L, "h1")).isZero();
  }
}
