package com.iwhalecloud.bote.mq;

import com.iwhalecloud.bote.dto.mq.MqConfig;
import java.io.Closeable;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MQ 客户端抽象类
 *
 * @author chen.linfa
 * @since 2025-12-04
 */
@Getter
public abstract class AbstractMqClient<T extends Closeable> {
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** MQ 实例 ID */
  protected final Long mqInstId;
  /** MQ 配置 */
  protected final MqConfig mqConfig;
  /** 客户端实例 */
  protected final T client;

  protected AbstractMqClient(Long mqInstId, MqConfig mqConfig, T client) {
    this.mqInstId = mqInstId;
    this.mqConfig = mqConfig;
    this.client = client;
  }

  /**
   * 检查客户端状态是否健康
   *
   * @return 是否健康
   */
  public abstract boolean isHealthy();

  /**
   * 关闭生产者，释放资源
   */
  public void close() {
    if (client == null) {
      return;
    }
    try {
      client.close();
      logger.debug("Closed MQ client successfully: mqInstId={}", mqInstId);
    }
    catch (Exception e) {
      logger.error("Failed to close MQ client: mqInstId={}", mqInstId, e);
    }
  }
}
