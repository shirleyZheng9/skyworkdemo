package com.iwhalecloud.bote.common.lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.iwhalecloud.bote.common.lock.DistributedLock.LockMetadata;
import com.iwhalecloud.bote.common.lock.DistributedLock.LockType;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import org.junit.jupiter.api.Test;

/**
 * {@link DistributedLock} 接口默认方法单元测试
 *
 * <p>覆盖 default 方法 {@link DistributedLock#tryLock(long, TimeUnit, boolean)} 的两个分支：
 * interruptible=true 时直接委托 tryLock(time, unit)（含 InterruptedException 向上传播）；
 * interruptible=false 时捕获 InterruptedException、重设中断标志并返回 false。
 * 使用最小 Stub 实现接口以隔离默认方法逻辑。</p>
 */
class DistributedLockDefaultMethodTest {

  /**
   * 最小 Stub 实现：仅控制 tryLock(long, TimeUnit) 的返回值与是否抛出 InterruptedException，
   * 其余方法返回默认值。
   */
  private static final class StubLock implements DistributedLock {
    private final boolean tryLockResult;
    private final boolean throwInterrupted;

    StubLock(boolean tryLockResult, boolean throwInterrupted) {
      this.tryLockResult = tryLockResult;
      this.throwInterrupted = throwInterrupted;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
      if (throwInterrupted) {
        throw new InterruptedException("stub");
      }
      return tryLockResult;
    }

    @Override
    public void lock() {
      // no-op
    }

    @Override
    public void lockInterruptibly() {
      // no-op
    }

    @Override
    public boolean tryLock() {
      return false;
    }

    @Override
    public void unlock() {
      // no-op
    }

    @Override
    public Condition newCondition() {
      return null;
    }

    @Override
    public boolean isHeldByCurrentThread() {
      return false;
    }

    @Override
    public String getLockKey() {
      return "stub";
    }

    @Override
    public LockType getLockType() {
      return LockType.MUTEX;
    }

    @Override
    public void forceUnlock() {
      // no-op
    }

    @Override
    public long getHoldTime() {
      return -1;
    }

    @Override
    public boolean exists() {
      return false;
    }

    @Override
    public LockMetadata getMetadata() {
      return null;
    }
  }

  // ==================== interruptible = true ====================

  @Test
  void tryLockWithInterruptible_true_returnsTrue() throws InterruptedException {
    StubLock lock = new StubLock(true, false);
    assertThat(lock.tryLock(1, TimeUnit.SECONDS, true)).isTrue();
  }

  @Test
  void tryLockWithInterruptible_true_returnsFalse() throws InterruptedException {
    StubLock lock = new StubLock(false, false);
    assertThat(lock.tryLock(1, TimeUnit.SECONDS, true)).isFalse();
  }

  @Test
  void tryLockWithInterruptible_true_propagatesInterruptedException() {
    StubLock lock = new StubLock(false, true);
    assertThatThrownBy(() -> lock.tryLock(1, TimeUnit.SECONDS, true))
        .isInstanceOf(InterruptedException.class);
  }

  // ==================== interruptible = false ====================

  @Test
  void tryLockWithInterruptible_false_returnsTrue() throws InterruptedException {
    StubLock lock = new StubLock(true, false);
    assertThat(lock.tryLock(1, TimeUnit.SECONDS, false)).isTrue();
  }

  @Test
  void tryLockWithInterruptible_false_returnsFalse() throws InterruptedException {
    StubLock lock = new StubLock(false, false);
    assertThat(lock.tryLock(1, TimeUnit.SECONDS, false)).isFalse();
  }

  @Test
  void tryLockWithInterruptible_false_catchesInterruptedExceptionAndReturnsFalse() throws InterruptedException {
    StubLock lock = new StubLock(false, true);
    try {
      assertThat(lock.tryLock(1, TimeUnit.SECONDS, false)).isFalse();
      // 验证中断标志被重设
      assertThat(Thread.currentThread().isInterrupted()).isTrue();
    } finally {
      // 清理中断标志，避免影响后续测试
      Thread.interrupted();
    }
  }

  // ==================== LockType enum ====================

  @Test
  void lockType_getCode_returnsExpectedCode() {
    assertThat(LockType.MUTEX.getCode()).isEqualTo("mutex");
    assertThat(LockType.READ.getCode()).isEqualTo("read");
    assertThat(LockType.WRITE.getCode()).isEqualTo("write");
  }
}
