package com.iwhalecloud.bote.mcp.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.iwhalecloud.bote.mcp.dto.message.JsonRpcMessage;
import com.iwhalecloud.bote.mcp.dto.message.JsonRpcNotification;
import com.iwhalecloud.bote.mcp.dto.message.JsonRpcRequest;
import com.iwhalecloud.bote.mcp.dto.message.JsonRpcResponse;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import okhttp3.sse.EventSource.Factory;
import org.junit.jupiter.api.Test;

/**
 * {@link McpUtil} 单元测试
 *
 * <p>覆盖 {@code deserializeMessage} 的全部分支（请求/通知/响应/未知）以及
 * {@code getEventSourceFactory} 单例获取。不依赖 Spring 环境。</p>
 */
class McpUtilTest {

  @Test
  void deserializeMessage_withMethodAndId_returnsRequest() {
    String json = "{\"jsonrpc\":\"2.0\",\"method\":\"ping\",\"id\":1,\"params\":{\"a\":1}}";
    JsonRpcMessage message = McpUtil.deserializeMessage(json);

    assertThat(message).isInstanceOf(JsonRpcRequest.class);
    JsonRpcRequest request = (JsonRpcRequest) message;
    assertThat(request.getMethod()).isEqualTo("ping");
    assertThat(request.getId()).isEqualTo(1);
    assertThat(request.getJsonrpc()).isEqualTo("2.0");
  }

  @Test
  void deserializeMessage_withMethodAndStringId_returnsRequest() {
    String json = "{\"jsonrpc\":\"2.0\",\"method\":\"tools/list\",\"id\":\"req-1\"}";
    JsonRpcMessage message = McpUtil.deserializeMessage(json);

    assertThat(message).isInstanceOf(JsonRpcRequest.class);
    assertThat(((JsonRpcRequest) message).getId()).isEqualTo("req-1");
  }

  @Test
  void deserializeMessage_withMethodNoId_returnsNotification() {
    String json = "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}";
    JsonRpcMessage message = McpUtil.deserializeMessage(json);

    assertThat(message).isInstanceOf(JsonRpcNotification.class);
    assertThat(((JsonRpcNotification) message).getMethod()).isEqualTo("notifications/initialized");
  }

  @Test
  void deserializeMessage_withResult_returnsResponse() {
    String json = "{\"jsonrpc\":\"2.0\",\"id\":2,\"result\":{\"tools\":[]}}";
    JsonRpcMessage message = McpUtil.deserializeMessage(json);

    assertThat(message).isInstanceOf(JsonRpcResponse.class);
    assertThat(((JsonRpcResponse) message).getId()).isEqualTo(2);
    assertThat(((JsonRpcResponse) message).getResult()).isNotNull();
  }

  @Test
  void deserializeMessage_withError_returnsResponse() {
    String json = "{\"jsonrpc\":\"2.0\",\"id\":3,\"error\":{\"code\":-32601,\"message\":\"not found\"}}";
    JsonRpcMessage message = McpUtil.deserializeMessage(json);

    assertThat(message).isInstanceOf(JsonRpcResponse.class);
    JsonRpcResponse response = (JsonRpcResponse) message;
    assertThat(response.getError()).isNotNull();
    assertThat(response.getError().getCode()).isEqualTo(-32601);
    assertThat(response.getError().getMessage()).isEqualTo("not found");
  }

  @Test
  void deserializeMessage_unknownType_throwsBssException() {
    String json = "{\"foo\":\"bar\"}";
    assertThatThrownBy(() -> McpUtil.deserializeMessage(json))
      .isInstanceOf(BssException.class)
      .hasMessageContaining("未知的 JSON-RPC 消息类型");
  }

  @Test
  void getEventSourceFactory_returnsNonNullSingleton() {
    Factory factory1 = McpUtil.getEventSourceFactory();
    Factory factory2 = McpUtil.getEventSourceFactory();

    assertThat(factory1).isNotNull();
    assertThat(factory2).isSameAs(factory1);
  }
}
