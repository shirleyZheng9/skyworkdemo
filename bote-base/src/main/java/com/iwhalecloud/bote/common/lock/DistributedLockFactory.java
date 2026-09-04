package com.iwhalecloud.bote.common.lock;

import java.util.Map;

/**
 * 分布式锁工厂抽象接口
 *
 * <p>定义了创建和管理分布式锁实例的通用方法，支持多种实现方式。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>创建各种类型的分布式锁</li>
 *   <li>管理锁实例的生命周期</li>
 *   <li>提供业务场景相关的锁创建方法</li>
 *   <li>支持锁缓存和性能优化</li>
 * </ul>
 *
 * @since 2025-08-21
 */
public interface DistributedLockFactory {

  /**
   * 获取工厂类型标识
   *
   * @return 工厂类型（如"zookeeper", "redis", "etcd"等）
   */
  String getFactoryType();

  /**
   * 检查工厂是否可用
   *
   * @return true 如果工厂可用
   */
  boolean isAvailable();

  /**
   * 获取互斥锁
   *
   * @param lockKey 锁标识
   * @return 分布式互斥锁实例
   * @throws DistributedLockException 如果创建锁失败
   */
  DistributedLock getMutexLock(String lockKey);

  /**
   * 获取读锁
   *
   * @param lockKey 锁标识
   * @return 分布式读锁实例
   * @throws DistributedLockException 如果创建锁失败
   */
  DistributedLock getReadLock(String lockKey);

  /**
   * 获取写锁
   *
   * @param lockKey 锁标识
   * @return 分布式写锁实例
   * @throws DistributedLockException 如果创建锁失败
   */
  DistributedLock getWriteLock(String lockKey);

  /**
   * 获取指定类型的锁
   *
   * @param lockKey 锁标识
   * @param lockType 锁类型
   * @return 分布式锁实例
   * @throws DistributedLockException 如果创建锁失败
   */
  DistributedLock getLock(String lockKey, DistributedLock.LockType lockType);

  /**
   * 创建新的锁实例（不使用缓存）
   *
   * @param lockKey 锁标识
   * @param lockType 锁类型
   * @return 新的分布式锁实例
   * @throws DistributedLockException 如果创建锁失败
   */
  DistributedLock createLock(String lockKey, DistributedLock.LockType lockType);

  /**
   * 根据业务标识获取锁
   *
   * <p>适用于业务场景，自动根据业务类型和ID构建锁标识</p>
   *
   * @param bizType 业务类型
   * @param bizId 业务ID
   * @return 分布式互斥锁实例
   * @throws DistributedLockException 如果创建锁失败
   */
  DistributedLock getBizLock(String bizType, String bizId);

  /**
   * 根据业务标识获取指定类型的锁
   *
   * @param bizType 业务类型
   * @param bizId 业务ID
   * @param lockType 锁类型
   * @return 分布式锁实例
   * @throws DistributedLockException 如果创建锁失败
   */
  DistributedLock getBizLock(String bizType, String bizId, DistributedLock.LockType lockType);

  /**
   * 获取带过期时间的锁
   *
   * @param lockKey 锁标识
   * @param lockType 锁类型
   * @param expireTimeMs 过期时间（毫秒）
   * @return 分布式锁实例
   * @throws DistributedLockException 如果创建锁失败
   */
  DistributedLock getLockWithExpire(String lockKey, DistributedLock.LockType lockType, long expireTimeMs);

  /**
   * 获取带属性的锁
   *
   * @param lockKey 锁标识
   * @param lockType 锁类型
   * @param attributes 锁属性
   * @return 分布式锁实例
   * @throws DistributedLockException 如果创建锁失败
   */
  DistributedLock getLockWithAttributes(String lockKey, DistributedLock.LockType lockType,
                                        Map<String, String> attributes);

  /**
   * 检查锁是否存在
   *
   * @param lockKey 锁标识
   * @return true 如果锁存在
   */
  boolean lockExists(String lockKey);

  /**
   * 强制删除锁（谨慎使用）
   *
   * @param lockKey 锁标识
   * @throws DistributedLockException 如果删除失败
   */
  void forceDeleteLock(String lockKey);

  /**
   * 清除锁缓存
   *
   * @param lockKey 锁标识（可选），如果为空则清除所有缓存
   */
  void clearCache(String lockKey);

  /**
   * 关闭工厂，释放资源
   */
  void shutdown();

  /**
   * 工厂配置信息接口
   */
  interface FactoryConfig {

    /**
     * 获取连接字符串或地址
     *
     * @return 连接信息
     */
    String getConnectionString();

    /**
     * 获取命名空间
     *
     * @return 命名空间
     */
    String getNamespace();

    /**
     * 获取锁根路径/前缀
     *
     * @return 根路径
     */
    String getLockBasePath();

    /**
     * 是否启用缓存
     *
     * @return true 如果启用缓存
     */
    boolean isCacheEnabled();

    /**
     * 获取配置属性
     *
     * @param key 属性键
     * @return 属性值
     */
    String getProperty(String key);
  }
}
