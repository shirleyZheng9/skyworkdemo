package com.iwhalecloud.bote.llm.client.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.times;

import com.iwhalecloud.bote.llm.client.config.EmbeddingProperties;
import com.iwhalecloud.bote.llm.client.dto.EmbeddingRequest;
import com.iwhalecloud.bote.llm.client.dto.EmbeddingResponse;
import com.iwhalecloud.bote.llm.client.dto.EmbeddingResponseItem;
import com.iwhalecloud.bote.llm.client.util.ModelHttpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * {@link OpenAIEmbeddingClient} 单元测试
 *
 * <p>使用 mockStatic(ModelHttpClient.class) 模式 B stub post 5 参重载（EmbeddingResponse.class），
 * 覆盖单文本嵌入、批处理、异常分支等。</p>
 */
class OpenAIEmbeddingClientTest {

  // ==================== 辅助方法 ====================

  private static EmbeddingProperties defaultProps() {
    EmbeddingProperties props = EmbeddingProperties.builder()
        .url("http://localhost/v1/embeddings")
        .apiKey("sk-test")
        .model("text-embedding-3-small")
        .build();
    return props;
  }

  private static EmbeddingResponseItem item(String name) {
    EmbeddingResponseItem item = new EmbeddingResponseItem();
    item.setEmbedding(new float[]{1f, 2f, 3f});
    return item;
  }

  private static EmbeddingResponse buildRespWithNItems(int n) {
    EmbeddingResponse resp = new EmbeddingResponse();
    List<EmbeddingResponseItem> data = IntStream.range(0, n)
        .mapToObj(i -> item("v" + i))
        .collect(Collectors.toList());
    resp.setData(data);
    return resp;
  }

  // ==================== embedding(String) ====================

  @Test
  void embedding_single_returnsVector() {
    EmbeddingProperties props = defaultProps();
    OpenAIEmbeddingClient client = new OpenAIEmbeddingClient(props);

    try (MockedStatic<ModelHttpClient> mocked = Mockito.mockStatic(ModelHttpClient.class)) {
      mocked.when(() -> ModelHttpClient.post(any(HttpUrl.class), nullable(Headers.class), any(),
          nullable(Consumer.class), eq(EmbeddingResponse.class)))
          .thenReturn(buildRespWithNItems(1));

      float[] result = client.embedding("hello", null);

      assertThat(result).hasSize(3);
      assertThat(result).containsExactly(1f, 2f, 3f);
    }
  }

  @Test
  void embedding_emptyText_throws() {
    EmbeddingProperties props = defaultProps();
    OpenAIEmbeddingClient client = new OpenAIEmbeddingClient(props);

    try (MockedStatic<ModelHttpClient> mocked = Mockito.mockStatic(ModelHttpClient.class)) {
      mocked.when(() -> ModelHttpClient.post(any(HttpUrl.class), nullable(Headers.class), any(),
          nullable(Consumer.class), eq(EmbeddingResponse.class)))
          .thenReturn(buildRespWithNItems(1));

      assertThatThrownBy(() -> client.embedding("", null))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("文本不能为空");
    }
  }

  @Test
  void embedding_emptyData_throws() {
    EmbeddingProperties props = defaultProps();
    OpenAIEmbeddingClient client = new OpenAIEmbeddingClient(props);

    try (MockedStatic<ModelHttpClient> mocked = Mockito.mockStatic(ModelHttpClient.class)) {
      mocked.when(() -> ModelHttpClient.post(any(HttpUrl.class), nullable(Headers.class), any(),
          nullable(Consumer.class), eq(EmbeddingResponse.class)))
          .thenReturn(new EmbeddingResponse());

      assertThatThrownBy(() -> client.embedding("hello", null))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("嵌入模型调用失败");
    }
  }

  // ==================== embedding(List) ====================

  @Test
  void embedding_list_batching() {
    EmbeddingProperties props = defaultProps();
    props.setBatchSize(2);
    OpenAIEmbeddingClient client = new OpenAIEmbeddingClient(props);

    // EmbeddingRequest 对象在批次间复用（setInput 覆盖），需捕获 input 副本
    List<List<String>> capturedInputs = new ArrayList<>();

    try (MockedStatic<ModelHttpClient> mocked = Mockito.mockStatic(ModelHttpClient.class)) {
      mocked.when(() -> ModelHttpClient.post(any(HttpUrl.class), nullable(Headers.class), any(),
          nullable(Consumer.class), eq(EmbeddingResponse.class)))
          .thenAnswer(invocation -> {
            EmbeddingRequest req = invocation.getArgument(2);
            capturedInputs.add(new ArrayList<>(req.getInput()));
            int n = req.getInput().size();
            return buildRespWithNItems(n);
          });

      List<float[]> result = client.embedding(List.of("a", "b", "c"));

      assertThat(result).hasSize(3);
      mocked.verify(() -> ModelHttpClient.post(any(HttpUrl.class), nullable(Headers.class), any(),
          nullable(Consumer.class), eq(EmbeddingResponse.class)), times(2));

      // 验证第一批 [a,b]、第二批 [c]
      assertThat(capturedInputs).hasSize(2);
      assertThat(capturedInputs.get(0)).containsExactly("a", "b");
      assertThat(capturedInputs.get(1)).containsExactly("c");
    }
  }

  @Test
  void embedding_list_sizeMismatch_throws() {
    EmbeddingProperties props = defaultProps();
    props.setBatchSize(2);
    OpenAIEmbeddingClient client = new OpenAIEmbeddingClient(props);

    try (MockedStatic<ModelHttpClient> mocked = Mockito.mockStatic(ModelHttpClient.class)) {
      // 始终返回 1 项，与 input 大小不一致
      mocked.when(() -> ModelHttpClient.post(any(HttpUrl.class), nullable(Headers.class), any(),
          nullable(Consumer.class), eq(EmbeddingResponse.class)))
          .thenReturn(buildRespWithNItems(1));

      assertThatThrownBy(() -> client.embedding(List.of("a", "b")))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("响应数量不一致");
    }
  }

  @Test
  void embedding_list_empty_throws() {
    EmbeddingProperties props = defaultProps();
    OpenAIEmbeddingClient client = new OpenAIEmbeddingClient(props);

    try (MockedStatic<ModelHttpClient> mocked = Mockito.mockStatic(ModelHttpClient.class)) {
      mocked.when(() -> ModelHttpClient.post(any(HttpUrl.class), nullable(Headers.class), any(),
          nullable(Consumer.class), eq(EmbeddingResponse.class)))
          .thenReturn(buildRespWithNItems(1));

      assertThatThrownBy(() -> client.embedding(List.of()))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("文本列表不能为空");
    }
  }

  @Test
  void embedding_list_defaultBatchSize10() {
    // 不设 batchSize，默认为 10
    EmbeddingProperties props = defaultProps();
    OpenAIEmbeddingClient client = new OpenAIEmbeddingClient(props);

    List<String> texts = IntStream.range(0, 11).mapToObj(i -> "t" + i).collect(Collectors.toList());

    try (MockedStatic<ModelHttpClient> mocked = Mockito.mockStatic(ModelHttpClient.class)) {
      mocked.when(() -> ModelHttpClient.post(any(HttpUrl.class), nullable(Headers.class), any(),
          nullable(Consumer.class), eq(EmbeddingResponse.class)))
          .thenAnswer(invocation -> {
            EmbeddingRequest req = invocation.getArgument(2);
            int n = req.getInput().size();
            return buildRespWithNItems(n);
          });

      List<float[]> result = client.embedding(texts);

      assertThat(result).hasSize(11);
      mocked.verify(() -> ModelHttpClient.post(any(HttpUrl.class), nullable(Headers.class), any(),
          nullable(Consumer.class), eq(EmbeddingResponse.class)), times(2));
    }
  }
}
