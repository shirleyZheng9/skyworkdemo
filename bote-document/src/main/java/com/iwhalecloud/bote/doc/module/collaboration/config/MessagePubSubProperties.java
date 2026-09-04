package com.iwhalecloud.bote.doc.module.collaboration.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 消息发布订阅配置属性
 *
 * @author Aiqing
 * @since 2025/12/04
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "bote.dc.message.pubsub")
public class MessagePubSubProperties {

  /**
   * 消息发布订阅实现类型
   * 可选值: redis, none
   * 默认值: redis
   */
  private Type type = Type.REDIS;

  /**
   * 是否启用消息发布订阅功能
   * 默认值: true
   */
  private Boolean enabled = true;

  /**
   * 消息发布订阅实现类型枚举
   */
  public enum Type {
    /**
     * Redis实现（默认）
     */
    REDIS,

    /**
     * 禁用
     */
    NONE
  }
}

