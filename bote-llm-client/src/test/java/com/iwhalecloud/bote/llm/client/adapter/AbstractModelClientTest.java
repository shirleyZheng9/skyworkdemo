package com.iwhalecloud.bote.llm.client.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.iwhalecloud.bote.llm.client.config.EmbeddingProperties;
import com.iwhalecloud.bote.llm.client.dto.HeaderItem;
import java.util.List;
import okhttp3.Headers;
import org.junit.jupiter.api.Test;

/**
 * {@link AbstractModelClient} 单元测试
 *
 * <p>通过 {@link OpenAIEmbeddingClient} 作为载体，覆盖 buildHeaders/resolveHeaders/defaultModel
 * 以及构造校验逻辑。</p>
 *
 * <p>buildHeaders 为 private static 方法，通过同包访问 protected resolveHeaders() 间接验证。
 * headerResolver 在无 Spring 环境下为 null，走 plain 分支。</p>
 */
class AbstractModelClientTest {

  private static EmbeddingProperties baseProps() {
    return EmbeddingProperties.builder()
        .url("http://localhost/v1/embeddings")
        .apiKey("sk-test")
        .model("text-embedding-3-small")
        .build();
  }

  // ==================== buildHeaders ====================

  @Test
  void buildHeaders_apiKey_bearerAuth() {
    OpenAIEmbeddingClient client = new OpenAIEmbeddingClient(baseProps());
    Headers headers = client.resolveHeaders();

    assertThat(headers.get("Authorization")).isEqualTo("Bearer sk-test");
    assertThat(headers.get("X-CHANNEL")).isNull();
  }

  @Test
  void buildHeaders_gptProxy_addsXChannel() {
    EmbeddingProperties props = EmbeddingProperties.builder()
        .url("https://lab.iwhalecloud.com/v1/embeddings")
        .apiKey("sk-test")
        .model("text-embedding-3-small")
        .build();
    OpenAIEmbeddingClient client = new OpenAIEmbeddingClient(props);
    Headers headers = client.resolveHeaders();

    assertThat(headers.get("X-CHANNEL")).isEqualTo("BOTE-AGENT");
  }

  @Test
  void buildHeaders_devProxy_addsXChannel() {
    EmbeddingProperties props = EmbeddingProperties.builder()
        .url("https://dev.iwhalecloud.com/v1/embeddings")
        .apiKey("sk-test")
        .model("text-embedding-3-small")
        .build();
    OpenAIEmbeddingClient client = new OpenAIEmbeddingClient(props);
    Headers headers = client.resolveHeaders();

    assertThat(headers.get("X-CHANNEL")).isEqualTo("BOTE-AGENT");
  }

  @Test
  void buildHeaders_noApiKey_noAuthHeader() {
    EmbeddingProperties props = EmbeddingProperties.builder()
        .url("http://localhost/v1/embeddings")
        .model("text-embedding-3-small")
        .build();
    OpenAIEmbeddingClient client = new OpenAIEmbeddingClient(props);
    Headers headers = client.resolveHeaders();

    assertThat(headers.get("Authorization")).isNull();
  }

  // ==================== resolveHeaders ====================

  @Test
  void resolveHeaders_noCustomHeaders_returnsBaseHeaders() {
    OpenAIEmbeddingClient client = new OpenAIEmbeddingClient(baseProps());
    Headers headers = client.resolveHeaders();

    // 与 buildHeaders 一致：只有 Authorization
    assertThat(headers.get("Authorization")).isEqualTo("Bearer sk-test");
    assertThat(headers.size()).isEqualTo(1);
  }

  @Test
  void resolveHeaders_customHeaders_appended() {
    EmbeddingProperties props = EmbeddingProperties.builder()
        .url("http://localhost/v1/embeddings")
        .apiKey("sk-test")
        .model("text-embedding-3-small")
        .headers(List.of(new HeaderItem("X-Foo", "bar")))
        .build();
    OpenAIEmbeddingClient client = new OpenAIEmbeddingClient(props);
    Headers headers = client.resolveHeaders();

    assertThat(headers.get("Authorization")).isEqualTo("Bearer sk-test");
    assertThat(headers.get("X-Foo")).isEqualTo("bar");
  }

  @Test
  void resolveHeaders_emptyHeaderValuesFiltered() {
    // 同时传非空和空值的 header，空值应被过滤
    EmbeddingProperties props = EmbeddingProperties.builder()
        .url("http://localhost/v1/embeddings")
        .apiKey("sk-test")
        .model("text-embedding-3-small")
        .headers(List.of(
            new HeaderItem("X-Foo", "bar"),
            new HeaderItem("X-Empty", "")))
        .build();
    OpenAIEmbeddingClient client = new OpenAIEmbeddingClient(props);
    Headers headers = client.resolveHeaders();

    assertThat(headers.get("X-Foo")).isEqualTo("bar");
    assertThat(headers.get("X-Empty")).isNull();
  }

  // ==================== defaultModel ====================

  @Test
  void defaultModel_returnsPropsModel() {
    OpenAIEmbeddingClient client = new OpenAIEmbeddingClient(baseProps());
    assertThat(client.defaultModel()).isEqualTo("text-embedding-3-small");
  }

  // ==================== 构造校验 ====================

  @Test
  void constructor_emptyUrl_throws() {
    EmbeddingProperties props = EmbeddingProperties.builder()
        .url(null)
        .apiKey("sk-test")
        .model("text-embedding-3-small")
        .build();
    assertThatThrownBy(() -> new OpenAIEmbeddingClient(props))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("接口地址不能为空");
  }

  @Test
  void constructor_emptyModel_throws() {
    EmbeddingProperties props = EmbeddingProperties.builder()
        .url("http://localhost/v1/embeddings")
        .apiKey("sk-test")
        .model(null)
        .build();
    assertThatThrownBy(() -> new OpenAIEmbeddingClient(props))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("模型不能为空");
  }
}
