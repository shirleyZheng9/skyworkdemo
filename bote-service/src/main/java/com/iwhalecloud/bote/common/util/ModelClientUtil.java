package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bote.llm.client.EmbeddingClient;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.LlmClientFactory;
import com.iwhalecloud.bote.llm.client.adapter.OpenAIEmbeddingClient;
import com.iwhalecloud.bote.llm.client.config.EmbeddingProperties;
import com.iwhalecloud.bote.llm.client.config.LlmProperties;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.util.Assert;

/**
 * 模型客户端工具类
 *
 * @author bianjp
 * @since 2025-05-12
 */
public final class ModelClientUtil {
  /** 模型客户端工厂实例列表，按 Bean 优先级排序 */
  private static final List<LlmClientFactory> llmClientFactories = SpringUtil.getBeans(LlmClientFactory.class);

  private ModelClientUtil() {
  }

  /**
   * 获取大语言模型的协议类型列表
   *
   * @return 协议类型列表
   */
  public static List<String> getLlmProtocolTypes() {
    return llmClientFactories.stream().map(LlmClientFactory::getProtocolType).collect(Collectors.toList());
  }

  /**
   * 创建大语言模型客户端实例
   *
   * @param protocolType 协议类型
   * @param properties 模型配置
   * @return 大语言模型客户端实例
   */
  public static LlmClient createLlmClient(String protocolType, LlmProperties properties) {
    LlmClientFactory factory = getFactory(protocolType);
    return factory.create(properties);
  }

  /**
   * 创建嵌入模型客户端实例
   *
   * @param properties 模型配置
   * @return 嵌入模型客户端实例
   */
  public static EmbeddingClient createEmbeddingClient(EmbeddingProperties properties) {
    return new OpenAIEmbeddingClient(properties);
  }

  /**
   * 根据协议类型获取工厂实例
   *
   * @param protocolType 协议类型
   */
  static LlmClientFactory getFactory(String protocolType) {
    Assert.hasLength(protocolType, "协议类型不能为空");
    LlmClientFactory factory = IterableUtils.find(llmClientFactories, f -> protocolType.equals(f.getProtocolType()));
    Assert.notNull(factory, () -> "未知的大模型协议类型: " + protocolType);
    return factory;
  }
}
