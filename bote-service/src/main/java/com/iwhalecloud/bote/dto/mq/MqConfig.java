package com.iwhalecloud.bote.dto.mq;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bss.litchi.mq.config.properties.CtgMQProperties;
import com.iwhalecloud.bss.litchi.mq.config.properties.KafkaProperties;
import com.iwhalecloud.bss.litchi.mq.config.properties.KafkaProperties.KafkaConsumerProperties;
import com.iwhalecloud.bss.litchi.mq.config.properties.LitchiMQProperties;
import com.iwhalecloud.bss.litchi.mq.config.properties.RocketMQProperties;
import com.iwhalecloud.bss.litchi.mq.consts.MQType;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.springframework.util.Assert;

/**
 * MQ 配置
 *
 * @author chen.linfa
 * @since 2025-12-04
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@JsonInclude(Include.NON_NULL)
@Schema(description = "MQ 配置")
@SuppressWarnings("java:S1192")
public class MqConfig {

  @Schema(description = "服务器(host:port)。多个服务器用逗号分隔(MQ 实际需要用分号分隔，运行时自动转换一下)", requiredMode = RequiredMode.REQUIRED)
  private String server;
  @Schema(description = "主题名称前缀（用于不同环境共用服务器时给不同环境配置不同主题以隔离数据）")
  private String topicNamePrefix;
  @Schema(description = "主题名称后缀")
  private String topicNameSuffix;
  @Schema(description = "Access Key")
  private String accessKey;
  @Schema(description = "Secret Key")
  private String secretKey;
  @Schema(description = "生产者分组")
  private String producerGroup;
  @Schema(description = "消费者分组")
  private String consumerGroup;

  // CtgMQ
  @Schema(description = "集群名称(CtgMQ 必填)")
  private String clusterName;
  @Schema(description = "租户 ID(CtgMQ 必填)")
  private String tenantId;

  // Kafka
  @Schema(description = "安全协议(security.protocol)", allowableValues = "PLAINTEXT,SASL_PLAINTEXT,SSL,SASL_SSL", requiredMode = RequiredMode.REQUIRED)
  private String securityProtocol;
  @Schema(description = "认证方式(sasl.mechanism)")
  private String saslMechanism;
  @Schema(description = "认证配置(sasl.jaas.config)")
  private String saslJaasConfig;
  @Schema(description = "SSL 信任文件路径(ssl.truststore.location)")
  private String sslTruststoreLocation;
  @Schema(description = "SSL 信任文件密码(ssl.truststore.password)")
  private String sslTruststorePassword;

  /**
   * 规范配置属性
   */
  public void normalize() {
    // 删除服务器中的空白字符，避免 MQ 客户端解析或使用时报错
    server = StringUtils.trimToNull(StringUtils.deleteWhitespace(server));
    // trim 所有值，为空时转为 null 以降低 JSON 序列化后的空间占用（null 属性会被忽略）
    topicNamePrefix = StringUtils.trimToNull(topicNamePrefix);
    topicNameSuffix = StringUtils.trimToNull(topicNameSuffix);
    accessKey = StringUtils.trimToNull(accessKey);
    secretKey = StringUtils.trimToNull(secretKey);
    producerGroup = StringUtils.trimToNull(producerGroup);
    consumerGroup = StringUtils.trimToNull(consumerGroup);
    clusterName = StringUtils.trimToNull(clusterName);
    tenantId = StringUtils.trimToNull(tenantId);
    securityProtocol = StringUtils.trimToNull(securityProtocol);
    saslMechanism = StringUtils.trimToNull(saslMechanism);
    saslJaasConfig = StringUtils.trimToNull(saslJaasConfig);
    sslTruststoreLocation = StringUtils.trimToNull(sslTruststoreLocation);
    sslTruststorePassword = StringUtils.trimToNull(sslTruststorePassword);
  }

  /**
   * 校验 MQ 配置
   *
   * @param mqType MQ 类型
   * @param producerEnabled 是否启用了生产者
   * @param consumerEnabled 是否启用了消费者
   */
  public void validate(String mqType, boolean producerEnabled, boolean consumerEnabled) {
    Assert.hasText(server, "服务器不能为空");
    // 支持两者都不启用（可能有时想临时关闭功能而不想直接删除配置）
    if (!producerEnabled && !consumerEnabled) {
      return;
    }

    if (BaseConsts.MQ_TYPE_KAFKA.equals(mqType)) {
      validateKafka(consumerEnabled);
    }
    else if (BaseConsts.MQ_TYPE_ZMQ.equals(mqType)) {
      validateZmq(producerEnabled, consumerEnabled);
    }
    else if (BaseConsts.MQ_TYPE_CTGMQ.equals(mqType)) {
      validateCtgMQ(producerEnabled, consumerEnabled);
    }
  }

  /**
   * 校验 Kafka 配置
   */
  private void validateKafka(boolean enableConsumer) {
    Assert.hasText(securityProtocol, "安全协议不能为空");
    if (securityProtocol.equalsIgnoreCase(SecurityProtocol.SASL_SSL.name) || securityProtocol.equalsIgnoreCase(SecurityProtocol.SASL_PLAINTEXT.name)) {
      Assert.hasText(saslJaasConfig, "认证方式不能为空");
    }
    if (securityProtocol.equalsIgnoreCase(SecurityProtocol.SASL_SSL.name) || securityProtocol.equalsIgnoreCase(SecurityProtocol.SSL.name)) {
      Assert.hasText(sslTruststoreLocation, "SSL 信任文件路径不能为空");
    }
    if (enableConsumer) {
      Assert.hasLength(consumerGroup, "消费者分组不能为空");
    }
  }

  /**
   * 校验 ZMQ 配置
   */
  private void validateZmq(boolean enableProducer, boolean enableConsumer) {
    // com.ztesoft.mq.client.impl.ZMQClientFactoryImpl#createProducer
    if (enableProducer) {
      Assert.hasText(producerGroup, "生产者分组不能为空");
    }
    // com.ztesoft.mq.client.impl.ZMQClientFactoryImpl.createConsumer
    if (enableConsumer) {
      Assert.hasLength(consumerGroup, "消费者分组不能为空");
    }
  }

  /**
   * 校验 CtgMQ 配置
   */
  private void validateCtgMQ(boolean enableProducer, boolean enableConsumer) {
    // com.ctg.mq.api.impl.MQProducerImpl#checkConfig
    // com.ctg.mq.api.impl.MQConsumerImpl#checkConfig
    Assert.hasLength(accessKey, "Access Key 不能为空");
    Assert.hasLength(secretKey, "Secret Key 不能为空");
    Assert.hasLength(clusterName, "集群名称不能为空");
    Assert.hasLength(tenantId, "租户 ID 不能为空");

    // MQ 客户端未要求必填，但不设置可能会影响功能；跟 ZMQ 保持一致也要求必填吧
    if (enableProducer) {
      Assert.hasText(producerGroup, "生产者分组不能为空");
    }
    if (enableConsumer) {
      Assert.hasLength(consumerGroup, "消费者分组不能为空");
    }
  }

  /**
   * 构造配置属性
   *
   * <p>MQ 功能目前用的不多，自动添加一些降低资源占用的配置（线程池数量配置为 1、关闭 trace 功能、降低心跳频率等），用户确实有需要时可以通过高级配置覆盖。</p>
   *
   * @param mqType MQ 类型
   * @param isProducer 是否是生产者
   * @return MQ 配置属性
   */
  public LitchiMQProperties buildProperties(String mqType, boolean isProducer) {
    LitchiMQProperties properties = new LitchiMQProperties();
    MQType type = MQType.typeOf(mqType);
    properties.setType(type);
    switch (type) {
      case ROCKETMQ:
        properties.setRocketmq(buildRocketMqProperties(isProducer));
        break;
      case ZMQ:
        properties.setZmq(buildRocketMqProperties(isProducer));
        break;
      case CTGMQ:
        properties.setCtgmq(buildCtgMqProperties(isProducer));
        break;
      case KAFKA:
        properties.setKafka(buildKafkaProperties(isProducer));
        break;
      default:
        throw new IllegalArgumentException("未知的 MQ 类型: " + type);
    }
    properties.setTopicPrefix(topicNamePrefix);
    properties.setTopicSuffix(topicNameSuffix);
    return properties;
  }

  /**
   * 构造 Kafka 配置
   */
  private KafkaProperties buildKafkaProperties(boolean isProducer) {
    KafkaProperties properties = new KafkaProperties();
    properties.setServer(server);
    properties.setSecurityProtocol(securityProtocol);
    properties.setSaslMechanism(StringUtils.defaultIfEmpty(saslMechanism, SaslConfigs.DEFAULT_SASL_MECHANISM));
    properties.setSaslJaasConfig(saslJaasConfig);
    properties.setSslTruststoreLocation(sslTruststoreLocation);
    properties.setSslTruststorePassword(sslTruststorePassword);
    if (!isProducer) {
      KafkaConsumerProperties consumerProperties = properties.getConsumer();
      consumerProperties.setGroupId(consumerGroup);
      // MQ 功能应该用得不多，调大心跳间隔，牺牲一点可靠性，降低系统负载
      consumerProperties.setHeartbeatIntervalMs(10000);
    }
    return properties;
  }

  /**
   * 构造 ZMQ 配置
   */
  private RocketMQProperties buildRocketMqProperties(boolean isProducer) {
    RocketMQProperties properties = new RocketMQProperties();
    properties.setServer(server);
    properties.setAccessKey(accessKey);
    properties.setSecretKey(secretKey);
    if (isProducer) {
      properties.getProducer().setGroupId(producerGroup);
    }
    else {
      properties.getConsumer().setGroupId(consumerGroup);
    }

    // MQ 功能应该用得不多，减少线程数，降低系统负载
    properties.setEnableMsgTrace(false);
    properties.setClientCallbackExecutorThreads(1);
    properties.getConsumer().setConsumeThreadMin(1);
    properties.getConsumer().setConsumeThreadMax(1);

    return properties;
  }

  /**
   * 构造 CtgMQ 配置
   */
  private CtgMQProperties buildCtgMqProperties(boolean isProducer) {
    CtgMQProperties properties = new CtgMQProperties();
    properties.setServer(server);
    properties.setAuthId(accessKey);
    properties.setAuthPwd(secretKey);
    properties.setClusterName(clusterName);
    properties.setTenantID(tenantId);
    if (isProducer) {
      properties.getProducer().setProducerGroupName(producerGroup);
    }
    else {
      properties.getConsumer().setConsumerGroupName(consumerGroup);
    }

    // MQ 功能应该用得不多，减少线程数，降低系统负载
    properties.getConsumer().setConsumeThreadMin(1);
    properties.getConsumer().setConsumeThreadMax(1);
    properties.setClientWorkerThreads(1);
    properties.setClientCallbackExecutorThreads(1);
    properties.setEnableTrace(false);

    return properties;
  }

}
