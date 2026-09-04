package com.iwhalecloud.bote.mcp.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.iwhalecloud.bote.mcp.client.transport.SseClientTransport;
import com.iwhalecloud.bote.mcp.client.transport.StdioClientTransport;
import com.iwhalecloud.bote.mcp.client.transport.StreamableClientTransport;
import com.iwhalecloud.bote.mcp.dto.Implementation;
import com.iwhalecloud.bote.mcp.dto.StdioServerParameters;
import com.iwhalecloud.bote.mcp.dto.response.LoggingMessageNotification;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import okhttp3.Headers;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * {@link McpClientBuilder} 单元测试
 *
 * <p>验证各配置方法、传输协议设置、build() 构造以及未指定协议时的异常。
 * 不依赖 Spring 环境（{@code McpClientProperties} 通过 fallback 构造）。</p>
 */
class McpClientBuilderTest {

  @Test
  void build_withoutTransport_throwsIllegalArgument() {
    assertThatThrownBy(() -> McpClient.builder("test").build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("未指定传输协议");
  }

  @Test
  void build_withSse_returnsClientWithSseTransport() {
    McpClient client = McpClient.builder("sse-client")
      .sse("https://example.com/sse", null, Headers.of("Authorization", "Bearer token"))
      .build();

    assertThat(client.getName()).isEqualTo("sse-client");
    assertThat(client.isHealthy()).isFalse();
    Object transport = ReflectionTestUtils.getField(client, "transport");
    assertThat(transport).isInstanceOf(SseClientTransport.class);
  }

  @Test
  void build_withStreamable_returnsClientWithStreamableTransport() {
    McpClient client = McpClient.builder("streamable-client")
      .streamable("https://example.com/mcp", Headers.of("X-Test", "1"))
      .build();

    assertThat(client.isHealthy()).isFalse();
    Object transport = ReflectionTestUtils.getField(client, "transport");
    assertThat(transport).isInstanceOf(StreamableClientTransport.class);
  }

  @Test
  void build_withStdio_returnsClientWithStdioTransport() {
    StdioServerParameters params = new StdioServerParameters(List.of("echo", "hello"));
    McpClient client = McpClient.builder("stdio-client")
      .stdio(params)
      .build();

    Object transport = ReflectionTestUtils.getField(client, "transport");
    assertThat(transport).isInstanceOf(StdioClientTransport.class);
  }

  @Test
  void builder_timeoutsAreAppliedFromDefaults() {
    // 默认初始化来自 McpClientProperties（fallback）：init=20s，request=60s
    McpClient client = McpClient.builder("t")
      .sse("https://example.com/sse", null, null)
      .build();

    assertThat(ReflectionTestUtils.getField(client, "initializationTimeout"))
      .isEqualTo(Duration.ofSeconds(20));
    assertThat(ReflectionTestUtils.getField(client, "requestTimeout"))
      .isEqualTo(Duration.ofSeconds(60));
  }

  @Test
  void builder_customTimeoutsAndClientInfoApplied() {
    Implementation info = new Implementation("custom", "9.9.9");
    McpClient client = McpClient.builder("t")
      .requestTimeout(Duration.ofSeconds(3))
      .initializationTimeout(Duration.ofSeconds(7))
      .clientInfo(info)
      .streamable("https://example.com/mcp", null)
      .build();

    assertThat(ReflectionTestUtils.getField(client, "initializationTimeout")).isEqualTo(Duration.ofSeconds(7));
    assertThat(ReflectionTestUtils.getField(client, "requestTimeout")).isEqualTo(Duration.ofSeconds(3));

    Object features = ReflectionTestUtils.getField(client, "features");
    Object clientInfo = ReflectionTestUtils.getField(features, "clientInfo");
    assertThat(clientInfo).isSameAs(info);
  }

  @Test
  void builder_loggingAndToolsChangeConsumersWrapped() {
    AtomicReference<List<com.iwhalecloud.bote.mcp.dto.McpTool>> capturedTools = new AtomicReference<>();
    AtomicReference<LoggingMessageNotification> capturedLog = new AtomicReference<>();

    McpClient client = McpClient.builder("t")
      .toolsChangeConsumer(capturedTools::set)
      .loggingConsumer(capturedLog::set)
      .loggingConsumers(List.of(l -> { }))
      .streamable("https://example.com/mcp", null)
      .build();

    Object features = ReflectionTestUtils.getField(client, "features");
    assertThat(ReflectionTestUtils.getField(features, "toolsChangeConsumers")).asList().hasSize(1);
    assertThat(ReflectionTestUtils.getField(features, "loggingConsumers")).asList().hasSize(2);
  }

  @Test
  void builder_clientIdAndSessionPrefixSet() {
    McpClient client = McpClient.builder("myname")
      .streamable("https://example.com/mcp", null)
      .build();

    String clientId = (String) ReflectionTestUtils.getField(client, "clientId");
    assertThat(clientId).startsWith("myname@");
    assertThat((String) ReflectionTestUtils.getField(client, "sessionPrefix")).hasSize(8);
  }

  @Test
  void stdioParameters_envMergedWithUserEnv() {
    Map<String, String> customEnv = Map.of("MY_VAR", "value1");
    StdioServerParameters params = new StdioServerParameters(List.of("cmd"), customEnv);

    assertThat(params.getEnv()).containsEntry("MY_VAR", "value1");
    assertThat(params.getCommand()).containsExactly("cmd");
  }
}
