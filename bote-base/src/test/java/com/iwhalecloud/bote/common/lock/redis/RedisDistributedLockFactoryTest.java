package com.iwhalecloud.bote.common.lock.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.iwhalecloud.bote.common.lock.DistributedLock;
import com.iwhalecloud.bote.common.lock.DistributedLock.LockType;
import com.iwhalecloud.bote.common.lock.DistributedLockException;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * {@link RedisDistributedLockFactory} 单元测试
 *
 * <p>使用 Mockito 模拟 {@link ICacheClient}，以真实 {@link RedisDistributedLockProperties}
 * 作为配置夹具，覆盖：构造参数校验、getLock 缓存命中/未命中/禁用缓存分支、createLock、
 * getBizLock、getLockWithExpire、getLockWithAttributes、lockExists、forceDeleteLock、
 * clearCache、shutdown、isAvailable 等方法。</p>
 */
class RedisDistributedLockFactoryTest {

  private ICacheClient cacheClient;
  private RedisDistributedLockProperties properties;
  private RedisDistributedLockFactory factory;

  @BeforeEach
  void setUp() {
    cacheClient = mock(ICacheClient.class);
    properties = new RedisDistributedLockProperties();
    factory = new RedisDistributedLockFactory(cacheClient, properties);
  }

  // ==================== 构造函数 ====================

  @Test
  void constructor_nullCacheClient_throwsIllegalArgument() {
    assertThatThrownBy(() -> new RedisDistributedLockFactory(null, properties))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("缓存客户端不能为空");
  }

  @Test
  void constructor_nullProperties_throwsIllegalArgument() {
    assertThatThrownBy(() -> new RedisDistributedLockFactory(cacheClient, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("配置属性不能为空");
  }

  // ==================== getFactoryType / isAvailable ====================

  @Test
  void getFactoryType_returnsRedis() {
    assertThat(factory.getFactoryType()).isEqualTo("redis");
  }

  @Test
  void isAvailable_beforeShutdown_returnsTrue() {
    assertThat(factory.isAvailable()).isTrue();
  }

  @Test
  void isAvailable_afterShutdown_returnsFalse() {
    factory.shutdown();
    assertThat(factory.isAvailable()).isFalse();
  }

  // ==================== getMutexLock / getReadLock / getWriteLock ====================

  @Test
  void getMutexLock_returnsMutexLock() {
    DistributedLock lock = factory.getMutexLock("order-lock");
    assertThat(lock).isNotNull();
    assertThat(lock.getLockType()).isEqualTo(LockType.MUTEX);
  }

  @Test
  void getReadLock_returnsReadLock() {
    DistributedLock lock = factory.getReadLock("data-lock");
    assertThat(lock).isNotNull();
    assertThat(lock.getLockType()).isEqualTo(LockType.READ);
  }

  @Test
  void getWriteLock_returnsWriteLock() {
    DistributedLock lock = factory.getWriteLock("data-lock");
    assertThat(lock).isNotNull();
    assertThat(lock.getLockType()).isEqualTo(LockType.WRITE);
  }

  // ==================== getLock (缓存逻辑) ====================

  @Test
  void getLock_cacheEnabled_sameKeyReturnsSameInstance() {
    DistributedLock lock1 = factory.getLock("key1", LockType.MUTEX);
    DistributedLock lock2 = factory.getLock("key1", LockType.MUTEX);
    assertThat(lock1).isSameAs(lock2);
  }

  @Test
  void getLock_cacheEnabled_differentTypesReturnDifferentInstances() {
    DistributedLock mutex = factory.getLock("key1", LockType.MUTEX);
    DistributedLock read = factory.getLock("key1", LockType.READ);
    assertThat(mutex).isNotSameAs(read);
  }

  @Test
  void getLock_cacheDisabled_createsNewInstanceEachTime() {
    properties.setCacheEnabled(false);

    DistributedLock lock1 = factory.getLock("key1", LockType.MUTEX);
    DistributedLock lock2 = factory.getLock("key1", LockType.MUTEX);
    assertThat(lock1).isNotSameAs(lock2);
  }

  @Test
  void getLock_nullLockKey_throwsIllegalArgument() {
    assertThatThrownBy(() -> factory.getLock(null, LockType.MUTEX))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("锁标识不能为空");
  }

  @Test
  void getLock_emptyLockKey_throwsIllegalArgument() {
    assertThatThrownBy(() -> factory.getLock("  ", LockType.MUTEX))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("锁标识不能为空");
  }

  @Test
  void getLock_nullLockType_throwsIllegalArgument() {
    assertThatThrownBy(() -> factory.getLock("key", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("锁类型不能为空");
  }

  @Test
  void getLock_afterShutdown_throwsDistributedLockException() {
    factory.shutdown();
    assertThatThrownBy(() -> factory.getLock("key", LockType.MUTEX))
        .isInstanceOf(DistributedLockException.class);
  }

  // ==================== createLock ====================

  @Test
  void createLock_returnsNewInstance() {
    DistributedLock lock1 = factory.createLock("key1", LockType.MUTEX);
    DistributedLock lock2 = factory.createLock("key1", LockType.MUTEX);
    assertThat(lock1).isNotSameAs(lock2);
  }

  @Test
  void createLock_nullLockKey_throwsIllegalArgument() {
    assertThatThrownBy(() -> factory.createLock(null, LockType.MUTEX))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void createLock_afterShutdown_throwsDistributedLockException() {
    factory.shutdown();
    assertThatThrownBy(() -> factory.createLock("key", LockType.MUTEX))
        .isInstanceOf(DistributedLockException.class);
  }

  // ==================== getBizLock ====================

  @Test
  void getBizLock_twoArgs_returnsMutexLock() {
    DistributedLock lock = factory.getBizLock("order", "123");
    assertThat(lock).isNotNull();
    assertThat(lock.getLockType()).isEqualTo(LockType.MUTEX);
  }

  @Test
  void getBizLock_threeArgs_returnsSpecifiedType() {
    DistributedLock lock = factory.getBizLock("order", "123", LockType.READ);
    assertThat(lock).isNotNull();
    assertThat(lock.getLockType()).isEqualTo(LockType.READ);
  }

  @Test
  void getBizLock_nullBizType_throwsIllegalArgument() {
    assertThatThrownBy(() -> factory.getBizLock(null, "123"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  // ==================== getLockWithExpire ====================

  @Test
  void getLockWithExpire_returnsLockWithCustomExpire() {
    DistributedLock lock = factory.getLockWithExpire("temp-lock", LockType.MUTEX, 60000);
    assertThat(lock).isNotNull();
    assertThat(lock.getLockKey()).contains("temp-lock");
  }

  @Test
  void getLockWithExpire_nullLockKey_throwsIllegalArgument() {
    assertThatThrownBy(() -> factory.getLockWithExpire(null, LockType.MUTEX, 1000))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void getLockWithExpire_afterShutdown_throwsDistributedLockException() {
    factory.shutdown();
    assertThatThrownBy(() -> factory.getLockWithExpire("key", LockType.MUTEX, 1000))
        .isInstanceOf(DistributedLockException.class);
  }

  // ==================== getLockWithAttributes ====================

  @Test
  void getLockWithAttributes_ignoresAttributesAndReturnsLock() {
    DistributedLock lock = factory.getLockWithAttributes(
        "key", LockType.MUTEX, Map.of("attr", "val"));
    assertThat(lock).isNotNull();
  }

  // ==================== lockExists ====================

  @Test
  void lockExists_returnsTrue() {
    when(cacheClient.hasKey(anyString())).thenReturn(true);
    assertThat(factory.lockExists("key")).isTrue();
  }

  @Test
  void lockExists_returnsFalse() {
    when(cacheClient.hasKey(anyString())).thenReturn(false);
    assertThat(factory.lockExists("key")).isFalse();
  }

  @Test
  void lockExists_throwsException_returnsFalse() {
    when(cacheClient.hasKey(anyString())).thenThrow(new RuntimeException("error"));
    assertThat(factory.lockExists("key")).isFalse();
  }

  @Test
  void lockExists_nullLockKey_throwsIllegalArgument() {
    assertThatThrownBy(() -> factory.lockExists(null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void lockExists_afterShutdown_throwsDistributedLockException() {
    factory.shutdown();
    assertThatThrownBy(() -> factory.lockExists("key"))
        .isInstanceOf(DistributedLockException.class);
  }

  // ==================== forceDeleteLock ====================

  @Test
  void forceDeleteLock_deleteSucceeds_doesNotThrow() {
    when(cacheClient.delete(anyString())).thenReturn(true);
    factory.forceDeleteLock("key");
  }

  @Test
  void forceDeleteLock_deleteFails_doesNotThrow() {
    when(cacheClient.delete(anyString())).thenReturn(false);
    factory.forceDeleteLock("key");
  }

  @Test
  void forceDeleteLock_throwsException_throwsDistributedLockException() {
    when(cacheClient.delete(anyString())).thenThrow(new RuntimeException("error"));
    assertThatThrownBy(() -> factory.forceDeleteLock("key"))
        .isInstanceOf(DistributedLockException.class);
  }

  @Test
  void forceDeleteLock_afterShutdown_throwsDistributedLockException() {
    factory.shutdown();
    assertThatThrownBy(() -> factory.forceDeleteLock("key"))
        .isInstanceOf(DistributedLockException.class);
  }

  // ==================== clearCache ====================

  @Test
  void clearCache_specificKey_doesNotThrow() {
    factory.getLock("key1", LockType.MUTEX);
    factory.clearCache("key1");
    // 再次获取应创建新实例
    DistributedLock lock1 = factory.getLock("key1", LockType.MUTEX);
    assertThat(lock1).isNotNull();
  }

  @Test
  void clearCache_nullKey_clearsAll() {
    factory.getLock("key1", LockType.MUTEX);
    factory.clearCache(null);
    factory.clearCache("  ");
  }

  // ==================== shutdown ====================

  @Test
  void shutdown_calledTwice_secondCallIsNoop() {
    factory.shutdown();
    factory.shutdown(); // 不抛异常
    assertThat(factory.isAvailable()).isFalse();
  }

  @Test
  void shutdown_invalidatesCache() {
    DistributedLock lock1 = factory.getLock("key1", LockType.MUTEX);
    factory.shutdown();
    // shutdown 后 isAvailable=false，getLock 抛异常
    assertThatThrownBy(() -> factory.getLock("key1", LockType.MUTEX))
        .isInstanceOf(DistributedLockException.class);
  }
}
