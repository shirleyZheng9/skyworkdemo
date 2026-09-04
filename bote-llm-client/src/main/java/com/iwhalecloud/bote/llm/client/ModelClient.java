package com.iwhalecloud.bote.llm.client;

/**
 * 模型客户端
 *
 * <p>用于适配不同的大模型，以 OpenAI 的接口协议为准，其它大模型如有不同在内部实现中适配为 OpenAI 的接口协议。</p>
 *
 * @author bianjp
 * @since 2024-08-01
 */
public interface ModelClient {

  /**
   * 获取默认使用的模型
   *
   * @return 默认模型
   */
  String defaultModel();

}
