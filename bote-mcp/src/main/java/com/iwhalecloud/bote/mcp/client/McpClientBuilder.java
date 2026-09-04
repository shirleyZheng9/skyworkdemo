package com.iwhalecloud.bote.mcp.client;

import com.iwhalecloud.bote.mcp.client.transport.McpClientTransport;
import com.iwhalecloud.bote.mcp.client.transport.SseClientTransport;
import com.iwhalecloud.bote.mcp.client.transport.StdioClientTransport;
import com.iwhalecloud.bote.mcp.client.transport.StreamableClientTransport;
import com.iwhalecloud.bote.mcp.config.McpClientProperties;
import com.iwhalecloud.bote.mcp.dto.ClientCapabilities;
import com.iwhalecloud.bote.mcp.dto.Implementation;
import com.iwhalecloud.bote.mcp.dto.McpTool;
import com.iwhalecloud.bote.mcp.dto.StdioServerParameters;
import com.iwhalecloud.bote.mcp.dto.response.LoggingMessageNotification;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import okhttp3.Headers;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * MCP 客户端构造器
 *
 * @author bianjp
 * @since 2025-05-22
 */
public final class McpClientBuilder {
  /** 客户端配置 */
  private static final McpClientProperties properties = SpringUtil.getBean(McpClientProperties.class, McpClientProperties::new);

  /** 客户端名称 */
  private final String name;
  /** 会话前缀，用于隔离不同会话 */
  private final String sessionPrefix;
  /** 客户端标识，用于日志中区分不同客户端 */
  private final String clientId;
  /** 传输协议 */
  private McpClientTransport transport;
  /** 请求超时时间 */
  private Duration requestTimeout;
  /** 初始化超时时间 */
  private Duration initializationTimeout;
  /** 客户端信息 */
  private Implementation clientInfo = new Implementation("Bote MCP Client", "1.0.0");
  /** 工具列表变化处理器列表 */
  private final List<Consumer<List<McpTool>>> toolsChangeConsumers = new ArrayList<>();
  /** 日志通知处理器列表 */
  private final List<Consumer<LoggingMessageNotification>> loggingConsumers = new ArrayList<>();

  McpClientBuilder(String name) {
    this.name = name;
    this.sessionPrefix = UUID.randomUUID().toString().substring(0, 8);
    this.clientId = name + "@" + this.sessionPrefix;
    this.requestTimeout = Duration.ofSeconds(properties.getRequestTimeout());
    this.initializationTimeout = Duration.ofSeconds(properties.getInitializationTimeout());
  }

  /**
   * 设置请求超时时间
   */
  public McpClientBuilder requestTimeout(Duration requestTimeout) {
    this.requestTimeout = requestTimeout;
    return this;
  }

  /**
   * 设置初始化超时时间
   */
  public McpClientBuilder initializationTimeout(Duration initializationTimeout) {
    this.initializationTimeout = initializationTimeout;
    return this;
  }


  /**
   * 设置客户端信息
   */
  public McpClientBuilder clientInfo(Implementation clientInfo) {
    this.clientInfo = clientInfo;
    return this;
  }

  /**
   * 添加工具列表变化处理器
   */
  public McpClientBuilder toolsChangeConsumer(Consumer<List<McpTool>> toolsChangeConsumer) {
    this.toolsChangeConsumers.add(toolsChangeConsumer);
    return this;
  }

  /**
   * 添加日志通知处理器
   */
  public McpClientBuilder loggingConsumer(Consumer<LoggingMessageNotification> loggingConsumer) {
    this.loggingConsumers.add(loggingConsumer);
    return this;
  }

  /**
   * 添加多个日志通知处理器
   */
  public McpClientBuilder loggingConsumers(List<Consumer<LoggingMessageNotification>> loggingConsumers) {
    this.loggingConsumers.addAll(loggingConsumers);
    return this;
  }

  /**
   * 设置 stdio 协议
   */
  public McpClientBuilder stdio(StdioServerParameters serverParameters) {
    this.transport = new StdioClientTransport(clientId, serverParameters);
    return this;
  }

  /**
   * 设置 sse 协议
   */
  public McpClientBuilder sse(String url, @Nullable String endpoint, @Nullable Headers headers) {
    this.transport = new SseClientTransport(clientId, url, endpoint, headers);
    return this;
  }

  /**
   * 设置 streamable 协议
   */
  public McpClientBuilder streamable(String url, @Nullable Headers headers) {
    this.transport = new StreamableClientTransport(clientId, url, headers);
    return this;
  }

  /**
   * 构造客户端实例
   */
  public McpClient build() {
    Assert.notNull(transport, "未指定传输协议");
    List<Function<List<McpTool>, Mono<Void>>> asyncToolsChangeConsumers = toolsChangeConsumers.stream()
      .<Function<List<McpTool>, Mono<Void>>>map(consumer ->
        t -> Mono.<Void>fromRunnable(() -> consumer.accept(t)).subscribeOn(Schedulers.boundedElastic()))
      .collect(Collectors.toList());
    List<Function<LoggingMessageNotification, Mono<Void>>> asyncLoggingConsumers = loggingConsumers.stream()
      .<Function<LoggingMessageNotification, Mono<Void>>>map(consumer ->
        l -> Mono.<Void>fromRunnable(() -> consumer.accept(l)).subscribeOn(Schedulers.boundedElastic()))
      .collect(Collectors.toList());
    McpClientFeatures features = new McpClientFeatures(clientInfo, ClientCapabilities.EMPTY, asyncToolsChangeConsumers, asyncLoggingConsumers);
    return new McpClient(name, clientId, sessionPrefix, initializationTimeout, requestTimeout, features, transport);
  }
}
