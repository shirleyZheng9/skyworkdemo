package com.iwhalecloud.bote.common.lock;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;

/**
 * 分布式锁配置抽象基类
 *
 * <p>提供了分布式锁通用配置的基础实现，各种具体的锁实现可以继承此类
 * 来扩展特定的配置项。</p>
 *
 * <p>通用配置项包括：</p>
 * <ul>
 *   <li>基础连接配置</li>
 *   <li>锁的生命周期配置</li>
 *   <li>性能优化配置</li>
 *   <li>扩展属性支持</li>
 * </ul>
 *
 * @since 2025-08-21
 */
@Data
public abstract class AbstractDistributedLockProperties {

  /** 是否启用分布式锁 */
  private boolean enabled = false;

  /** 命名空间 */
  private String namespace = "bote-locks";

  /** 锁根路径/前缀 */
  private String lockBasePath = "/locks";

  /** 默认锁过期时间（毫秒） */
  private long defaultExpireTimeMs = 300000; // 5分钟

  /** 最大锁等待时间（毫秒） */
  private long maxWaitTimeMs = 30000; // 30秒

  /** 是否启用锁缓存 */
  private boolean cacheEnabled = true;

  /** 缓存最大大小 */
  private int maxCacheSize = 1000;

  /** 缓存过期时间（毫秒） */
  private long cacheExpireTimeMs = 600000; // 10分钟

  /** 锁重试次数 */
  private int maxRetries = 3;

  /** 重试间隔时间（毫秒） */
  private long retryIntervalMs = 1000;

  /** 扩展属性 */
  private Map<String, String> properties = new HashMap<>();

  /**
   * 获取连接字符串或地址
   *
   * @return 连接信息
   */
  public abstract String getConnectionString();

  /**
   * 设置连接字符串或地址
   *
   * @param connectionString 连接信息
   */
  public abstract void setConnectionString(String connectionString);

  /**
   * 获取具体实现类型
   *
   * @return 实现类型（如"zookeeper", "redis", "etcd"等）
   */
  public abstract String getImplementationType();

  /**
   * 验证配置的有效性
   *
   * @throws IllegalArgumentException 如果配置无效
   */
  public void validate() {
    if (enabled) {
      validateRequired();
      validateRanges();
      validateCustom();
    }
  }

  /**
   * 验证必需的配置项
   *
   * @throws IllegalArgumentException 如果必需配置缺失
   */
  protected void validateRequired() {
    String connectionString = getConnectionString();
    if (connectionString == null || connectionString.trim().isEmpty()) {
      throw new IllegalArgumentException("连接字符串不能为空");
    }

    if (namespace == null || namespace.trim().isEmpty()) {
      throw new IllegalArgumentException("命名空间不能为空");
    }

    if (lockBasePath == null || lockBasePath.trim().isEmpty()) {
      throw new IllegalArgumentException("锁根路径不能为空");
    }
  }

  /**
   * 验证数值范围
   *
   * @throws IllegalArgumentException 如果数值超出有效范围
   */
  protected void validateRanges() {
    if (defaultExpireTimeMs <= 0) {
      throw new IllegalArgumentException("默认过期时间必须大于0");
    }

    if (maxWaitTimeMs <= 0) {
      throw new IllegalArgumentException("最大等待时间必须大于0");
    }

    if (cacheExpireTimeMs <= 0) {
      throw new IllegalArgumentException("缓存过期时间必须大于0");
    }

    if (maxRetries < 0) {
      throw new IllegalArgumentException("重试次数不能小于0");
    }
  }

  /**
   * 自定义验证逻辑
   *
   * <p>子类可以重写此方法来实现特定的验证逻辑</p>
   *
   * @throws IllegalArgumentException 如果自定义验证失败
   */
  protected void validateCustom() {
    // 默认不做额外验证，子类可以重写
  }

  /**
   * 获取扩展属性
   *
   * @param key 属性键
   * @return 属性值，如果不存在则返回null
   */
  public String getProperty(String key) {
    return properties.get(key);
  }

  /**
   * 获取扩展属性（带默认值）
   *
   * @param key 属性键
   * @param defaultValue 默认值
   * @return 属性值，如果不存在则返回默认值
   */
  public String getProperty(String key, String defaultValue) {
    return properties.getOrDefault(key, defaultValue);
  }

  /**
   * 设置扩展属性
   *
   * @param key 属性键
   * @param value 属性值
   */
  public void setProperty(String key, String value) {
    if (properties == null) {
      properties = new HashMap<>();
    }
    properties.put(key, value);
  }

  /**
   * 移除扩展属性
   *
   * @param key 属性键
   * @return 被移除的属性值
   */
  public String removeProperty(String key) {
    return properties != null ? properties.remove(key) : null;
  }

  /**
   * 清空所有扩展属性
   */
  public void clearProperties() {
    if (properties != null) {
      properties.clear();
    }
  }

  /**
   * 构建完整的锁路径
   *
   * @param lockKey 锁标识
   * @return 完整的锁路径
   */
  public String buildLockPath(String lockKey) {
    if (lockKey == null || lockKey.trim().isEmpty()) {
      throw new IllegalArgumentException("锁标识不能为空");
    }

    String basePath = getLockBasePath();
    if (!basePath.endsWith("/")) {
      basePath += "/";
    }

    // 清理锁标识中的非法字符
    String safeLockKey = lockKey.replaceAll("[^a-zA-Z0-9_:.-]", "_");

    return basePath + safeLockKey;
  }
}
