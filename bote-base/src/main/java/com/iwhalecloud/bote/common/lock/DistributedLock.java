package com.iwhalecloud.bote.common.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import lombok.Getter;

/**
 * 分布式锁通用接口
 *
 * <p>扩展了标准的{@link Lock}接口，专门为分布式环境设计，提供了额外的分布式锁特性。</p>
 *
 * <p>主要扩展功能：</p>
 * <ul>
 *   <li>锁持有状态检查</li>
 *   <li>锁标识信息获取</li>
 *   <li>锁类型支持</li>
 *   <li>强制释放功能</li>
 * </ul>
 *
 * <p>支持的锁类型：</p>
 * <ul>
 *   <li>{@link LockType#MUTEX} - 互斥锁</li>
 *   <li>{@link LockType#READ} - 读锁</li>
 *   <li>{@link LockType#WRITE} - 写锁</li>
 * </ul>
 *
 * @since 2025-08-21
 */
public interface DistributedLock extends Lock {

  /**
   * 检查锁是否被当前线程持有
   *
   * @return true 如果当前线程持有此锁
   */
  boolean isHeldByCurrentThread();

  /**
   * 获取锁的唯一标识
   *
   * @return 锁的唯一标识符
   */
  String getLockKey();

  /**
   * 获取锁类型
   *
   * @return 锁类型
   */
  LockType getLockType();

  /**
   * 尝试在指定时间内获取锁，支持可中断
   *
   * @param time 等待时间数值
   * @param unit 时间单位
   * @param interruptible 是否支持中断
   * @return true 如果在指定时间内获取到锁，false 如果超时
   * @throws InterruptedException 当线程被中断且interruptible为true时抛出
   * @throws DistributedLockException 当获取锁过程中发生其他错误时抛出
   */
  default boolean tryLock(long time, TimeUnit unit, boolean interruptible)
    throws InterruptedException {
    if (interruptible) {
      return tryLock(time, unit);
    }
    else {
      try {
        return tryLock(time, unit);
      }
      catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return false;
      }
    }
  }

  /**
   * 强制释放锁（谨慎使用）
   *
   * <p>此方法会强制释放锁，即使当前线程不是锁的持有者。
   * 主要用于异常情况下的锁清理，使用时需要特别谨慎。</p>
   *
   * @throws DistributedLockException 当强制释放锁过程中发生错误时抛出
   */
  void forceUnlock();

  /**
   * 获取锁的持有时间（毫秒）
   *
   * @return 锁被持有的时间，如果锁未被持有则返回-1
   */
  long getHoldTime();

  /**
   * 检查锁是否存在（无论是否被当前线程持有）
   *
   * @return true 如果锁存在
   */
  boolean exists();

  /**
   * 获取锁的元数据信息
   *
   * @return 锁的元数据信息
   */
  LockMetadata getMetadata();

  /**
   * 锁类型枚举
   */
  @Getter
  enum LockType {
    /** 互斥锁 */
    MUTEX("mutex"),
    /** 读锁 */
    READ("read"),
    /** 写锁 */
    WRITE("write");

    private final String code;

    LockType(String code) {
      this.code = code;
    }

  }

  /**
   * 锁元数据接口
   */
  interface LockMetadata {

    /**
     * 获取锁的创建时间
     *
     * @return 锁的创建时间戳（毫秒）
     */
    long getCreateTime();

    /**
     * 获取锁的最后更新时间
     *
     * @return 锁的最后更新时间戳（毫秒）
     */
    long getLastUpdateTime();

    /**
     * 获取锁的持有者信息
     *
     * @return 锁的持有者标识
     */
    String getHolder();

    /**
     * 获取锁的重入次数
     *
     * @return 重入次数，如果不支持可重入则返回-1
     */
    int getReentrantCount();

    /**
     * 获取锁的过期时间
     *
     * @return 过期时间戳（毫秒），如果没有过期时间则返回-1
     */
    long getExpireTime();

    /**
     * 获取锁的额外属性
     *
     * @param key 属性键
     * @return 属性值，如果不存在则返回null
     */
    String getAttribute(String key);
  }
}
