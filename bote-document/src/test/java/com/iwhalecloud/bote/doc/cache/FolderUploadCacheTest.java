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
import com.iwhalecloud.bote.doc.module.base.dto.FolderUploadCacheDTO;
import com.iwhalecloud.bote.doc.module.base.dto.FolderUploadCacheDTO.UploadedFileInfo;
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
 * {@link FolderUploadCache} 单元测试。
 *
 * <p>直接继承 BaseSecondaryCache，构造无参、useLocalCache=false。saveFileInfoWithHash/
 * getAllFileInfos/hasFileInfo 直接使用 cacheClient.opsForHash()/expire()，故反射注入 mock
 * ICacheClient + mock HashOperations。getUploadStatus/pause/resume/cancel/saveFolderUploadCache
 * 经 get()/put()：为测写入与状态迁移往返，开启本地缓存（真实 Guava localCache）。JsonUtil 经
 * mockStatic(SpringUtil) 初始化。</p>
 */
@SuppressWarnings("unchecked")
class FolderUploadCacheTest {

  private static MockedStatic<SpringUtil> spring;

  private FolderUploadCache cache;
  private ICacheClient cacheClient;
  private HashOperations<String, Object, Object> hashOps;

  private static final String TASK_KEY = "folder_upload:t1";
  private static final String FILE_HASH_KEY = "folder_upload:t1:files";

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
    cache = new FolderUploadCache();
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

  private FolderUploadCacheDTO dto(String taskId) {
    FolderUploadCacheDTO d = new FolderUploadCacheDTO();
    d.setTaskId(taskId);
    return d;
  }

  private UploadedFileInfo fileInfo(String fileHash) {
    UploadedFileInfo info = new UploadedFileInfo();
    info.setFileHash(fileHash);
    info.setOriginalFileName(fileHash + ".txt");
    return info;
  }

  // ==================== getUploadStatus / getFolderUploadCache ====================

  @Test
  void getUploadStatus_blankTaskId_null() {
    assertThat(cache.getUploadStatus("")).isNull();
    verifyNoInteractions(cacheClient);
  }

  @Test
  void getUploadStatus_noEntity_null() {
    assertThat(cache.getUploadStatus("t1")).isNull();
  }

  @Test
  void getFolderUploadCache_delegatesToGetUploadStatus() throws Exception {
    enableLocal();
    cache.saveFolderUploadCache(dto("t1"));
    assertThat(cache.getFolderUploadCache("t1")).isNotNull();
    assertThat(cache.getFolderUploadCache("")).isNull();
  }

  // ==================== saveFolderUploadCache ====================

  @Test
  void saveFolderUploadCache_blankTaskId_earlyReturn() throws Exception {
    enableLocal();
    cache.saveFolderUploadCache(dto(""));
    assertThat(cache.getLocalCache(TASK_KEY)).isNull();
  }

  @Test
  void saveFolderUploadCache_valid_roundTrip() throws Exception {
    enableLocal();
    FolderUploadCacheDTO d = dto("t1");
    cache.saveFolderUploadCache(d);
    FolderUploadCacheDTO loaded = cache.getUploadStatus("t1");
    assertThat(loaded).isNotNull();
    assertThat(loaded.getTaskId()).isEqualTo("t1");
  }

  // ==================== pauseUploadTask ====================

  @Test
  void pauseUploadTask_blankTaskId_earlyReturn() {
    cache.pauseUploadTask("", 2);
    assertThat(cache.getUploadStatus("t1")).isNull();
  }

  @Test
  void pauseUploadTask_noStatus_noop() throws Exception {
    enableLocal();
    cache.pauseUploadTask("t1", 2);
    assertThat(cache.getUploadStatus("t1")).isNull();
  }

  @Test
  void pauseUploadTask_setsPaused() throws Exception {
    enableLocal();
    cache.saveFolderUploadCache(dto("t1"));
    cache.pauseUploadTask("t1", 2);
    assertThat(cache.getUploadStatus("t1").getStatus()).isEqualTo("PAUSED");
  }

  // ==================== resumeUploadTask ====================

  @Test
  void resumeUploadTask_setsUploading() throws Exception {
    enableLocal();
    cache.saveFolderUploadCache(dto("t1"));
    cache.pauseUploadTask("t1", 2);
    assertThat(cache.getUploadStatus("t1").getStatus()).isEqualTo("PAUSED");
    cache.resumeUploadTask("t1", 2);
    assertThat(cache.getUploadStatus("t1").getStatus()).isEqualTo("UPLOADING");
  }

  // ==================== cancelUploadTask ====================

  @Test
  void cancelUploadTask_setsCancelled() throws Exception {
    enableLocal();
    cache.saveFolderUploadCache(dto("t1"));
    cache.cancelUploadTask("t1", 2);
    assertThat(cache.getUploadStatus("t1").getStatus()).isEqualTo("CANCELLED");
  }

  // ==================== saveFileInfoWithHash ====================

  @Test
  void saveFileInfoWithHash_blankTaskIdOrFileHash_earlyReturn() {
    cache.saveFileInfoWithHash("", "h1", fileInfo("h1"), 2);
    cache.saveFileInfoWithHash("t1", "", fileInfo("h1"), 2);
    verify(cacheClient, never()).opsForHash();
  }

  @Test
  void saveFileInfoWithHash_valid_putsAndExpires() {
    cache.saveFileInfoWithHash("t1", "h1", fileInfo("h1"), 2);
    verify(hashOps).put(eq(FILE_HASH_KEY), eq("h1"), anyString());
    verify(cacheClient).expire(eq(FILE_HASH_KEY), eq(7200L), eq(TimeUnit.SECONDS));
  }

  @Test
  void saveFileInfoWithHash_putThrows_bssException() {
    doThrow(new RuntimeException("redis down")).when(hashOps).put(anyString(), any(), any());
    assertThatThrownBy(() -> cache.saveFileInfoWithHash("t1", "h1", fileInfo("h1"), 2))
      .isInstanceOf(BssException.class);
  }

  // ==================== getAllFileInfos ====================

  @Test
  void getAllFileInfos_blankTaskId_empty() {
    assertThat(cache.getAllFileInfos("")).isEmpty();
    verifyNoInteractions(cacheClient);
  }

  @Test
  void getAllFileInfos_emptyMap_empty() {
    when(hashOps.entries(FILE_HASH_KEY)).thenReturn(new LinkedHashMap<>());
    assertThat(cache.getAllFileInfos("t1")).isEmpty();
  }

  @Test
  void getAllFileInfos_valid() {
    Map<Object, Object> map = new LinkedHashMap<>();
    map.put("h1", JsonUtil.toJsonString(fileInfo("h1")));
    map.put("h2", JsonUtil.toJsonString(fileInfo("h2")));
    when(hashOps.entries(FILE_HASH_KEY)).thenReturn(map);
    List<UploadedFileInfo> result = cache.getAllFileInfos("t1");
    assertThat(result).hasSize(2);
  }

  @Test
  void getAllFileInfos_malformedJson_filtered() {
    Map<Object, Object> map = new LinkedHashMap<>();
    map.put("h1", "not-json");
    when(hashOps.entries(FILE_HASH_KEY)).thenReturn(map);
    assertThat(cache.getAllFileInfos("t1")).isEmpty();
  }

  @Test
  void getAllFileInfos_entriesThrows_empty() {
    when(hashOps.entries(FILE_HASH_KEY)).thenThrow(new RuntimeException("redis down"));
    assertThat(cache.getAllFileInfos("t1")).isEmpty();
  }

  // ==================== hasFileInfo ====================

  @Test
  void hasFileInfo_blankTaskIdOrFileHash_false() {
    assertThat(cache.hasFileInfo("", "h1")).isFalse();
    assertThat(cache.hasFileInfo("t1", "")).isFalse();
    verifyNoInteractions(cacheClient);
  }

  @Test
  void hasFileInfo_true() {
    when(hashOps.hasKey(FILE_HASH_KEY, "h1")).thenReturn(true);
    assertThat(cache.hasFileInfo("t1", "h1")).isTrue();
  }

  @Test
  void hasFileInfo_throws_false() {
    when(hashOps.hasKey(anyString(), any())).thenThrow(new RuntimeException("redis down"));
    assertThat(cache.hasFileInfo("t1", "h1")).isFalse();
  }
}
