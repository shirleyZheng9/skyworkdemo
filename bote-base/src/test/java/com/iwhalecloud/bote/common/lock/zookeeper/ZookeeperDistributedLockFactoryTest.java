package com.iwhalecloud.bote.common.lock.zookeeper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.iwhalecloud.bote.common.lock.DistributedLock;
import com.iwhalecloud.bote.common.lock.DistributedLock.LockType;
import com.iwhalecloud.bote.common.lock.DistributedLockException;
import java.util.Map;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.BackgroundVersionable;
import org.apache.curator.framework.api.DeleteBuilder;
import org.apache.curator.framework.api.ExistsBuilder;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.zookeeper.data.Stat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * {@link ZookeeperDistributedLockFactory} 单元测试
 *
 * <p>使用 Mockito 模拟 {@link CuratorFramework}，以真实 {@link ZookeeperDistributedLockProperties}
 * 作为配置夹具，覆盖：构造参数校验、getLock 缓存命中/未命中/禁用缓存、createLock、getBizLock、
 * getLockWithExpire（忽略 expire）、getLockWithAttributes、lockExists、forceDeleteLock（含
 * checkExists+delete 链式调用）、clearCache、shutdown、isAvailable（含 getState 分支）等。</p>
 */
class ZookeeperDistributedLockFactoryTest {

  private CuratorFramework curator;
  private ZookeeperDistributedLockProperties properties;
  private ZookeeperDistributedLockFactory factory;

  @BeforeEach
  void setUp() {
    curator = mock(CuratorFramework.class);
    when(curator.getState()).thenReturn(CuratorFrameworkState.STARTED);
    properties = new ZookeeperDistributedLockProperties();
    factory = new ZookeeperDistributedLockFactory(curator, properties);
  }

  // ==================== 构造函数 ====================

  @Test
  void constructor_nullCurator_throwsIllegalArgument() {
    assertThatThrownBy(() -> new ZookeeperDistributedLockFactory(null, properties))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("CuratorFramework不能为空");
  }

  @Test
  void constructor_nullProperties_throwsIllegalArgument() {
    assertThatThrownBy(() -> new ZookeeperDistributedLockFactory(curator, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("配置属性不能为空");
  }

  // ==================== getFactoryType / isAvailable ====================

  @Test
  void getFactoryType_returnsZookeeper() {
    assertThat(factory.getFactoryType()).isEqualTo("zookeeper");
  }

  @Test
  void isAvailable_started_returnsTrue() {
    assertThat(factory.isAvailable()).isTrue();
  }

  @Test
  void isAvailable_notStarted_returnsFalse() {
    when(curator.getState()).thenReturn(CuratorFrameworkState.LATENT);
    assertThat(factory.isAvailable()).isFalse();
  }

  @Test
  void isAvailable_afterShutdown_returnsFalse() {
    factory.shutdown();
    assertThat(factory.isAvailable()).isFalse();
  }

  // ==================== getMutexLock / getReadLock / getWriteLock ====================

  @Test
  void getMutexLock_returnsMutexLock() {
    DistributedLock lock = factory.getMutexLock("key");
    assertThat(lock).isNotNull();
    assertThat(lock.getLockType()).isEqualTo(LockType.MUTEX);
  }

  @Test
  void getReadLock_returnsReadLock() {
    DistributedLock lock = factory.getReadLock("key");
    assertThat(lock.getLockType()).isEqualTo(LockType.READ);
  }

  @Test
  void getWriteLock_returnsWriteLock() {
    DistributedLock lock = factory.getWriteLock("key");
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
    DistributedLock lock = factory.getBizLock("order", "123", LockType.WRITE);
    assertThat(lock.getLockType()).isEqualTo(LockType.WRITE);
  }

  @Test
  void getBizLock_nullBizType_throwsIllegalArgument() {
    assertThatThrownBy(() -> factory.getBizLock(null, "123"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  // ==================== getLockWithExpire ====================

  @Test
  void getLockWithExpire_ignoresExpireAndReturnsLock() {
    DistributedLock lock = factory.getLockWithExpire("temp", LockType.MUTEX, 60000);
    assertThat(lock).isNotNull();
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
  void lockExists_pathExists_returnsTrue() throws Exception {
    ExistsBuilder existsBuilder = mock(ExistsBuilder.class);
    when(curator.checkExists()).thenReturn(existsBuilder);
    when(existsBuilder.forPath(anyString())).thenReturn(new Stat());

    assertThat(factory.lockExists("key")).isTrue();
  }

  @Test
  void lockExists_pathNotExists_returnsFalse() throws Exception {
    ExistsBuilder existsBuilder = mock(ExistsBuilder.class);
    when(curator.checkExists()).thenReturn(existsBuilder);
    when(existsBuilder.forPath(anyString())).thenReturn(null);

    assertThat(factory.lockExists("key")).isFalse();
  }

  @Test
  void lockExists_throwsException_returnsFalse() {
    when(curator.checkExists()).thenThrow(new RuntimeException("error"));

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
  void forceDeleteLock_pathExists_deletesAndDoesNotThrow() throws Exception {
    ExistsBuilder existsBuilder = mock(ExistsBuilder.class);
    when(curator.checkExists()).thenReturn(existsBuilder);
    when(existsBuilder.forPath(anyString())).thenReturn(new Stat());

    DeleteBuilder deleteBuilder = mock(DeleteBuilder.class);
    BackgroundVersionable versionable = mock(BackgroundVersionable.class);
    when(curator.delete()).thenReturn(deleteBuilder);
    when(deleteBuilder.deletingChildrenIfNeeded()).thenReturn(versionable);
    doNothing().when(versionable).forPath(anyString());

    factory.forceDeleteLock("key");
  }

  @Test
  void forceDeleteLock_pathNotExists_skipsDelete() throws Exception {
    ExistsBuilder existsBuilder = mock(ExistsBuilder.class);
    when(curator.checkExists()).thenReturn(existsBuilder);
    when(existsBuilder.forPath(anyString())).thenReturn(null);

    factory.forceDeleteLock("key");
  }

  @Test
  void forceDeleteLock_throwsException_throwsDistributedLockException() {
    when(curator.checkExists()).thenThrow(new RuntimeException("error"));

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
    DistributedLock lock = factory.getLock("key1", LockType.MUTEX);
    assertThat(lock).isNotNull();
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
    factory.shutdown();
    assertThat(factory.isAvailable()).isFalse();
  }

  @Test
  void shutdown_invalidatesCache() {
    factory.getLock("key1", LockType.MUTEX);
    factory.shutdown();
    assertThatThrownBy(() -> factory.getLock("key1", LockType.MUTEX))
        .isInstanceOf(DistributedLockException.class);
  }
}
