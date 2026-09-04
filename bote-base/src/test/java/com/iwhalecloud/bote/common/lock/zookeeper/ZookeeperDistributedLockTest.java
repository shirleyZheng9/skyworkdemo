package com.iwhalecloud.bote.common.lock.zookeeper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.iwhalecloud.bote.common.lock.DistributedLock.LockMetadata;
import com.iwhalecloud.bote.common.lock.DistributedLock.LockType;
import com.iwhalecloud.bote.common.lock.DistributedLockException;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.ExistsBuilder;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.zookeeper.data.Stat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * {@link ZookeeperDistributedLock} 单元测试
 *
 * <p>使用 Mockito 模拟 {@link CuratorFramework}，构造锁实例后通过反射将内部 mutex 字段
 * 替换为 mock {@link InterProcessMutex}，从而覆盖 lock/tryLock/lockInterruptibly/unlock/
 * forceUnlock/isHeldByCurrentThread 等方法的成功/超时/中断/异常分支。exists() 通过模拟
 * checkExists().forPath() 链式调用覆盖。同时覆盖构造参数校验与各便捷构造函数。</p>
 *
 * <p>注意：构造函数内部以 new InterProcessMutex/InterProcessReadWriteLock(client, path) 创建
 * 真有锁对象，但由于 Curator 构造函数仅存储引用（不发起 ZK 请求），传入 mock client 不会抛异常。</p>
 */
class ZookeeperDistributedLockTest {

  private CuratorFramework curator;
  private InterProcessMutex mockMutex;

  @BeforeEach
  void setUp() {
    curator = mock(CuratorFramework.class);
    mockMutex = mock(InterProcessMutex.class);
  }

  /**
   * 创建指定类型的锁，并用 mock 替换内部 mutex 字段。
   */
  private ZookeeperDistributedLock newLockWithMockMutex(LockType lockType) throws Exception {
    ZookeeperDistributedLock lock =
        new ZookeeperDistributedLock(curator, "test-key", "/locks/test", lockType, 30000);
    Field f = ZookeeperDistributedLock.class.getDeclaredField("mutex");
    f.setAccessible(true);
    f.set(lock, mockMutex);
    return lock;
  }

  // ==================== 构造函数 ====================

  @Test
  void constructor_threeArgs_createsMutexLock() {
    ZookeeperDistributedLock lock =
        new ZookeeperDistributedLock(curator, "key", "/locks/key");
    assertThat(lock.getLockKey()).isEqualTo("key");
    assertThat(lock.getLockType()).isEqualTo(LockType.MUTEX);
  }

  @Test
  void constructor_fourArgs_createsSpecifiedType() {
    ZookeeperDistributedLock lock =
        new ZookeeperDistributedLock(curator, "key", "/locks/key", LockType.READ);
    assertThat(lock.getLockType()).isEqualTo(LockType.READ);
  }

  @Test
  void constructor_fiveArgs_createsWriteLock() {
    ZookeeperDistributedLock lock =
        new ZookeeperDistributedLock(curator, "key", "/locks/key", LockType.WRITE, 5000);
    assertThat(lock.getLockType()).isEqualTo(LockType.WRITE);
  }

  @Test
  void constructor_nullCurator_throwsIllegalArgument() {
    assertThatThrownBy(() ->
        new ZookeeperDistributedLock(null, "key", "/locks/key", LockType.MUTEX, 1000))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("CuratorFramework不能为空");
  }

  @Test
  void constructor_nullLockKey_throwsIllegalArgument() {
    assertThatThrownBy(() ->
        new ZookeeperDistributedLock(curator, null, "/locks/key", LockType.MUTEX, 1000))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("锁标识不能为空");
  }

  @Test
  void constructor_emptyLockKey_throwsIllegalArgument() {
    assertThatThrownBy(() ->
        new ZookeeperDistributedLock(curator, "  ", "/locks/key", LockType.MUTEX, 1000))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("锁标识不能为空");
  }

  @Test
  void constructor_nullLockPath_throwsIllegalArgument() {
    assertThatThrownBy(() ->
        new ZookeeperDistributedLock(curator, "key", null, LockType.MUTEX, 1000))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("锁路径不能为空");
  }

  @Test
  void constructor_emptyLockPath_throwsIllegalArgument() {
    assertThatThrownBy(() ->
        new ZookeeperDistributedLock(curator, "key", "  ", LockType.MUTEX, 1000))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("锁路径不能为空");
  }

  @Test
  void constructor_nullLockType_throwsIllegalArgument() {
    assertThatThrownBy(() ->
        new ZookeeperDistributedLock(curator, "key", "/locks/key", null, 1000))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("锁类型不能为空");
  }

  @Test
  void constructor_zeroMaxWaitTime_throwsIllegalArgument() {
    assertThatThrownBy(() ->
        new ZookeeperDistributedLock(curator, "key", "/locks/key", LockType.MUTEX, 0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("最大等待时间必须大于0");
  }

  // ==================== lock() ====================

  @Test
  void lock_success_acquiresLock() throws Exception {
    when(mockMutex.acquire(anyLong(), any(TimeUnit.class))).thenReturn(true);
    ZookeeperDistributedLock lock = newLockWithMockMutex(LockType.MUTEX);
    lock.lock();
    assertThat(lock.getHoldTime()).isGreaterThanOrEqualTo(0);
  }

  @Test
  void lock_timeout_throwsDistributedLockException() throws Exception {
    when(mockMutex.acquire(anyLong(), any(TimeUnit.class))).thenReturn(false);
    ZookeeperDistributedLock lock = newLockWithMockMutex(LockType.MUTEX);
    assertThatThrownBy(() -> lock.lock())
        .isInstanceOf(DistributedLockException.class);
  }

  @Test
  void lock_interrupted_throwsDistributedLockException() throws Exception {
    when(mockMutex.acquire(anyLong(), any(TimeUnit.class)))
        .thenThrow(new InterruptedException("test"));
    ZookeeperDistributedLock lock = newLockWithMockMutex(LockType.MUTEX);
    try {
      assertThatThrownBy(() -> lock.lock())
          .isInstanceOf(DistributedLockException.class);
      assertThat(Thread.currentThread().isInterrupted()).isTrue();
    } finally {
      Thread.interrupted();
    }
  }

  @Test
  void lock_otherException_throwsDistributedLockException() throws Exception {
    when(mockMutex.acquire(anyLong(), any(TimeUnit.class)))
        .thenThrow(new RuntimeException("zk error"));
    ZookeeperDistributedLock lock = newLockWithMockMutex(LockType.MUTEX);
    assertThatThrownBy(() -> lock.lock())
        .isInstanceOf(DistributedLockException.class);
  }

  // ==================== lockInterruptibly() ====================

  @Test
  void lockInterruptibly_success_acquiresLock() throws Exception {
    doNothing().when(mockMutex).acquire(); // acquire() returns void, just don't throw
    ZookeeperDistributedLock lock = newLockWithMockMutex(LockType.MUTEX);
    lock.lockInterruptibly();
    assertThat(lock.getHoldTime()).isGreaterThanOrEqualTo(0);
  }

  @Test
  void lockInterruptibly_interrupted_throwsInterruptedException() throws Exception {
    doThrow(new InterruptedException("test")).when(mockMutex).acquire();
    ZookeeperDistributedLock lock = newLockWithMockMutex(LockType.MUTEX);
    try {
      assertThatThrownBy(() -> lock.lockInterruptibly())
          .isInstanceOf(InterruptedException.class);
      assertThat(Thread.currentThread().isInterrupted()).isTrue();
    } finally {
      Thread.interrupted();
    }
  }

  @Test
  void lockInterruptibly_otherException_throwsDistributedLockException() throws Exception {
    doThrow(new RuntimeException("zk error")).when(mockMutex).acquire();
    ZookeeperDistributedLock lock = newLockWithMockMutex(LockType.MUTEX);
    assertThatThrownBy(() -> lock.lockInterruptibly())
        .isInstanceOf(DistributedLockException.class);
  }

  // ==================== tryLock() ====================

  @Test
  void tryLock_success_returnsTrue() throws Exception {
    when(mockMutex.acquire(anyLong(), any(TimeUnit.class))).thenReturn(true);
    ZookeeperDistributedLock lock = newLockWithMockMutex(LockType.MUTEX);
    assertThat(lock.tryLock()).isTrue();
  }

  @Test
  void tryLock_fail_returnsFalse() throws Exception {
    when(mockMutex.acquire(anyLong(), any(TimeUnit.class))).thenReturn(false);
    ZookeeperDistributedLock lock = newLockWithMockMutex(LockType.MUTEX);
    assertThat(lock.tryLock()).isFalse();
  }

  @Test
  void tryLock_exception_throwsDistributedLockException() throws Exception {
    when(mockMutex.acquire(anyLong(), any(TimeUnit.class)))
        .thenThrow(new RuntimeException("error"));
    ZookeeperDistributedLock lock = newLockWithMockMutex(LockType.MUTEX);
    assertThatThrownBy(() -> lock.tryLock())
        .isInstanceOf(DistributedLockException.class);
  }

  // ==================== tryLock(long, TimeUnit) ====================

  @Test
  void tryLockWithTimeout_success_returnsTrue() throws Exception {
    when(mockMutex.acquire(anyLong(), any(TimeUnit.class))).thenReturn(true);
    ZookeeperDistributedLock lock = newLockWithMockMutex(LockType.MUTEX);
    assertThat(lock.tryLock(1, TimeUnit.SECONDS)).isTrue();
  }

  @Test
  void tryLockWithTimeout_fail_returnsFalse() throws Exception {
    when(mockMutex.acquire(anyLong(), any(TimeUnit.class))).thenReturn(false);
    ZookeeperDistributedLock lock = newLockWithMockMutex(LockType.MUTEX);
    assertThat(lock.tryLock(1, TimeUnit.SECONDS)).isFalse();
  }

  @Test
  void tryLockWithTimeout_interrupted_throwsInterruptedException() throws Exception {
    when(mockMutex.acquire(anyLong(), any(TimeUnit.class)))
        .thenThrow(new InterruptedException("test"));
    ZookeeperDistributedLock lock = newLockWithMockMutex(LockType.MUTEX);
    try {
      assertThatThrownBy(() -> lock.tryLock(1, TimeUnit.SECONDS))
          .isInstanceOf(InterruptedException.class);
      assertThat(Thread.currentThread().isInterrupted()).isTrue();
    } finally {
      Thread.interrupted();
    }
  }

  @Test
  void tryLockWithTimeout_otherException_throwsDistributedLockException() throws Exception {
    when(mockMutex.acquire(anyLong(), any(TimeUnit.class)))
        .thenThrow(new RuntimeException("error"));
    ZookeeperDistributedLock lock = newLockWithMockMutex(LockType.MUTEX);
    assertThatThrownBy(() -> lock.tryLock(1, TimeUnit.SECONDS))
        .isInstanceOf(DistributedLockException.class);
  }

  // ==================== unlock() ====================

  @Test
  void unlock_success_doesNotThrow() throws Exception {
    // release() returns void, just don't throw
    ZookeeperDistributedLock lock = newLockWithMockMutex(LockType.MUTEX);
    lock.unlock();
  }

  @Test
  void unlock_exception_throwsDistributedLockException() throws Exception {
    doThrow(new RuntimeException("release error")).when(mockMutex).release();
    ZookeeperDistributedLock lock = newLockWithMockMutex(LockType.MUTEX);
    assertThatThrownBy(() -> lock.unlock())
        .isInstanceOf(DistributedLockException.class);
  }

  // ==================== isHeldByCurrentThread() ====================

  @Test
  void isHeldByCurrentThread_true_returnsTrue() throws Exception {
    when(mockMutex.isOwnedByCurrentThread()).thenReturn(true);
    ZookeeperDistributedLock lock = newLockWithMockMutex(LockType.MUTEX);
    assertThat(lock.isHeldByCurrentThread()).isTrue();
  }

  @Test
  void isHeldByCurrentThread_false_returnsFalse() throws Exception {
    when(mockMutex.isOwnedByCurrentThread()).thenReturn(false);
    ZookeeperDistributedLock lock = newLockWithMockMutex(LockType.MUTEX);
    assertThat(lock.isHeldByCurrentThread()).isFalse();
  }

  // ==================== forceUnlock() ====================

  @Test
  void forceUnlock_held_callsUnlock() throws Exception {
    when(mockMutex.isOwnedByCurrentThread()).thenReturn(true);
    ZookeeperDistributedLock lock = newLockWithMockMutex(LockType.MUTEX);
    lock.forceUnlock();
  }

  @Test
  void forceUnlock_notHeld_doesNotThrow() throws Exception {
    when(mockMutex.isOwnedByCurrentThread()).thenReturn(false);
    ZookeeperDistributedLock lock = newLockWithMockMutex(LockType.MUTEX);
    lock.forceUnlock();
  }

  @Test
  void forceUnlock_unlockThrows_throwsDistributedLockException() throws Exception {
    when(mockMutex.isOwnedByCurrentThread()).thenReturn(true);
    doThrow(new RuntimeException("error")).when(mockMutex).release();
    ZookeeperDistributedLock lock = newLockWithMockMutex(LockType.MUTEX);
    assertThatThrownBy(() -> lock.forceUnlock())
        .isInstanceOf(DistributedLockException.class);
  }

  // ==================== getHoldTime() ====================

  @Test
  void getHoldTime_notAcquired_returnsMinusOne() throws Exception {
    ZookeeperDistributedLock lock = newLockWithMockMutex(LockType.MUTEX);
    assertThat(lock.getHoldTime()).isEqualTo(-1);
  }

  @Test
  void getHoldTime_acquired_returnsPositive() throws Exception {
    when(mockMutex.acquire(anyLong(), any(TimeUnit.class))).thenReturn(true);
    ZookeeperDistributedLock lock = newLockWithMockMutex(LockType.MUTEX);
    lock.lock();
    assertThat(lock.getHoldTime()).isGreaterThanOrEqualTo(0);
  }

  // ==================== exists() ====================

  @Test
  void exists_pathExists_returnsTrue() throws Exception {
    ExistsBuilder existsBuilder = mock(ExistsBuilder.class);
    when(curator.checkExists()).thenReturn(existsBuilder);
    when(existsBuilder.forPath(anyString())).thenReturn(new Stat());

    ZookeeperDistributedLock lock = newLockWithMockMutex(LockType.MUTEX);
    assertThat(lock.exists()).isTrue();
  }

  @Test
  void exists_pathNotExists_returnsFalse() throws Exception {
    ExistsBuilder existsBuilder = mock(ExistsBuilder.class);
    when(curator.checkExists()).thenReturn(existsBuilder);
    when(existsBuilder.forPath(anyString())).thenReturn(null);

    ZookeeperDistributedLock lock = newLockWithMockMutex(LockType.MUTEX);
    assertThat(lock.exists()).isFalse();
  }

  @Test
  void exists_throwsException_returnsFalse() throws Exception {
    when(curator.checkExists()).thenThrow(new RuntimeException("zk error"));

    ZookeeperDistributedLock lock = newLockWithMockMutex(LockType.MUTEX);
    assertThat(lock.exists()).isFalse();
  }

  // ==================== getMetadata() ====================

  @Test
  void getMetadata_notAcquired_returnsDefaults() throws Exception {
    ZookeeperDistributedLock lock = newLockWithMockMutex(LockType.MUTEX);
    LockMetadata metadata = lock.getMetadata();

    assertThat(metadata.getCreateTime()).isGreaterThan(0);
    assertThat(metadata.getLastUpdateTime()).isEqualTo(metadata.getCreateTime());
    assertThat(metadata.getHolder()).isEqualTo(Thread.currentThread().getName());
    assertThat(metadata.getReentrantCount()).isEqualTo(-1);
    assertThat(metadata.getExpireTime()).isEqualTo(-1);
    assertThat(metadata.getAttribute("foo")).isNull();
  }

  @Test
  void getMetadata_acquired_returnsAcquireTime() throws Exception {
    when(mockMutex.acquire(anyLong(), any(TimeUnit.class))).thenReturn(true);
    ZookeeperDistributedLock lock = newLockWithMockMutex(LockType.MUTEX);
    lock.lock();

    LockMetadata metadata = lock.getMetadata();
    assertThat(metadata.getLastUpdateTime()).isGreaterThanOrEqualTo(metadata.getCreateTime());
  }

  // ==================== getLockKey / getLockType ====================

  @Test
  void getLockKey_returnsKey() throws Exception {
    ZookeeperDistributedLock lock = newLockWithMockMutex(LockType.READ);
    assertThat(lock.getLockKey()).isEqualTo("test-key");
  }

  @Test
  void getLockType_returnsType() throws Exception {
    ZookeeperDistributedLock lock = newLockWithMockMutex(LockType.WRITE);
    assertThat(lock.getLockType()).isEqualTo(LockType.WRITE);
  }

  // ==================== newCondition / toString ====================

  @Test
  void newCondition_throwsUnsupportedOperation() throws Exception {
    ZookeeperDistributedLock lock = newLockWithMockMutex(LockType.MUTEX);
    assertThatThrownBy(() -> lock.newCondition())
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void toString_returnsFormattedString() throws Exception {
    when(mockMutex.isOwnedByCurrentThread()).thenReturn(false);
    ZookeeperDistributedLock lock = newLockWithMockMutex(LockType.MUTEX);
    String str = lock.toString();
    assertThat(str).contains("ZookeeperDistributedLock")
        .contains("test-key")
        .contains("/locks/test")
        .contains("MUTEX");
  }
}
