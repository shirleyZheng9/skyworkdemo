package com.iwhalecloud.bote.common.lock.zookeeper;

import com.iwhalecloud.bote.common.lock.DistributedLock;
import com.iwhalecloud.bote.common.lock.DistributedLockException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于Zookeeper的分布式锁实现
 *
 * <p>基于Apache Curator框架实现，继承抽象分布式锁接口。</p>
 *
 * <p>特性：</p>
 * <ul>
 *   <li>分布式互斥：确保集群环境下的资源独占访问</li>
 *   <li>可重入性：同一线程可多次获取同一锁</li>
 *   <li>故障恢复：节点故障时自动释放锁</li>
 *   <li>公平性：按照请求顺序获取锁（FIFO）</li>
 * </ul>
 *
 * @since 2025-01-28
 */
public class ZookeeperDistributedLock implements DistributedLock {

  private static final Logger logger = LoggerFactory.getLogger(ZookeeperDistributedLock.class);

  /** Curator客户端 */
  private final CuratorFramework curatorFramework;

  /** 锁标识 */
  private final String lockKey;

  /** 锁路径 */
  private final String lockPath;

  /** 互斥锁实现 */
  private final InterProcessMutex mutex;

  /** 锁类型 */
  private final LockType lockType;

  /** 锁创建时间 */
  private final long createTime;

  /** 最大等待时间（毫秒） */
  private final long maxWaitTimeMs;

  /** 锁获取时间 */
  private volatile long acquireTime = -1;

  /**
   * 构造函数 - 创建互斥锁
   *
   * @param curatorFramework Curator客户端
   * @param lockKey 锁标识
   * @param lockPath 锁在ZooKeeper中的路径
   */
  @SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
  public ZookeeperDistributedLock(CuratorFramework curatorFramework, String lockKey, String lockPath) {
    this(curatorFramework, lockKey, lockPath, LockType.MUTEX);
  }

  /**
   * 构造函数 - 创建指定类型的锁
   *
   * @param curatorFramework Curator客户端
   * @param lockKey 锁标识
   * @param lockPath 锁在ZooKeeper中的路径
   * @param lockType 锁类型
   */
  @SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
  public ZookeeperDistributedLock(CuratorFramework curatorFramework, String lockKey,
                                  String lockPath, LockType lockType) {
    this(curatorFramework, lockKey, lockPath, lockType, 30000); // 默认30秒最大等待时间
  }

  /**
   * 构造函数 - 创建指定类型的锁（完整参数）
   *
   * @param curatorFramework Curator客户端
   * @param lockKey 锁标识
   * @param lockPath 锁在ZooKeeper中的路径
   * @param lockType 锁类型
   * @param maxWaitTimeMs 最大等待时间（毫秒）
   */
  @SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
  public ZookeeperDistributedLock(CuratorFramework curatorFramework, String lockKey,
                                  String lockPath, LockType lockType, long maxWaitTimeMs) {
    if (curatorFramework == null) {
      throw new IllegalArgumentException("CuratorFramework不能为空");
    }
    if (lockKey == null || lockKey.trim().isEmpty()) {
      throw new IllegalArgumentException("锁标识不能为空");
    }
    if (lockPath == null || lockPath.trim().isEmpty()) {
      throw new IllegalArgumentException("锁路径不能为空");
    }
    if (lockType == null) {
      throw new IllegalArgumentException("锁类型不能为空");
    }
    if (maxWaitTimeMs <= 0) {
      throw new IllegalArgumentException("最大等待时间必须大于0");
    }

    this.curatorFramework = curatorFramework;
    this.lockKey = lockKey;
    this.lockPath = lockPath;
    this.lockType = lockType;
    this.maxWaitTimeMs = maxWaitTimeMs;
    this.createTime = System.currentTimeMillis();

    if (lockType == LockType.MUTEX) {
      this.mutex = new InterProcessMutex(curatorFramework, lockPath);
    }
    else {
      InterProcessReadWriteLock readWriteLock = new InterProcessReadWriteLock(curatorFramework, lockPath);
      this.mutex = (lockType == LockType.READ) ? readWriteLock.readLock() : readWriteLock.writeLock();
    }
    logger.debug("创建Zookeeper分布式锁: key={}, path={}, type={}, maxWait={}ms", lockKey, lockPath, lockType, maxWaitTimeMs);
  }

  @Override
  public void lock() {
    try {
      logger.debug("尝试获取锁: key={}, type={}, maxWait={}ms", lockKey, lockType, maxWaitTimeMs);

      long startTime = System.currentTimeMillis();

      // 使用maxWaitTimeMs限制等待时间
      boolean acquired = mutex.acquire(maxWaitTimeMs, TimeUnit.MILLISECONDS);

      if (!acquired) {
        long elapsedTime = System.currentTimeMillis() - startTime;
        throw new DistributedLockException(
          DistributedLockException.ErrorCodes.ACQUIRE_TIMEOUT,
          String.format("获取锁超时: %s, 等待时间: %dms", lockKey, elapsedTime),
          lockKey
        );
      }

      acquireTime = System.currentTimeMillis();
      long waitTime = acquireTime - startTime;
      logger.debug("成功获取锁: key={}, type={}, 等待时间: {}ms", lockKey, lockType, waitTime);
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.debug("获取锁被中断: key={}, type={}", lockKey, lockType);
      throw new DistributedLockException(
        DistributedLockException.ErrorCodes.ACQUIRE_TIMEOUT,
        "获取锁被中断: " + lockKey,
        lockKey,
        e
      );
    }
    catch (DistributedLockException e) {
      // 重新抛出自定义异常
      throw e;
    }
    catch (Exception e) {
      logger.error("获取锁失败: key={}, type={}", lockKey, lockType, e);
      throw new DistributedLockException(
        DistributedLockException.ErrorCodes.ACQUIRE_TIMEOUT,
        "获取锁失败: " + lockKey,
        lockKey,
        e
      );
    }
  }

  @Override
  public void lockInterruptibly() throws InterruptedException {
    try {
      logger.debug("尝试获取锁(可中断): key={}, type={}", lockKey, lockType);
      mutex.acquire();
      acquireTime = System.currentTimeMillis();
      logger.debug("成功获取锁(可中断): key={}, type={}", lockKey, lockType);
    }
    catch (InterruptedException e) {
      logger.debug("获取锁被中断: key={}, type={}", lockKey, lockType);
      Thread.currentThread().interrupt();
      throw e;
    }
    catch (Exception e) {
      logger.error("获取锁失败(可中断): key={}, type={}", lockKey, lockType, e);
      throw new DistributedLockException(
        DistributedLockException.ErrorCodes.ACQUIRE_TIMEOUT,
        "获取锁失败: " + lockKey,
        lockKey,
        e
      );
    }
  }

  @Override
  public boolean tryLock() {
    try {
      logger.debug("尝试获取锁(非阻塞): key={}, type={}", lockKey, lockType);
      boolean acquired = mutex.acquire(0, TimeUnit.MILLISECONDS);
      if (acquired) {
        acquireTime = System.currentTimeMillis();
        logger.debug("成功获取锁(非阻塞): key={}, type={}", lockKey, lockType);
      }
      else {
        logger.debug("获取锁失败(非阻塞): key={}, type={}", lockKey, lockType);
      }
      return acquired;
    }
    catch (Exception e) {
      logger.error("尝试获取锁失败(非阻塞): key={}, type={}", lockKey, lockType, e);
      throw new DistributedLockException(
        DistributedLockException.ErrorCodes.ACQUIRE_TIMEOUT,
        "尝试获取锁失败: " + lockKey,
        lockKey,
        e
      );
    }
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
    try {
      logger.debug("尝试获取锁(超时): key={}, type={}, timeout={}ms", lockKey, lockType, unit.toMillis(time));
      boolean acquired = mutex.acquire(time, unit);
      if (acquired) {
        acquireTime = System.currentTimeMillis();
        logger.debug("成功获取锁(超时): key={}, type={}", lockKey, lockType);
      }
      else {
        logger.debug("获取锁超时: key={}, type={}, timeout={}ms",
          lockKey, lockType, unit.toMillis(time));
      }
      return acquired;
    }
    catch (InterruptedException e) {
      logger.debug("获取锁被中断(超时): key={}, type={}", lockKey, lockType);
      Thread.currentThread().interrupt();
      throw e;
    }
    catch (Exception e) {
      logger.error("尝试获取锁失败(超时): key={}, type={}", lockKey, lockType, e);
      throw new DistributedLockException(
        DistributedLockException.ErrorCodes.ACQUIRE_TIMEOUT,
        "尝试获取锁失败: " + lockKey,
        lockKey,
        e
      );
    }
  }

  @Override
  public void unlock() {
    try {
      logger.debug("尝试释放锁: key={}, type={}", lockKey, lockType);
      mutex.release();
      acquireTime = -1;
      logger.debug("成功释放锁: key={}, type={}", lockKey, lockType);
    }
    catch (Exception e) {
      logger.error("释放锁失败: key={}, type={}", lockKey, lockType, e);
      throw new DistributedLockException(
        DistributedLockException.ErrorCodes.RELEASE_FAILED,
        "释放锁失败: " + lockKey,
        lockKey,
        e
      );
    }
  }

  @Override
  public Condition newCondition() {
    throw new UnsupportedOperationException("Zookeeper分布式锁不支持Condition");
  }

  @Override
  public boolean isHeldByCurrentThread() {
    return mutex.isOwnedByCurrentThread();
  }

  @Override
  public String getLockKey() {
    return lockKey;
  }

  @Override
  public LockType getLockType() {
    return lockType;
  }

  @Override
  public void forceUnlock() {
    try {
      logger.warn("强制释放锁: key={}, type={}", lockKey, lockType);
      // 注意：Curator的InterProcessMutex不支持强制释放，这里记录警告
      if (isHeldByCurrentThread()) {
        unlock();
      }
      else {
        logger.warn("当前线程未持有锁，无法强制释放: key={}", lockKey);
      }
    }
    catch (Exception e) {
      logger.error("强制释放锁失败: key={}, type={}", lockKey, lockType, e);
      throw new DistributedLockException(
        DistributedLockException.ErrorCodes.RELEASE_FAILED,
        "强制释放锁失败: " + lockKey,
        lockKey,
        e
      );
    }
  }

  @Override
  public long getHoldTime() {
    return acquireTime > 0 ? System.currentTimeMillis() - acquireTime : -1;
  }

  @Override
  public boolean exists() {
    try {
      return curatorFramework.checkExists().forPath(lockPath) != null;
    }
    catch (Exception e) {
      logger.error("检查锁是否存在失败: key={}, path={}", lockKey, lockPath, e);
      return false;
    }
  }

  @Override
  public LockMetadata getMetadata() {
    return new ZookeeperLockMetadata();
  }

  @Override
  public String toString() {
    return String.format("ZookeeperDistributedLock{key='%s', path='%s', type=%s, isHeld=%s}",
      lockKey, lockPath, lockType, isHeldByCurrentThread());
  }

  /**
   * Zookeeper锁元数据实现
   */
  private final class ZookeeperLockMetadata implements LockMetadata {

    @Override
    public long getCreateTime() {
      return createTime;
    }

    @Override
    public long getLastUpdateTime() {
      return acquireTime > 0 ? acquireTime : createTime;
    }

    @Override
    public String getHolder() {
      return Thread.currentThread().getName();
    }

    @Override
    public int getReentrantCount() {
      // Curator不直接提供重入次数，返回-1表示不支持
      return -1;
    }

    @Override
    public long getExpireTime() {
      // Zookeeper锁没有固定过期时间，返回-1
      return -1;
    }

    @Override
    public String getAttribute(String key) {
      // 基础实现不支持自定义属性
      return null;
    }
  }
}
