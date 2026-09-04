package com.iwhalecloud.bote.mcp.client.transport;

import com.iwhalecloud.bote.mcp.dto.message.JsonRpcMessage;
import java.time.Duration;
import java.util.function.Function;
import reactor.core.publisher.Mono;

/**
 * MCP 客户端传输协议
 *
 * @author bianjp
 * @since 2025-05-20
 */
public interface McpClientTransport {

  /**
   * 连接服务器
   *
   * @param handler 消息处理器
   * @param disconnectionHandler 连接异常断开处理器，用于实现自动重连
   */
  Mono<Void> connect(Function<Mono<JsonRpcMessage>, Mono<JsonRpcMessage>> handler, Runnable disconnectionHandler);

  /**
   * 检查是否已连接
   */
  boolean isConnected();

  /**
   * 发送消息
   *
   * @param message 消息
   */
  Mono<Void> sendMessage(JsonRpcMessage message);

  /**
   * 关闭连接
   */
  default void close() {
    this.closeGracefully().timeout(Duration.ofSeconds(10)).subscribe();
  }

  /**
   * 优雅关闭连接
   */
  Mono<Void> closeGracefully();
}
