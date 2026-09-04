package com.iwhalecloud.bote.llm.client;

import java.util.List;
import java.util.function.Consumer;
import org.springframework.lang.Nullable;

/**
 * 文本嵌入模型客户端
 *
 * @author bianjp
 * @since 2024-12-23
 */
public interface EmbeddingClient extends ModelClient {

  /**
   * 向量化文本
   *
   * @param text 文本
   * @return 向量结果
   */
  float[] embedding(String text);

  /**
   * 向量化文本
   *
   * @param text 文本
   * @param requestListener 请求监听器，用于调用方存储 Call 对象以实现中断请求
   * @return 向量结果
   */
  float[] embedding(String text, @Nullable Consumer<Object> requestListener);

  /**
   * 批量向量化多个文本
   *
   * @param texts 文本列表
   * @return 向量结果列表，与文本列表一一对应
   */
  List<float[]> embedding(List<String> texts);

  /**
   * 批量向量化多个文本
   *
   * @param texts 文本列表
   * @param requestListener 请求监听器，用于调用方存储 Call 对象以实现中断请求
   * @return 向量结果列表，与文本列表一一对应
   */
  List<float[]> embedding(List<String> texts, @Nullable Consumer<Object> requestListener);

}
