package com.iwhalecloud.bote.common.lock.redis;

import com.iwhalecloud.bote.common.lock.DistributedLock;
import com.iwhalecloud.bote.common.lock.DistributedLockException;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于Redis的分布式锁实现
 *
 * <p>特性：</p>
 * <ul>
 *   <li>基于SET命令的原子性</li>
 *   <li>支持过期时间防止死锁</li>
 *   <li>使用UUID作为锁标识防止误解锁</li>
 * </ul>
 *
 * @since 2025-08-21
 */
public class RedisDistributedLock implements DistributedLock {

  private static final Logger logger = LoggerFactory.getLogger(RedisDistributedLock.class);

  /** Redis客户端 */
  private final ICacheClient cacheClient;

  /** 锁标识 */
  private final String lockKey;

  /** 锁类型 */
  private final LockType lockType;

  /** 锁值（UUID） */
  private final String lockValue;

  /** 锁TTL（毫秒） */
  private final long lockTtlMs;

  /** 最大等待时间（毫秒） */
  private final long maxWaitTimeMs;

  /** 锁创建时间 */
  private final long createTime;

  /** 锁获取时间 */
  private volatile long acquireTime = -1;

  /** 是否被当前线程持有 */
  private volatile boolean heldByCurrentThread = false;

  /**
   * 构造函数（完整参数）
   *
   * @param cacheClient Redis客户端
   * @param lockKey 锁标识
   * @param lockType 锁类型
   * @param lockTtlMs 锁TTL（毫秒）
   * @param maxWaitTimeMs 最大等待时间（毫秒）
   */
  @SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
  public RedisDistributedLock(ICacheClient cacheClient, String lockKey, LockType lockType, long lockTtlMs, long maxWaitTimeMs) {
    // 先进行参数验证
    validateConstructorParameters(cacheClient, lockKey, lockType, lockTtlMs, maxWaitTimeMs);

    // 然后设置所有字段
    this.cacheClient = cacheClient;
    this.lockKey = lockKey;
    this.lockType = lockType;
    this.lockTtlMs = lockTtlMs;
    this.maxWaitTimeMs = maxWaitTimeMs;
    this.lockValue = generateLockValue();
    this.createTime = System.currentTimeMillis();

    logger.trace("创建Redis分布式锁: key={}, type={}, ttl={}ms, maxWait={}ms", lockKey, lockType, lockTtlMs, maxWaitTimeMs);
  }

  /**
   * 创建Redis分布式锁的静态工厂方法
   *
   * @param cacheClient Redis客户端
   * @param lockKey 锁标识
   * @param lockType 锁类型
   * @param lockTtlMs 锁TTL（毫秒）
   * @return Redis分布式锁实例
   * @throws IllegalArgumentException 如果参数无效
   */
  public static RedisDistributedLock create(ICacheClient cacheClient, String lockKey, LockType lockType, long lockTtlMs) {
    return create(cacheClient, lockKey, lockType, lockTtlMs, 30000);
  }

  /**
   * 创建Redis分布式锁的静态工厂方法
   *
   * @param cacheClient Redis客户端
   * @param lockKey 锁标识
   * @param lockType 锁类型
   * @param lockTtlMs 锁TTL（毫秒）
   * @param maxWaitTimeMs 最大等待时间（毫秒）
   * @return Redis分布式锁实例
   * @throws IllegalArgumentException 如果参数无效
   */
  public static RedisDistributedLock create(ICacheClient cacheClient,
                                            String lockKey,
                                            LockType lockType,
                                            long lockTtlMs,
                                            long maxWaitTimeMs) {
    validateConstructorParameters(cacheClient, lockKey, lockType, lockTtlMs, maxWaitTimeMs);
    return new RedisDistributedLock(cacheClient, lockKey, lockType, lockTtlMs, maxWaitTimeMs);
  }

  /**
   * 验证构造函数参数
   *
   * @param cacheClient Redis客户端
   * @param lockKey 锁标识
   * @param lockType 锁类型
   * @param lockTtlMs 锁TTL（毫秒）
   * @param maxWaitTimeMs 最大等待时间（毫秒）
   * @throws IllegalArgumentException 如果参数无效
   */
  private static void validateConstructorParameters(ICacheClient cacheClient, String lockKey, LockType lockType, long lockTtlMs, long maxWaitTimeMs) {
    if (cacheClient == null) {
      throw new IllegalArgumentException("缓存客户端不能为空");
    }
    if (lockKey == null || lockKey.trim().isEmpty()) {
      throw new IllegalArgumentException("锁标识不能为空");
    }
    if (lockType == null) {
      throw new IllegalArgumentException("锁类型不能为空");
    }
    if (lockTtlMs <= 0) {
      throw new IllegalArgumentException("锁TTL必须大于0");
    }
    if (maxWaitTimeMs <= 0) {
      throw new IllegalArgumentException("最大等待时间必须大于0");
    }
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public void lock() {
    try {
      logger.trace("尝试获取锁: key={}, type={}, maxWait={}ms", lockKey, lockType, maxWaitTimeMs);

      long startTime = System.currentTimeMillis();
      long retryInterval = 100; // 重试间隔100ms

      // 使用maxWaitTimeMs限制总等待时间
      while (!tryAcquireLock()) {
        long elapsedTime = System.currentTimeMillis() - startTime;

        // 检查是否超时
        if (elapsedTime >= maxWaitTimeMs) {
          throw new DistributedLockException(
            DistributedLockException.ErrorCodes.ACQUIRE_TIMEOUT,
            String.format("获取锁超时: %s, 等待时间: %dms", lockKey, elapsedTime),
            lockKey
          );
        }

        try {
          // 计算剩余等待时间，避免超时
          long remainingTime = maxWaitTimeMs - elapsedTime;
          long sleepTime = Math.min(retryInterval, remainingTime);

          if (sleepTime > 0) {
            Thread.sleep(sleepTime);
          }
        }
        catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new DistributedLockException(
            DistributedLockException.ErrorCodes.ACQUIRE_TIMEOUT,
            "获取锁被中断: " + lockKey,
            lockKey,
            e
          );
        }
      }

      acquireTime = System.currentTimeMillis();
      heldByCurrentThread = true;
      logger.trace("成功获取锁: key={}, type={}, 等待时间: {}ms", lockKey, lockType, acquireTime - startTime);

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
      logger.trace("尝试获取锁(可中断): key={}, type={}", lockKey, lockType);

      // 无限重试直到获取到锁，但响应中断
      while (!tryAcquireLock()) {
        Thread.sleep(100); // 100ms后重试，会响应中断
      }
      acquireTime = System.currentTimeMillis();
      heldByCurrentThread = true;
      logger.trace("成功获取锁(可中断): key={}, type={}", lockKey, lockType);

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
      logger.trace("尝试获取锁(非阻塞): key={}, type={}", lockKey, lockType);
      boolean acquired = tryAcquireLock();

      if (acquired) {
        acquireTime = System.currentTimeMillis();
        heldByCurrentThread = true;
        logger.trace("成功获取锁(非阻塞): key={}, type={}", lockKey, lockType);
      }
      else {
        logger.trace("获取锁失败(非阻塞): key={}, type={}", lockKey, lockType);
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
      logger.trace("尝试获取锁(超时): key={}, type={}, timeout={}ms",
        lockKey, lockType, unit.toMillis(time));

      long timeoutMs = unit.toMillis(time);
      long deadline = System.currentTimeMillis() + timeoutMs;

      while (System.currentTimeMillis() < deadline) {
        if (tryAcquireLock()) {
          acquireTime = System.currentTimeMillis();
          heldByCurrentThread = true;
          logger.debug("成功获取锁(超时): key={}, type={}", lockKey, lockType);
          return true;
        }
        Thread.sleep(Math.min(100, timeoutMs)); // 100ms后重试
      }

      logger.trace("获取锁超时: key={}, type={}, timeout={}ms", lockKey, lockType, timeoutMs);
      return false;

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
      if (!heldByCurrentThread) {
        logger.warn("尝试释放未持有的锁: key={}, type={}", lockKey, lockType);
        return;
      }

      logger.trace("尝试释放锁: key={}, type={}", lockKey, lockType);

      // 使用Lua脚本确保原子性删除
      boolean released = tryReleaseLock();

      if (released) {
        heldByCurrentThread = false;
        acquireTime = -1;
        logger.trace("成功释放锁: key={}, type={}", lockKey, lockType);
      }
      else {
        logger.warn("释放锁失败，可能已过期: key={}, type={}", lockKey, lockType);
      }

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
    throw new UnsupportedOperationException("Redis分布式锁不支持Condition");
  }

  @Override
  public boolean isHeldByCurrentThread() {
    // 除了检查本地状态，还要验证Redis中的锁值是否匹配
    return heldByCurrentThread && checkLockValueMatch();
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
      // 强制删除锁，不检查锁值
      boolean deleted = forceDeleteLock();
      if (deleted) {
        heldByCurrentThread = false;
        acquireTime = -1;
        logger.debug("成功强制释放锁: key={}, type={}", lockKey, lockType);
      }
      else {
        logger.warn("强制释放锁失败，锁可能不存在: key={}, type={}", lockKey, lockType);
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
      // 模拟检查Redis中是否存在该key
      return checkLockExists();
    }
    catch (Exception e) {
      logger.error("检查锁是否存在失败: key={}", lockKey, e);
      return false;
    }
  }

  @Override
  public LockMetadata getMetadata() {
    return new RedisLockMetadata();
  }

  /**
   * 生成锁值
   */
  private String generateLockValue() {
    return UUID.randomUUID() + ":" + Thread.currentThread().threadId();
  }

  /**
   * 尝试获取锁
   */
  private boolean tryAcquireLock() {
    try {
      // 使用 SET key value NX PX ttl 命令获取锁
      // NX: 仅当键不存在时设置
      // PX: 设置过期时间（毫秒）
      logger.trace("执行: SET {} {} NX PX {}", lockKey, lockValue, lockTtlMs);
      // 使用setIfAbsent方法
      Boolean result = cacheClient.opsForValue().setIfAbsent(
        lockKey,
        lockValue,
        lockTtlMs,
        TimeUnit.MILLISECONDS
      );
      boolean acquired = Boolean.TRUE.equals(result);
      logger.trace("获取锁结果: key={}, acquired={}", lockKey, acquired);
      return acquired;
    }
    catch (Exception e) {
      logger.error("执行获取锁命令失败: key={}", lockKey, e);
      return false;
    }
  }

  /**
   * 尝试释放锁
   */
  private boolean tryReleaseLock() {
    try {
      // todo 此处ctgcache可能不兼容
      // 使用Lua脚本确保原子性：只有当锁的值匹配时才删除
      String luaScript =
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
          "  return redis.call('del', KEYS[1]) " +
          "else " +
          "  return 0 " +
          "end";

      logger.trace("执行Lua脚本释放锁: key={}, value={}", lockKey, lockValue);

      // 执行Lua脚本
      Object result = cacheClient.execute(luaScript, lockKey, lockValue);

      boolean released = "1".equals(String.valueOf(result)) || Long.valueOf(1).equals(result);
      logger.trace("释放锁结果: key={}, released={}", lockKey, released);

      return released;
    }
    catch (Exception e) {
      logger.error("执行释放锁Lua脚本失败: key={}", lockKey, e);
      return false;
    }
  }

  /**
   * 强制删除锁
   */
  private boolean forceDeleteLock() {
    try {
      // 直接删除锁，不检查锁值
      logger.trace("执行: DEL {}", lockKey);
      boolean deleted = cacheClient.delete(lockKey);
      logger.trace("强制删除锁结果: key={}, deleted={}", lockKey, deleted);
      return deleted;
    }
    catch (Exception e) {
      logger.error("执行强制删除锁命令失败: key={}", lockKey, e);
      return false;
    }
  }

  /**
   * 检查锁是否存在
   */
  private boolean checkLockExists() {
    try {
      // 检查Redis中是否存在该key
      logger.trace("执行: EXISTS {}", lockKey);
      boolean hasKey = cacheClient.hasKey(lockKey);
      logger.trace("检查锁存在结果: key={}, exists={}", lockKey, hasKey);
      return hasKey;
    }
    catch (Exception e) {
      logger.error("执行检查锁存在命令失败: key={}", lockKey, e);
      return false;
    }
  }

  /**
   * 检查锁值是否匹配当前线程
   */
  private boolean checkLockValueMatch() {
    try {
      String currentValue = cacheClient.opsForValue().get(lockKey);
      boolean matches = lockValue.equals(currentValue);

      logger.trace("检查锁值匹配结果: key={}, expected={}, actual={}, matches={}",
        lockKey, lockValue, currentValue, matches);
      return matches;
    }
    catch (Exception e) {
      logger.error("检查锁值匹配失败: key={}", lockKey, e);
      return false;
    }
  }

  @Override
  public String toString() {
    return String.format("RedisDistributedLock{key='%s', type=%s, value='%s', isHeld=%s}",
      lockKey, lockType, lockValue, heldByCurrentThread);
  }

  /**
   * Redis锁元数据实现
   */
  private final class RedisLockMetadata implements LockMetadata {

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
      return lockValue;
    }

    @Override
    public int getReentrantCount() {
      // 基础实现不支持重入计数
      return -1;
    }

    @Override
    public long getExpireTime() {
      return acquireTime > 0 ? acquireTime + lockTtlMs : -1;
    }

    @Override
    public String getAttribute(String key) {
      // 基础实现不支持自定义属性
      return null;
    }
  }
}
