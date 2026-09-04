package com.iwhalecloud.bote.doc.module.collaboration.config;

import com.iwhalecloud.bote.doc.consts.DocCacheConsts;
import com.iwhalecloud.bote.doc.module.collaboration.pubsub.MessagePubSubTemplate;
import com.iwhalecloud.bote.doc.module.collaboration.pubsub.none.NoOpMessagePubSubTemplate;
import com.iwhalecloud.bote.doc.module.collaboration.pubsub.redis.RedisMessagePubSubTemplate;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 消息发布订阅配置类
 * 根据配置创建不同实现的MessagePubSubTemplate Bean实例
 *
 * @author Aiqing
 * @since 2025/12/04
 */
@Configuration
@EnableConfigurationProperties(MessagePubSubProperties.class)
@SuppressWarnings("PMD.GuardLogStatement")
public class MessagePubSubConfig {
  private static final Logger logger = LoggerFactory.getLogger(MessagePubSubConfig.class);

  /**
   * Redis实现的消息发布订阅模板
   * 当 enabled=true 且 type=redis 时生效（默认配置）
   */
  @Bean
  @ConditionalOnProperty(name = "bote.dc.message.pubsub.type", havingValue = "redis", matchIfMissing = true)
  @SuppressWarnings("PMD.GuardLogStatement")
  public MessagePubSubTemplate redisMessagePubSubTemplate(CacheFactory cacheFactory, MessagePubSubProperties properties) {
    boolean enabled = Boolean.TRUE.equals(properties.getEnabled());
    if (!enabled) {
      logger.warn("消息发布订阅功能已禁用(enabled=false), 将使用NoOp实现");
      return new NoOpMessagePubSubTemplate();
    }
    logger.info("初始化Redis消息发布订阅模板, type={}", properties.getType());
    ICacheClient cacheClient = cacheFactory.getCacheClient(DocCacheConsts.GROUP_DOC,
      DocCacheConsts.CACHE_PREFIX_SOCKET_SERVER);
    if (!cacheClient.supportsPubSub()) {
      return new NoOpMessagePubSubTemplate();
    }
    return new RedisMessagePubSubTemplate(cacheClient);
  }

  /**
   * 禁用实现（空实现）
   * 当 type=none 时生效
   */
  @Bean
  @ConditionalOnProperty(name = "bote.dc.message.pubsub.type", havingValue = "none")
  public MessagePubSubTemplate noOpMessagePubSubTemplate(MessagePubSubProperties properties) {
    logger.warn("消息发布订阅功能已禁用, type={}", properties.getType());
    return new NoOpMessagePubSubTemplate();
  }
}

