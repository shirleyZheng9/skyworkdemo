package com.iwhalecloud.bote.llm.client.adapter;

import com.iwhalecloud.bote.llm.client.EmbeddingClient;
import com.iwhalecloud.bote.llm.client.config.EmbeddingProperties;
import com.iwhalecloud.bote.llm.client.dto.EmbeddingRequest;
import com.iwhalecloud.bote.llm.client.dto.EmbeddingResponse;
import com.iwhalecloud.bote.llm.client.dto.EmbeddingResponseItem;
import com.iwhalecloud.bote.llm.client.util.ModelHttpClient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * OpenAI 嵌入模型客户端
 *
 * <p>适用于兼容 OpenAI 接口协议的嵌入模型</p>
 *
 * @author bianjp
 * @since 2024-12-23
 */
public class OpenAIEmbeddingClient extends AbstractModelClient<EmbeddingProperties> implements EmbeddingClient {
  public OpenAIEmbeddingClient(EmbeddingProperties properties) {
    super(properties);
  }

  @Override
  public float[] embedding(String text) {
    return embedding(text, null);
  }

  @Override
  public float[] embedding(String text, @Nullable Consumer<Object> requestListener) {
    Assert.hasLength(text, "文本不能为空");
    EmbeddingRequest request = new EmbeddingRequest();
    request.setModel(properties.getModel());
    request.setInput(Collections.singletonList(text));
    EmbeddingResponse response = ModelHttpClient.post(apiUrl, resolveHeaders(), request, requestListener, EmbeddingResponse.class);
    Assert.notEmpty(response.getData(), "嵌入模型调用失败");
    return response.getData().get(0).getEmbedding();
  }

  @Override
  public List<float[]> embedding(List<String> texts) {
    return embedding(texts, null);
  }

  @Override
  public List<float[]> embedding(List<String> texts, @Nullable Consumer<Object> requestListener) {
    Assert.notEmpty(texts, "文本列表不能为空");
    EmbeddingRequest request = new EmbeddingRequest();
    request.setModel(properties.getModel());
    List<float[]> embeddings = new ArrayList<>(texts.size());
    // 服务器端一般对总的 token 数量或者文本列表大小有限制，需要分批调用
    int batchSize = ObjectUtils.getIfNull(properties.getBatchSize(), 10);
    for (List<String> partitionedTexts : ListUtils.partition(texts, batchSize)) {
      request.setInput(partitionedTexts);
      EmbeddingResponse response = ModelHttpClient.post(apiUrl, resolveHeaders(), request, requestListener, EmbeddingResponse.class);
      Assert.notEmpty(response.getData(), "嵌入模型调用失败");
      Assert.isTrue(partitionedTexts.size() == response.getData().size(), "嵌入模型调用失败，响应数量不一致");
      for (EmbeddingResponseItem item : response.getData()) {
        embeddings.add(item.getEmbedding());
      }
    }
    return embeddings;
  }
}
