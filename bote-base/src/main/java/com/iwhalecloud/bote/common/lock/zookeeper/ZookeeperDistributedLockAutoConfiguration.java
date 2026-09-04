package com.iwhalecloud.bote.common.lock.zookeeper;

import com.iwhalecloud.bote.common.lock.DistributedLockException;
import com.iwhalecloud.bote.common.lock.DistributedLockFactory;
import com.iwhalecloud.bss.litchi.cache.refresh.impl.ZooKeeperRefreshBroadcastServiceImpl.CreatorACLProvider;
import jakarta.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.CuratorFrameworkFactory.Builder;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Zookeeper分布式锁自动配置类
 *
 * <p>负责配置和管理Zookeeper连接，以及分布式锁相关的Bean注册。</p>
 *
 * <p>配置项说明：</p>
 * <ul>
 *   <li>bote.lock.zookeeper.enabled: 是否启用Zookeeper分布式锁，默认false</li>
 *   <li>bote.lock.zookeeper.connect-string: Zookeeper连接字符串</li>
 *   <li>bote.lock.zookeeper.session-timeout-ms: 会话超时时间，默认30秒</li>
 *   <li>bote.lock.zookeeper.connection-timeout-ms: 连接超时时间，默认15秒</li>
 *   <li>bote.lock.zookeeper.base-sleep-time-ms: 重试基础等待时间，默认1秒</li>
 *   <li>bote.lock.zookeeper.max-retries: 最大重试次数，默认3次</li>
 *   <li>bote.lock.zookeeper.namespace: 命名空间，默认为"bote-locks"</li>
 * </ul>
 *
 * @since 2025-08-21
 */
@Configuration
@ConditionalOnBooleanProperty("bote.lock.zookeeper.enabled")
@EnableConfigurationProperties(ZookeeperDistributedLockProperties.class)
public class ZookeeperDistributedLockAutoConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(ZookeeperDistributedLockAutoConfiguration.class);

  private CuratorFramework curatorFramework;

  /**
   * 配置CuratorFramework客户端
   *
   * @param properties Zookeeper配置属性
   * @return CuratorFramework客户端实例
   */
  @Bean
  @SuppressWarnings("PMD.GuardLogStatement")
  public CuratorFramework zookeeperCuratorFramework(ZookeeperDistributedLockProperties properties) {
    // 验证配置
    properties.validate();

    logger.info("初始化Zookeeper客户端: connectString={}, namespace={}",
      properties.getConnectString(), properties.getNamespace());

    // 配置重试策略：基础等待时间、最多重试次数
    ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(
      (int) properties.getRetryIntervalMs(),
      properties.getMaxRetries()
    );

    // 创建CuratorFramework实例
    CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
      .connectString(properties.getConnectString())
      .sessionTimeoutMs(properties.getSessionTimeoutMs())
      .connectionTimeoutMs(properties.getConnectionTimeoutMs())
      .retryPolicy(retryPolicy);

    // 设置命名空间（可选）
    if (StringUtils.isNotEmpty(properties.getNamespace())) {
      builder.namespace(properties.getNamespace());
    }
    // 处理鉴权
    setupAuth(builder, properties);
    this.curatorFramework = builder.build();

    // 启动客户端
    this.curatorFramework.start();

    try {
      // 等待连接建立，最多等待连接超时时间
      if (!this.curatorFramework.blockUntilConnected(
        properties.getConnectionTimeoutMs(),
        java.util.concurrent.TimeUnit.MILLISECONDS)) {
        throw new DistributedLockException(
          DistributedLockException.ErrorCodes.CONNECTION_FAILED,
          "无法连接到Zookeeper服务器: " + properties.getConnectString()
        );
      }
      logger.info("Zookeeper客户端连接成功: {}", properties.getConnectString());
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new DistributedLockException(
        DistributedLockException.ErrorCodes.CONNECTION_FAILED,
        "连接Zookeeper时被中断",
        e
      );
    }

    return this.curatorFramework;
  }

  /**
   * 配置Zookeeper分布式锁工厂
   *
   * @param curatorFramework Curator客户端
   * @param properties Zookeeper配置属性
   * @return Zookeeper分布式锁工厂实例
   */
  @Bean
  public DistributedLockFactory zookeeperDistributedLockFactory(
    CuratorFramework curatorFramework,
    ZookeeperDistributedLockProperties properties) {

    ZookeeperDistributedLockFactory factory = new ZookeeperDistributedLockFactory(curatorFramework, properties);
    logger.info("Zookeeper分布式锁工厂初始化完成");
    return factory;
  }

  /**
   * 销毁资源，关闭Zookeeper连接
   */
  @PreDestroy
  public void destroy() {
    if (curatorFramework != null) {
      logger.info("关闭Zookeeper客户端连接");
      try {
        curatorFramework.close();
      }
      catch (Exception e) {
        logger.warn("关闭Zookeeper客户端时发生异常", e);
      }
    }
  }

  private void setupAuth(Builder builder, ZookeeperDistributedLockProperties lockProperties) {
    String digest = lockProperties.getDigest();
    if (StringUtils.isNotBlank(digest)) {
      // 使用 zkCli 对节点设置访问权限: setAcl /path digest:username:DIGEST:crwad
      //   * /path 表示节点路径
      //   * username 为用户名
      //   * DIGEST 为 username:password 的哈希。可使用 shell 命令生成:
      //      echo -n 'user:password' | openssl dgst -binary -sha1 | openssl base64
      //      echo -n 'user:password' | sha1sum | xxd -r -p | base64
      //   * crwad 表示权限，对应 ZooDefs.Perms
      // 在 zkCli 中访问节点时鉴权: addauth digest username:password
      byte[] auth = digest.getBytes(StandardCharsets.UTF_8);
      builder.authorization("digest", auth).aclProvider(new CreatorACLProvider());
    }
  }
}
