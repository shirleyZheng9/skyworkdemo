package com.iwhalecloud.bote.mcp.client;

import com.iwhalecloud.bote.mcp.dto.request.CallToolRequest;
import com.iwhalecloud.bote.mcp.dto.response.CallToolResult;
import org.springframework.lang.Nullable;

/**
 * MCP 工具调用观察者（用于可观测性埋点）
 *
 * @author cursor
 * @since 2026-07-23
 */
public interface McpToolCallObserver {

  /**
   * 工具调用结束回调（成功或失败都会调用）
   *
   * @param clientName MCP 客户端名称
   * @param request 工具请求
   * @param result 工具结果，失败时可能为 null
   * @param startEpochMs 开始时间戳
   * @param endEpochMs 结束时间戳
   * @param error 异常，成功时为 null
   */
  void onToolCall(String clientName, CallToolRequest request, @Nullable CallToolResult result,
                  long startEpochMs, long endEpochMs, @Nullable Throwable error);
}
