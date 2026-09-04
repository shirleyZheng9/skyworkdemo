package com.iwhalecloud.bote.common.lock.redis;

import com.iwhalecloud.bote.common.lock.DistributedLockFactory;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redis分布式锁自动配置类
 *
 * <p>负责配置和管理Redis分布式锁相关的Bean注册。</p>
 *
 * <p>配置项说明：</p>
 * <ul>
 *   <li>bote.lock.redis.enabled: 是否启用Redis分布式锁，默认false</li>
 *   <li>bote.lock.redis.cache-group: 缓存组名称，默认"distributed-lock"</li>
 *   <li>bote.lock.redis.key-prefix: 缓存键前缀，默认"redis-lock"</li>
 * </ul>
 *
 * @author Aiqing
 * @since 2025-08-21
 */
@Configuration
@ConditionalOnBooleanProperty("bote.lock.redis.enabled")
@EnableConfigurationProperties(RedisDistributedLockProperties.class)
public class RedisDistributedLockAutoConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(RedisDistributedLockAutoConfiguration.class);

  /**
   * 配置Redis分布式锁缓存客户端
   *
   * @param cacheFactory 缓存工厂
   * @param properties Redis分布式锁配置属性
   * @return ICacheClient实例
   */
  @SuppressWarnings("PMD.GuardLogStatement")
  private ICacheClient createRedisDistributedLockCacheClient(CacheFactory cacheFactory,
                                                             RedisDistributedLockProperties properties) {
    // 验证配置
    properties.validate();
    logger.info("初始化Redis分布式锁缓存客户端: cacheGroup={}, keyPrefix={}",
      properties.getCacheGroup(), properties.getFullKeyPrefix());
    // 使用项目统一的缓存工厂创建ICacheClient
    ICacheClient cacheClient = cacheFactory.getCacheClient(properties.getCacheGroup(), properties.getFullKeyPrefix());
    logger.info("Redis分布式锁缓存客户端初始化完成");
    return cacheClient;
  }

  /**
   * 配置Redis分布式锁工厂
   *
   * @param cacheFactory cacheFactory
   * @param properties Redis分布式锁配置属性
   * @return Redis分布式锁工厂实例
   */
  @Bean
  @SuppressWarnings("PMD.GuardLogStatement")
  public DistributedLockFactory redisDistributedLockFactory(CacheFactory cacheFactory,
                                                            RedisDistributedLockProperties properties) {
    ICacheClient redisDistributedLockCacheClient = createRedisDistributedLockCacheClient(cacheFactory, properties);
    RedisDistributedLockFactory factory = new RedisDistributedLockFactory(
      redisDistributedLockCacheClient, properties);

    logger.info("Redis分布式锁工厂初始化完成: factoryType={}", factory.getFactoryType());
    return factory;
  }

}
