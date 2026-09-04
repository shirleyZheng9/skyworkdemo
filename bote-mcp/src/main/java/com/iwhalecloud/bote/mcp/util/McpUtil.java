package com.iwhalecloud.bote.mcp.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.iwhalecloud.bote.llm.client.util.ModelHttpClient;
import com.iwhalecloud.bote.mcp.dto.message.JsonRpcMessage;
import com.iwhalecloud.bote.mcp.dto.message.JsonRpcNotification;
import com.iwhalecloud.bote.mcp.dto.message.JsonRpcRequest;
import com.iwhalecloud.bote.mcp.dto.message.JsonRpcResponse;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import okhttp3.OkHttpClient;
import okhttp3.sse.EventSource.Factory;
import okhttp3.sse.EventSources;

/**
 * MCP 工具类
 *
 * @author bianjp
 * @since 2025-05-22
 */
public final class McpUtil {
  private McpUtil() {
  }

  /** SSE 客户端，读取超时时间设置为无限制，以避免 SseClientTransport 中连接超时断开 */
  private static final OkHttpClient sseHttpClient = ModelHttpClient.getClient().newBuilder()
    .readTimeout(0, TimeUnit.SECONDS)
    .build();
  /** SSE 工厂实例 */
  @Getter
  private static final Factory eventSourceFactory = EventSources.createFactory(sseHttpClient);

  /**
   * 反序列化 JSON-RPC 消息
   *
   * @param jsonText json 字符串
   * @return 消息对象
   */
  public static JsonRpcMessage deserializeMessage(String jsonText) {
    JsonNode node = JsonUtil.readTree(jsonText);
    // 根据 JSON 结构确定消息类型
    if (node.has("method") && node.has("id")) {
      return JsonUtil.convert(node, JsonRpcRequest.class);
    }
    else if (node.has("method") && !node.has("id")) {
      return JsonUtil.convert(node, JsonRpcNotification.class);
    }
    else if (node.has("result") || node.has("error")) {
      return JsonUtil.convert(node, JsonRpcResponse.class);
    }
    throw new BssException("未知的 JSON-RPC 消息类型: " + jsonText);
  }

}
