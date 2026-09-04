/**
 * 通用分布式锁框架
 *
 * <p>本包提供了一套完整的分布式锁解决方案，采用抽象接口设计，支持多种分布式锁实现，
 * 遵循Java标准Lock接口规范，适用于分布式环境下的资源同步和互斥访问控制。</p>
 *
 * <h2>核心抽象组件</h2>
 * <ul>
 *   <li>{@link com.iwhalecloud.bote.common.lock.DistributedLock} - 通用分布式锁接口</li>
 *   <li>{@link com.iwhalecloud.bote.common.lock.DistributedLockFactory} - 通用分布式锁工厂接口</li>
 *   <li>{@link com.iwhalecloud.bote.common.lock.DistributedLockException} - 通用分布式锁异常类</li>
 *   <li>{@link com.iwhalecloud.bote.common.lock.AbstractDistributedLockProperties} - 通用配置抽象类</li>
 * </ul>
 *
 * <h2>具体实现</h2>
 *
 * <h3>Zookeeper实现</h3>
 * <ul>
 *   <li>{@link com.iwhalecloud.bote.common.lock.zookeeper.ZookeeperDistributedLock} - Zookeeper分布式锁实现</li>
 *   <li>{@link com.iwhalecloud.bote.common.lock.zookeeper.ZookeeperDistributedLockFactory} - Zookeeper锁工厂</li>
 *   <li>{@link com.iwhalecloud.bote.common.lock.zookeeper.ZookeeperDistributedLockProperties} - Zookeeper配置</li>
 *   <li>{@link com.iwhalecloud.bote.common.lock.zookeeper.ZookeeperDistributedLockAutoConfiguration} - Zookeeper自动配置</li>
 * </ul>
 *
 * <h3>Redis实现</h3>
 * <ul>
 *   <li>{@link com.iwhalecloud.bote.common.lock.redis.RedisDistributedLock} - Redis分布式锁</li>
 *   <li>{@link com.iwhalecloud.bote.common.lock.redis.RedisDistributedLockFactory} - Redis锁工厂</li>
 *   <li>{@link com.iwhalecloud.bote.common.lock.redis.RedisDistributedLockProperties} - Redis配置</li>
 * </ul>
 *
 * <h2>主要特性</h2>
 * <ul>
 *   <li><strong>多实现支持</strong>：支持Zookeeper、Redis等多种分布式锁实现</li>
 *   <li><strong>统一接口</strong>：基于抽象接口，可无缝切换不同实现</li>
 *   <li><strong>分布式互斥</strong>：确保集群环境下的资源独占访问</li>
 *   <li><strong>可重入性</strong>：同一线程可多次获取同一锁</li>
 *   <li><strong>故障恢复</strong>：节点故障时自动释放锁</li>
 *   <li><strong>公平性</strong>：按照请求顺序获取锁（FIFO）</li>
 *   <li><strong>多种锁类型</strong>：支持互斥锁、读锁、写锁</li>
 *   <li><strong>超时控制</strong>：支持阻塞获取和超时获取</li>
 *   <li><strong>元数据支持</strong>：提供锁的详细元数据信息</li>
 *   <li><strong>缓存优化</strong>：支持锁实例缓存和性能统计</li>
 * </ul>
 *
 * <h2>Zookeeper实现配置</h2>
 * <p>在application.yml中添加以下配置：</p>
 * <pre>
 * bote:
 *   lock:
 *     zookeeper:
 *       enabled: true
 *       connect-string: localhost:2181
 *       session-timeout-ms: 30000
 *       connection-timeout-ms: 15000
 *       base-sleep-time-ms: 1000
 *       max-retries: 3
 *       namespace: bote-locks
 *       lock-base-path: /locks
 *       cache-enabled: true
 *       max-cache-size: 1000
 * </pre>
 *
 * <h2>Redis实现配置</h2>
 * <pre>
 * bote:
 *   lock:
 *     redis:
 *       enabled: true
 *       namespace: bote-locks
 *       lock-base-path: locks
 *       lock-ttl-ms: 30000
 *       watchdog-enabled: true
 * </pre>
 *
 * <h2>基本使用示例</h2>
 * <pre>{@code
 * @Autowired
 * private DistributedLockFactory lockFactory; // 可以是任何实现
 *
 * public void doSomething() {
 *     DistributedLock lock = lockFactory.getMutexLock("my-lock");
 *     try {
 *         lock.lock();
 *         // 执行需要同步的业务逻辑
 *
 *         // 获取锁信息
 *         System.out.println("锁类型: " + lock.getLockType());
 *         System.out.println("持有时间: " + lock.getHoldTime() + "ms");
 *         System.out.println("锁标识: " + lock.getLockKey());
 *     } finally {
 *         if (lock.isHeldByCurrentThread()) {
 *             lock.unlock();
 *         }
 *     }
 * }
 * }</pre>
 *
 * <h2>高级使用示例</h2>
 * <pre>{@code
 * // 带超时的锁获取
 * if (lock.tryLock(5, TimeUnit.SECONDS)) {
 *     try {
 *         // 业务逻辑
 *     } finally {
 *         lock.unlock();
 *     }
 * } else {
 *     // 处理获取锁超时
 * }
 *
 * // 租户级别锁
 * DistributedLock tenantLock = lockFactory.getTenantLock(tenantId, "operation");
 *
 * // 业务锁
 * DistributedLock bizLock = lockFactory.getBizLock("payment", orderId);
 *
 * // 读写锁
 * DistributedLock readLock = lockFactory.getReadLock("data-access");
 * DistributedLock writeLock = lockFactory.getWriteLock("data-access");
 *
 * // 带过期时间的锁
 * DistributedLock expireLock = lockFactory.getLockWithExpire("temp-lock",
 *     DistributedLock.LockType.MUTEX, 60000);
 *
 * // 获取锁元数据
 * DistributedLock.LockMetadata metadata = lock.getMetadata();
 * System.out.println("创建时间: " + metadata.getCreateTime());
 * System.out.println("持有者: " + metadata.getHolder());
 * }</pre>
 *
 * <h2>工厂信息获取</h2>
 * <pre>{@code
 * // 获取工厂类型
 * String factoryType = lockFactory.getFactoryType(); // "zookeeper" 或 "redis"
 *
 * // 检查工厂可用性
 * boolean available = lockFactory.isAvailable();
 *
 * // 获取缓存统计
 * DistributedLockFactory.CacheStats stats = lockFactory.getCacheStats();
 * System.out.println("缓存命中率: " + stats.getHitRate());
 *
 * // 获取工厂配置
 * DistributedLockFactory.FactoryConfig config = lockFactory.getConfig();
 * System.out.println("连接字符串: " + config.getConnectionString());
 * }</pre>
 *
 * <h2>注意事项</h2>
 * <ul>
 *   <li>确保在finally块中释放锁，避免死锁</li>
 *   <li>合理设置锁的超时时间，避免长时间阻塞</li>
 *   <li>锁的粒度要适中，避免性能问题</li>
 *   <li>监控分布式锁服务的连接状态，确保高可用性</li>
 *   <li>选择合适的锁实现：Zookeeper适合强一致性场景，Redis适合高性能场景</li>
 *   <li>在高并发场景下，注意锁的争用和性能影响</li>
 *   <li>考虑锁的过期策略，防止死锁和资源泄露</li>
 * </ul>
 *
 * @since 2025-08-21
 */
package com.iwhalecloud.bote.common.lock;
