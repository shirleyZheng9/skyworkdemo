package com.iwhalecloud.bote.common.lock.zookeeper;

import com.iwhalecloud.bote.common.lock.AbstractDistributedLockProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Zookeeper分布式锁配置属性
 *
 * <p>继承通用配置，添加Zookeeper特有的配置项。</p>
 *
 * <p>Zookeeper特有配置项：</p>
 * <ul>
 *   <li>connect-string: Zookeeper连接字符串</li>
 *   <li>session-timeout-ms: 会话超时时间</li>
 *   <li>connection-timeout-ms: 连接超时时间</li>
 *   <li>base-sleep-time-ms: 重试基础等待时间</li>
 *   <li>max-retries: 最大重试次数</li>
 * </ul>
 *
 * @since 2025-08-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = "bote.lock.zookeeper")
public class ZookeeperDistributedLockProperties extends AbstractDistributedLockProperties {

  /** Zookeeper连接字符串 */
  private String connectString = "localhost:2181";

  /** 会话超时时间（毫秒） */
  private int sessionTimeoutMs = 30000;

  /** 连接超时时间（毫秒） */
  private int connectionTimeoutMs = 30000;

  /** Zookeeper 鉴权设置 */
  private String digest;

  @Override
  public String getConnectionString() {
    return connectString;
  }

  @Override
  public void setConnectionString(String connectionString) {
    this.connectString = connectionString;
  }

  @Override
  public String getImplementationType() {
    return "zookeeper";
  }

  @Override
  protected void validateCustom() {
    super.validateCustom();

    if (sessionTimeoutMs <= 0) {
      throw new IllegalArgumentException("会话超时时间必须大于0");
    }

    if (connectionTimeoutMs <= 0) {
      throw new IllegalArgumentException("连接超时时间必须大于0");
    }

    // 验证连接字符串格式
    if (connectString != null && !connectString.trim().isEmpty()) {
      String[] hosts = connectString.split(",");
      for (String host : hosts) {
        if (!host.trim().matches("^[a-zA-Z0-9.-]+:[0-9]+$")) {
          throw new IllegalArgumentException("无效的Zookeeper连接字符串格式: " + host);
        }
      }
    }
  }

  /**
   * 获取完整的Zookeeper连接字符串（包含根路径）
   *
   * @return 完整连接字符串
   */
  public String getFullConnectionString() {
    if (getNamespace() != null && !getNamespace().trim().isEmpty() &&
      !"/".equals(getNamespace().trim())) {
      return connectString + "/" + getNamespace().replaceAll("^/+", "");
    }
    return connectString;
  }
}
