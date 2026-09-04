package com.iwhalecloud.bote.mcp.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.iwhalecloud.bote.mcp.dto.ClientCapabilities;
import com.iwhalecloud.bote.mcp.dto.Implementation;
import com.iwhalecloud.bote.mcp.dto.response.LoggingMessageNotification;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

/**
 * {@link McpClientFeatures} 单元测试
 *
 * <p>验证 Lombok 生成的 getter、toString 以及构造的字段赋值。</p>
 */
class McpClientFeaturesTest {

  @Test
  void getters_returnConstructorValues() {
    Implementation clientInfo = new Implementation("client", "1.2.3");
    ClientCapabilities capabilities = ClientCapabilities.EMPTY;
    List<Function<List<com.iwhalecloud.bote.mcp.dto.McpTool>, Mono<Void>>> toolsConsumers =
      Collections.singletonList(t -> Mono.empty());
    List<Function<LoggingMessageNotification, Mono<Void>>> loggingConsumers =
      Collections.singletonList(l -> Mono.empty());

    McpClientFeatures features = new McpClientFeatures(clientInfo, capabilities, toolsConsumers, loggingConsumers);

    assertThat(features.getClientInfo()).isSameAs(clientInfo);
    assertThat(features.getClientCapabilities()).isSameAs(capabilities);
    assertThat(features.getToolsChangeConsumers()).isSameAs(toolsConsumers);
    assertThat(features.getLoggingConsumers()).isSameAs(loggingConsumers);
  }

  @Test
  void toString_containsClassName() {
    McpClientFeatures features = new McpClientFeatures(
      new Implementation("client", "1.0.0"), ClientCapabilities.EMPTY, Collections.emptyList(), Collections.emptyList());

    assertThat(features.toString()).contains("McpClientFeatures");
    assertThat(features.toString()).contains("client");
  }
}
