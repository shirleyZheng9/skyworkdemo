package com.iwhalecloud.bote.mcp.client;

import com.iwhalecloud.bote.mcp.dto.ClientCapabilities;
import com.iwhalecloud.bote.mcp.dto.Implementation;
import com.iwhalecloud.bote.mcp.dto.McpTool;
import com.iwhalecloud.bote.mcp.dto.response.LoggingMessageNotification;
import java.util.List;
import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import reactor.core.publisher.Mono;

/**
 * 客户端功能
 *
 * @author bianjp
 * @since 2025-05-22
 */
@Getter
@ToString
@RequiredArgsConstructor
class McpClientFeatures {
  /** 客户端信息 */
  private final Implementation clientInfo;
  /** 客户端能力 */
  private final ClientCapabilities clientCapabilities;
  /** 工具列表变化处理器 */
  private final List<Function<List<McpTool>, Mono<Void>>> toolsChangeConsumers;
  /** 日志处理器 */
  private final List<Function<LoggingMessageNotification, Mono<Void>>> loggingConsumers;
}
