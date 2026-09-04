package com.iwhalecloud.bote.llm.interceptor;

import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.ModelConfigInfoDTO;

/**
 * 大模型客户端拦截器
 *
 * @author bianjp
 * @since 2025-11-10
 */
public interface LlmClientInterceptor {

  /**
   * 调用前
   *
   * @param request 请求对象
   */
  void beforeInvoke(ChatCompletionRequest request);

  /**
   * 流式输出过程中，分片回调
   *
   * <p>默认不处理，按需覆盖</p>
   *
   * @param modelConfigInfo 模型配置信息
   * @param request 请求对象
   * @param response 当前累计响应对象
   */
  default void onPartial(ModelConfigInfoDTO modelConfigInfo, ChatCompletionRequest request, ChatCompletionResponse response) {
    // 默认无操作
  }

  /**
   * 调用成功
   *
   * @param modelConfigInfo 模型配置信息
   * @param request 请求对象
   * @param response 响应对象
   */
  void onSuccess(ModelConfigInfoDTO modelConfigInfo, ChatCompletionRequest request, ChatCompletionResponse response);

}
