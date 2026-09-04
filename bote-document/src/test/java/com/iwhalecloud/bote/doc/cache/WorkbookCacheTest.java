package com.iwhalecloud.bote.doc.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwhalecloud.bote.doc.common.model.PortalUserDTO;
import com.iwhalecloud.bote.doc.module.document.dto.WorkbookLockInfoDTO;
import com.iwhalecloud.bote.doc.module.document.dto.WorkbookLockStatusDTO;
import com.iwhalecloud.bote.doc.module.user.service.IDcUserService;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.data.redis.core.ValueOperations;

/**
 * {@link WorkbookCache} 单元测试。
 *
 * <p>WorkbookCache 不继承 BaseSecondaryCache，构造时由 CacheFactory 取得 ICacheClient，故以 mock
 * CacheFactory 注入 mock ICacheClient。getLockStatus/lockDocument/unlockDocument 均依赖 JsonUtil
 * 静态序列化/反序列化，@BeforeAll 以 mockStatic(SpringUtil) 注入真实 ObjectMapper 供 JsonUtil 加载。
 * 覆盖未锁定/过期/正常锁定/解析失败/会话匹配/锁定冲突/解锁权限等分支。</p>
 */
class WorkbookCacheTest {

  private static MockedStatic<SpringUtil> spring;

  private CacheFactory cacheFactory;
  private ICacheClient cacheClient;
  private ValueOperations<String, String> valueOps;
  private IDcUserService dcUserService;
  private WorkbookCache cache;

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
  void setUp() {
    cacheFactory = mock(CacheFactory.class);
    cacheClient = mock(ICacheClient.class);
    valueOps = mock(ValueOperations.class);
    dcUserService = mock(IDcUserService.class);
    when(cacheFactory.getCacheClient(anyString(), anyString())).thenReturn(cacheClient);
    when(cacheClient.opsForValue()).thenReturn(valueOps);
    cache = new WorkbookCache(cacheFactory, dcUserService);
  }

  private String lockJson(Long userId, String sessionId, long expireTime) {
    long now = System.currentTimeMillis();
    return JsonUtil.toJsonString(new WorkbookLockInfoDTO(userId, sessionId, now, expireTime));
  }

  private String nonExpired(Long userId, String sessionId) {
    return lockJson(userId, sessionId, System.currentTimeMillis() + 600_000);
  }

  private String expired(Long userId, String sessionId) {
    return lockJson(userId, sessionId, System.currentTimeMillis() - 600_000);
  }

  private PortalUserDTO user(Long id, String name) {
    PortalUserDTO u = new PortalUserDTO();
    u.setUserId(id);
    u.setUserName(name);
    return u;
  }

  // ==================== getLockStatus ====================

  @Test
  void getLockStatus_notLocked_returnsUnlocked() {
    when(valueOps.get("lock:doc1")).thenReturn(null);
    WorkbookLockStatusDTO status = cache.getLockStatus("doc1", 1L, "s1");
    assertThat(status.getLocked()).isFalse();
    assertThat(status.getCanEdit()).isTrue();
    assertThat(status.getDocumentId()).isEqualTo("doc1");
  }

  @Test
  void getLockStatus_emptyLockInfo_returnsUnlocked() {
    when(valueOps.get("lock:doc1")).thenReturn("");
    assertThat(cache.getLockStatus("doc1", 1L, "s1").getLocked()).isFalse();
  }

  @Test
  void getLockStatus_expired_deletesAndReturnsUnlocked() {
    when(valueOps.get("lock:doc1")).thenReturn(expired(2L, "s2"));
    WorkbookLockStatusDTO status = cache.getLockStatus("doc1", 1L, "s1");
    assertThat(status.getLocked()).isFalse();
    assertThat(status.getCanEdit()).isTrue();
    verify(cacheClient).delete("lock:doc1");
  }

  @Test
  void getLockStatus_malformedJson_deletesAndReturnsUnlocked() {
    when(valueOps.get("lock:doc1")).thenReturn("not-a-json");
    assertThat(cache.getLockStatus("doc1", 1L, "s1").getLocked()).isFalse();
    verify(cacheClient).delete("lock:doc1");
  }

  @Test
  void getLockStatus_lockedByOther_returnsLockedNotEditable() {
    when(valueOps.get("lock:doc1")).thenReturn(nonExpired(2L, "s2"));
    when(dcUserService.findUserById(2L)).thenReturn(user(2L, "Bob"));
    WorkbookLockStatusDTO status = cache.getLockStatus("doc1", 1L, "s1");
    assertThat(status.getLocked()).isTrue();
    assertThat(status.getLockUserId()).isEqualTo(2L);
    assertThat(status.getLockUserName()).isEqualTo("Bob");
    assertThat(status.getCanEdit()).isFalse();
    verify(cacheClient, never()).delete(anyString());
  }

  @Test
  void getLockStatus_lockedBySelf_returnsLockedAndEditable() {
    when(valueOps.get("lock:doc1")).thenReturn(nonExpired(1L, "s1"));
    when(dcUserService.findUserById(1L)).thenReturn(user(1L, "Alice"));
    WorkbookLockStatusDTO status = cache.getLockStatus("doc1", 1L, "s1");
    assertThat(status.getLocked()).isTrue();
    assertThat(status.getLockUserName()).isEqualTo("Alice");
    assertThat(status.getCanEdit()).isTrue();
  }

  @Test
  void getLockStatus_blankSessionId_usesUserIdAsString() {
    // currentSessionId 为空 -> 回退为 String.valueOf(userId)；锁会话为 "1" 时可编辑
    when(valueOps.get("lock:doc1")).thenReturn(nonExpired(1L, "1"));
    when(dcUserService.findUserById(1L)).thenReturn(user(1L, "Alice"));
    WorkbookLockStatusDTO status = cache.getLockStatus("doc1", 1L, null);
    assertThat(status.getLocked()).isTrue();
    assertThat(status.getCanEdit()).isTrue();
  }

  @Test
  void getLockStatus_lockUserNull_skipsFindUserById() {
    // userId 为 null 的锁定信息：跳过 findUserById，canEdit=false（isLockedBy(null) 为 false）
    when(valueOps.get("lock:doc1")).thenReturn(lockJson(null, "s1", System.currentTimeMillis() + 600_000));
    WorkbookLockStatusDTO status = cache.getLockStatus("doc1", 1L, "s1");
    assertThat(status.getLocked()).isTrue();
    assertThat(status.getLockUserName()).isNull();
    assertThat(status.getCanEdit()).isFalse();
    verify(dcUserService, never()).findUserById(any());
  }

  // ==================== lockDocument ====================

  @Test
  void lockDocument_notLocked_acquiresLock() {
    String lockJson = nonExpired(1L, "s1");
    when(valueOps.get("lock:doc1")).thenReturn(null, lockJson);
    when(dcUserService.findUserById(1L)).thenReturn(user(1L, "Alice"));

    WorkbookLockStatusDTO status = cache.lockDocument("doc1", 1L, "s1", 5);

    assertThat(status.getLocked()).isTrue();
    assertThat(status.getCanEdit()).isTrue();
    assertThat(status.getLockUserName()).isEqualTo("Alice");
    verify(valueOps).set(eq("lock:doc1"), anyString(), eq(5L), eq(TimeUnit.MINUTES));
  }

  @Test
  void lockDocument_lockedByOther_doesNotOverwrite() {
    when(valueOps.get("lock:doc1")).thenReturn(nonExpired(2L, "s2"));
    when(dcUserService.findUserById(2L)).thenReturn(user(2L, "Bob"));

    WorkbookLockStatusDTO status = cache.lockDocument("doc1", 1L, "s1", 5);

    assertThat(status.getLocked()).isTrue();
    assertThat(status.getCanEdit()).isFalse();
    verify(valueOps, never()).set(anyString(), anyString(), eq(5L), eq(TimeUnit.MINUTES));
  }

  @Test
  void lockDocument_lockedBySelf_relocks() {
    String lockJson = nonExpired(1L, "s1");
    when(valueOps.get("lock:doc1")).thenReturn(lockJson, lockJson);
    when(dcUserService.findUserById(1L)).thenReturn(user(1L, "Alice"));

    WorkbookLockStatusDTO status = cache.lockDocument("doc1", 1L, "s1", 5);

    assertThat(status.getLocked()).isTrue();
    assertThat(status.getCanEdit()).isTrue();
    verify(valueOps).set(eq("lock:doc1"), anyString(), eq(5L), eq(TimeUnit.MINUTES));
  }

  // ==================== unlockDocument ====================

  @Test
  void unlockDocument_notLocked_noop() {
    when(valueOps.get("lock:doc1")).thenReturn(null);
    WorkbookLockStatusDTO status = cache.unlockDocument("doc1", 1L, "s1");
    assertThat(status.getLocked()).isFalse();
    verify(cacheClient, never()).delete(anyString());
  }

  @Test
  void unlockDocument_lockedByOther_noDelete() {
    when(valueOps.get("lock:doc1")).thenReturn(nonExpired(2L, "s2"));
    when(dcUserService.findUserById(2L)).thenReturn(user(2L, "Bob"));
    WorkbookLockStatusDTO status = cache.unlockDocument("doc1", 1L, "s1");
    assertThat(status.getLocked()).isTrue();
    verify(cacheClient, never()).delete(anyString());
  }

  @Test
  void unlockDocument_lockedBySelf_deletesAndReturnsUnlocked() {
    String lockJson = nonExpired(1L, "s1");
    when(valueOps.get("lock:doc1")).thenReturn(lockJson, null);
    when(dcUserService.findUserById(1L)).thenReturn(user(1L, "Alice"));

    WorkbookLockStatusDTO status = cache.unlockDocument("doc1", 1L, "s1");

    verify(cacheClient).delete("lock:doc1");
    assertThat(status.getLocked()).isFalse();
    assertThat(status.getCanEdit()).isTrue();
  }
}
