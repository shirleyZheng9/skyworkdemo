package com.iwhalecloud.bote.llm.client;

import com.iwhalecloud.bote.llm.client.config.LlmProperties;
import org.springframework.core.Ordered;

/**
 * 大语言模型客户端工厂类
 *
 * <p>使用 Ordered 控制协议列表的显示顺序</p>
 *
 * @author bianjp
 * @since 2025-05-12
 */
public interface LlmClientFactory extends Ordered {
  /**
   * 获取协议类型
   *
   * @return 协议类型
   */
  String getProtocolType();

  /**
   * 校验配置，不合法时抛异常
   *
   * @param properties 模型配置
   */
  void validate(LlmProperties properties);

  /**
   * 创建客户端实例
   *
   * @param properties 模型配置
   * @return 客户端实例
   */
  LlmClient create(LlmProperties properties);
}
