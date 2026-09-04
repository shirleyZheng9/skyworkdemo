package com.iwhalecloud.bote.common.lock.redis;

import com.iwhalecloud.bote.common.lock.AbstractDistributedLockProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Redis分布式锁配置属性
 *
 * <p>继承通用配置，添加Redis特有的配置项。</p>
 *
 * <p>Redis特有配置项：</p>
 * <ul>
 *   <li>cache-group: 缓存组名称</li>
 *   <li>key-prefix: 缓存键前缀</li>
 *   <li>lock-ttl-ms: 锁的默认TTL（毫秒）</li>
 *   <li>watchdog-interval-ms: 看门狗续期间隔</li>
 *   <li>watchdog-enabled: 是否启用看门狗自动续期</li>
 * </ul>
 *
 * @author Aiqing
 * @since 2025-08-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = "bote.lock.redis")
public class RedisDistributedLockProperties extends AbstractDistributedLockProperties {

  /** 缓存组名称 */
  private String cacheGroup = "distributed-lock";

  /** 缓存键前缀 */
  private String keyPrefix = "redis-lock";

  @Override
  public String getConnectionString() {
    // Redis分布式锁使用项目统一的缓存配置，无需单独连接字符串
    return String.format("cache-group:%s,key-prefix:%s", cacheGroup, keyPrefix);
  }

  @Override
  public void setConnectionString(String connectionString) {
    // Redis分布式锁使用项目统一的缓存配置，不需要设置连接字符串
  }

  @Override
  public String getImplementationType() {
    return "redis";
  }

  @Override
  protected void validateCustom() {
    super.validateCustom();

    if (cacheGroup == null || cacheGroup.trim().isEmpty()) {
      throw new IllegalArgumentException("缓存组名称不能为空");
    }

    if (keyPrefix == null || keyPrefix.trim().isEmpty()) {
      throw new IllegalArgumentException("缓存键前缀不能为空");
    }
  }

  /**
   * 获取完整的缓存键前缀
   *
   * @return 包含命名空间的完整键前缀
   */
  public String getFullKeyPrefix() {
    if (getNamespace() != null && !getNamespace().trim().isEmpty()) {
      return getNamespace() + ":" + keyPrefix;
    }
    return keyPrefix;
  }
}
