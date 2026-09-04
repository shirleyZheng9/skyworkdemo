package com.iwhalecloud.bote.service.model;

import com.iwhalecloud.bote.dto.model.FunctionCallingTestResult;
import com.iwhalecloud.bote.dto.model.query.LargeModelTestParams;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import java.util.function.Consumer;

/**
 * 模型测试服务
 *
 * @author bianjp
 * @since 2025-12-17
 */
public interface IModelTestService {

  /**
   * 测试文本嵌入模型
   *
   * @param params 测试参数
   * @return 向量结果
   */
  float[] testEmbedding(LargeModelTestParams params);

  /**
   * 测试大语言模型
   *
   * @param params 测试参数
   * @return 调用结果
   */
  ChatCompletionResponse testLlm(LargeModelTestParams params);

  /**
   * 测试大语言模型，流式输出
   *
   * @param params 测试参数
   * @param partialHandler 片段处理器
   */
  void testLlmStream(LargeModelTestParams params, Consumer<ChatCompletionResponse> partialHandler);

  /**
   * 测试函数调用
   *
   * @param params 测试参数
   * @return 测试结果
   */
  FunctionCallingTestResult testFunctionCalling(LargeModelTestParams params);

}
