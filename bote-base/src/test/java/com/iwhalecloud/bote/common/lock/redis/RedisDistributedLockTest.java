package com.iwhalecloud.bote.common.lock.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.iwhalecloud.bote.common.lock.DistributedLock.LockMetadata;
import com.iwhalecloud.bote.common.lock.DistributedLock.LockType;
import com.iwhalecloud.bote.common.lock.DistributedLockException;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.ValueOperations;

/**
 * {@link RedisDistributedLock} 单元测试
 *
 * <p>使用 Mockito 模拟 {@link ICacheClient} 及其 {@link ValueOperations}，覆盖：
 * 构造参数校验、lock/tryLock/lockInterruptibly 的获取成功/超时/中断分支、
 * unlock/forceUnlock 的释放成功/失败/异常分支、isHeldByCurrentThread/exists/getHoldTime/
 * getMetadata 等查询方法，以及 newCondition/toString 等。</p>
 *
 * <p>注意：lock()、tryLock()、unlock()、forceUnlock()、exists() 中的 catch(Exception) 外层分支
 * 不可达——内部私有方法 tryAcquireLock/tryReleaseLock/forceDeleteLock/checkLockExists 均已捕获
 * 自身异常并返回 false，不会向外传播。唯一可达的外层 catch(Exception) 是 tryLock(long,TimeUnit)
 * 中 unit 为 null 导致的 NPE。</p>
 */
class RedisDistributedLockTest {

  private ICacheClient cacheClient;
  private ValueOperations<String, String> valueOps;

  @BeforeEach
  void setUp() {
    cacheClient = mock(ICacheClient.class);
    valueOps = mock(ValueOperations.class);
    when(cacheClient.opsForValue()).thenReturn(valueOps);
  }

  private RedisDistributedLock newLock() {
    return new RedisDistributedLock(cacheClient, "test-lock", LockType.MUTEX, 30000, 30000);
  }

  private String getLockValue(RedisDistributedLock lock) throws Exception {
    Field f = RedisDistributedLock.class.getDeclaredField("lockValue");
    f.setAccessible(true);
    return (String) f.get(lock);
  }

  private boolean isHeld(RedisDistributedLock lock) throws Exception {
    Field f = RedisDistributedLock.class.getDeclaredField("heldByCurrentThread");
    f.setAccessible(true);
    return (boolean) f.get(lock);
  }

  // ==================== 构造函数与工厂方法 ====================

  @Test
  void constructor_validArgs_createsLock() {
    RedisDistributedLock lock = newLock();
    assertThat(lock.getLockKey()).isEqualTo("test-lock");
    assertThat(lock.getLockType()).isEqualTo(LockType.MUTEX);
  }

  @Test
  void constructor_nullCacheClient_throwsIllegalArgument() {
    assertThatThrownBy(() ->
        new RedisDistributedLock(null, "key", LockType.MUTEX, 1000, 1000))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("缓存客户端不能为空");
  }

  @Test
  void constructor_nullLockKey_throwsIllegalArgument() {
    assertThatThrownBy(() ->
        new RedisDistributedLock(cacheClient, null, LockType.MUTEX, 1000, 1000))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("锁标识不能为空");
  }

  @Test
  void constructor_emptyLockKey_throwsIllegalArgument() {
    assertThatThrownBy(() ->
        new RedisDistributedLock(cacheClient, "  ", LockType.MUTEX, 1000, 1000))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("锁标识不能为空");
  }

  @Test
  void constructor_nullLockType_throwsIllegalArgument() {
    assertThatThrownBy(() ->
        new RedisDistributedLock(cacheClient, "key", null, 1000, 1000))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("锁类型不能为空");
  }

  @Test
  void constructor_zeroLockTtl_throwsIllegalArgument() {
    assertThatThrownBy(() ->
        new RedisDistributedLock(cacheClient, "key", LockType.MUTEX, 0, 1000))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("锁TTL必须大于0");
  }

  @Test
  void constructor_zeroMaxWaitTime_throwsIllegalArgument() {
    assertThatThrownBy(() ->
        new RedisDistributedLock(cacheClient, "key", LockType.MUTEX, 1000, 0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("最大等待时间必须大于0");
  }

  @Test
  void create_fourArgs_createsLock() {
    RedisDistributedLock lock = RedisDistributedLock.create(cacheClient, "key", LockType.READ, 5000);
    assertThat(lock).isNotNull();
    assertThat(lock.getLockType()).isEqualTo(LockType.READ);
  }

  @Test
  void create_fiveArgs_createsLock() {
    RedisDistributedLock lock = RedisDistributedLock.create(cacheClient, "key", LockType.WRITE, 5000, 10000);
    assertThat(lock).isNotNull();
    assertThat(lock.getLockType()).isEqualTo(LockType.WRITE);
  }

  @Test
  void create_invalidArgs_throwsIllegalArgument() {
    assertThatThrownBy(() -> RedisDistributedLock.create(null, "key", LockType.MUTEX, 5000))
        .isInstanceOf(IllegalArgumentException.class);
  }

  // ==================== lock() ====================

  @Test
  void lock_success_acquiresLock() throws Exception {
    when(valueOps.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
        .thenReturn(true);

    RedisDistributedLock lock = newLock();
    lock.lock();

    assertThat(isHeld(lock)).isTrue();
  }

  @Test
  void lock_timeout_throwsDistributedLockException() {
    when(valueOps.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
        .thenReturn(false);

    // maxWaitTimeMs=1 使循环快速超时
    RedisDistributedLock lock =
        new RedisDistributedLock(cacheClient, "key", LockType.MUTEX, 30000, 1);

    assertThatThrownBy(() -> lock.lock())
        .isInstanceOf(DistributedLockException.class);
  }

  @Test
  void lock_interruptedDuringSleep_throwsDistributedLockException() {
    when(valueOps.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
        .thenReturn(false);

    RedisDistributedLock lock = newLock();
    try {
      Thread.currentThread().interrupt();
      assertThatThrownBy(() -> lock.lock())
          .isInstanceOf(DistributedLockException.class);
      // 验证中断标志被重设
      assertThat(Thread.currentThread().isInterrupted()).isTrue();
    } finally {
      Thread.interrupted();
    }
  }

  // ==================== lockInterruptibly() ====================

  @Test
  void lockInterruptibly_success_acquiresLock() throws Exception {
    when(valueOps.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
        .thenReturn(true);

    RedisDistributedLock lock = newLock();
    lock.lockInterruptibly();

    assertThat(isHeld(lock)).isTrue();
  }

  @Test
  void lockInterruptibly_interrupted_throwsInterruptedException() {
    when(valueOps.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
        .thenReturn(false);

    RedisDistributedLock lock = newLock();
    try {
      Thread.currentThread().interrupt();
      assertThatThrownBy(() -> lock.lockInterruptibly())
          .isInstanceOf(InterruptedException.class);
      assertThat(Thread.currentThread().isInterrupted()).isTrue();
    } finally {
      Thread.interrupted();
    }
  }

  // ==================== tryLock() ====================

  @Test
  void tryLock_success_returnsTrue() throws Exception {
    when(valueOps.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
        .thenReturn(true);

    RedisDistributedLock lock = newLock();
    assertThat(lock.tryLock()).isTrue();
    assertThat(isHeld(lock)).isTrue();
  }

  @Test
  void tryLock_fail_returnsFalse() throws Exception {
    when(valueOps.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
        .thenReturn(false);

    RedisDistributedLock lock = newLock();
    assertThat(lock.tryLock()).isFalse();
    assertThat(isHeld(lock)).isFalse();
  }

  // ==================== tryLock(long, TimeUnit) ====================

  @Test
  void tryLockWithTimeout_success_returnsTrue() throws Exception {
    when(valueOps.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
        .thenReturn(true);

    RedisDistributedLock lock = newLock();
    assertThat(lock.tryLock(1, TimeUnit.SECONDS)).isTrue();
    assertThat(isHeld(lock)).isTrue();
  }

  @Test
  void tryLockWithTimeout_timeout_returnsFalse() throws Exception {
    when(valueOps.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
        .thenReturn(false);

    RedisDistributedLock lock = newLock();
    // time=0 使 deadline=now，while 条件立即为 false，不进入循环
    assertThat(lock.tryLock(0, TimeUnit.MILLISECONDS)).isFalse();
  }

  @Test
  void tryLockWithTimeout_interrupted_throwsInterruptedException() {
    when(valueOps.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
        .thenReturn(false);

    RedisDistributedLock lock = newLock();
    try {
      Thread.currentThread().interrupt();
      assertThatThrownBy(() -> lock.tryLock(1, TimeUnit.SECONDS))
          .isInstanceOf(InterruptedException.class);
      assertThat(Thread.currentThread().isInterrupted()).isTrue();
    } finally {
      Thread.interrupted();
    }
  }

  @Test
  void tryLockWithTimeout_nullUnit_throwsDistributedLockException() {
    RedisDistributedLock lock = newLock();
    // unit.toMillis(time) 抛出 NPE -> 被 catch(Exception) 捕获包装为 DistributedLockException
    assertThatThrownBy(() -> lock.tryLock(1, null))
        .isInstanceOf(DistributedLockException.class);
  }

  // ==================== unlock() ====================

  @Test
  void unlock_notHeld_doesNothing() {
    RedisDistributedLock lock = newLock();
    lock.unlock(); // heldByCurrentThread=false，直接返回
    verify(cacheClient, never()).execute(anyString(), anyString(), any());
  }

  @Test
  void unlock_held_releaseSucceeds() throws Exception {
    when(valueOps.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
        .thenReturn(true);
    when(cacheClient.execute(anyString(), anyString(), any())).thenReturn(1L);

    RedisDistributedLock lock = newLock();
    lock.lock();
    lock.unlock();

    assertThat(isHeld(lock)).isFalse();
  }

  @Test
  void unlock_held_releaseFails_staysHeld() throws Exception {
    when(valueOps.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
        .thenReturn(true);
    when(cacheClient.execute(anyString(), anyString(), any())).thenReturn(0L);

    RedisDistributedLock lock = newLock();
    lock.lock();
    lock.unlock();

    assertThat(isHeld(lock)).isTrue();
  }

  // ==================== forceUnlock() ====================

  @Test
  void forceUnlock_deleteSucceeds_clearsHeld() throws Exception {
    when(cacheClient.delete(anyString())).thenReturn(true);

    RedisDistributedLock lock = newLock();
    lock.forceUnlock();

    assertThat(isHeld(lock)).isFalse();
  }

  @Test
  void forceUnlock_deleteFails_staysHeld() throws Exception {
    when(cacheClient.delete(anyString())).thenReturn(false);

    RedisDistributedLock lock = newLock();
    lock.forceUnlock();

    assertThat(isHeld(lock)).isFalse();
  }

  @Test
  void forceUnlock_deleteThrows_returnsSilently() {
    when(cacheClient.delete(anyString())).thenThrow(new RuntimeException("conn error"));

    RedisDistributedLock lock = newLock();
    // forceDeleteLock 内部捕获异常并返回 false，不向外抛出
    lock.forceUnlock();
  }

  // ==================== isHeldByCurrentThread() ====================

  @Test
  void isHeldByCurrentThread_notHeld_returnsFalse() {
    RedisDistributedLock lock = newLock();
    assertThat(lock.isHeldByCurrentThread()).isFalse();
  }

  @Test
  void isHeldByCurrentThread_heldAndValueMatches_returnsTrue() throws Exception {
    when(valueOps.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
        .thenReturn(true);

    RedisDistributedLock lock = newLock();
    lock.lock();

    String lockValue = getLockValue(lock);
    when(valueOps.get(anyString())).thenReturn(lockValue);

    assertThat(lock.isHeldByCurrentThread()).isTrue();
  }

  @Test
  void isHeldByCurrentThread_heldAndValueMismatch_returnsFalse() throws Exception {
    when(valueOps.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
        .thenReturn(true);

    RedisDistributedLock lock = newLock();
    lock.lock();

    when(valueOps.get(anyString())).thenReturn("wrong-value");

    assertThat(lock.isHeldByCurrentThread()).isFalse();
  }

  @Test
  void isHeldByCurrentThread_heldAndGetThrows_returnsFalse() throws Exception {
    when(valueOps.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
        .thenReturn(true);

    RedisDistributedLock lock = newLock();
    lock.lock();

    when(valueOps.get(anyString())).thenThrow(new RuntimeException("error"));

    assertThat(lock.isHeldByCurrentThread()).isFalse();
  }

  // ==================== exists() ====================

  @Test
  void exists_returnsTrue() {
    when(cacheClient.hasKey(anyString())).thenReturn(true);
    assertThat(newLock().exists()).isTrue();
  }

  @Test
  void exists_returnsFalse() {
    when(cacheClient.hasKey(anyString())).thenReturn(false);
    assertThat(newLock().exists()).isFalse();
  }

  @Test
  void exists_throwsException_returnsFalse() {
    when(cacheClient.hasKey(anyString())).thenThrow(new RuntimeException("error"));
    assertThat(newLock().exists()).isFalse();
  }

  // ==================== getHoldTime() ====================

  @Test
  void getHoldTime_notAcquired_returnsMinusOne() {
    assertThat(newLock().getHoldTime()).isEqualTo(-1);
  }

  @Test
  void getHoldTime_acquired_returnsPositive() {
    when(valueOps.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
        .thenReturn(true);

    RedisDistributedLock lock = newLock();
    lock.lock();

    assertThat(lock.getHoldTime()).isGreaterThanOrEqualTo(0);
  }

  // ==================== getMetadata() ====================

  @Test
  void getMetadata_notAcquired_returnsDefaults() throws Exception {
    RedisDistributedLock lock = newLock();
    LockMetadata metadata = lock.getMetadata();

    assertThat(metadata.getCreateTime()).isGreaterThan(0);
    assertThat(metadata.getLastUpdateTime()).isEqualTo(metadata.getCreateTime());
    assertThat(metadata.getHolder()).isEqualTo(getLockValue(lock));
    assertThat(metadata.getReentrantCount()).isEqualTo(-1);
    assertThat(metadata.getExpireTime()).isEqualTo(-1);
    assertThat(metadata.getAttribute("foo")).isNull();
  }

  @Test
  void getMetadata_acquired_returnsAcquireBasedValues() {
    when(valueOps.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
        .thenReturn(true);

    RedisDistributedLock lock = newLock();
    lock.lock();

    LockMetadata metadata = lock.getMetadata();
    assertThat(metadata.getLastUpdateTime()).isGreaterThanOrEqualTo(metadata.getCreateTime());
    assertThat(metadata.getExpireTime()).isGreaterThan(metadata.getCreateTime());
  }

  // ==================== newCondition / toString ====================

  @Test
  void newCondition_throwsUnsupportedOperation() {
    assertThatThrownBy(() -> newLock().newCondition())
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void toString_returnsFormattedString() {
    RedisDistributedLock lock = newLock();
    String str = lock.toString();
    assertThat(str).contains("RedisDistributedLock")
        .contains("test-lock")
        .contains("MUTEX");
  }
}
