package com.iwhalecloud.bote.mcp.consts;

import com.google.common.collect.ImmutableList;
import java.util.List;

/**
 * MCP 相关常量
 *
 * @author bianjp
 * @since 2025-05-22
 */
public final class McpConsts {
  private McpConsts() {
  }

  /** MCP 协议版本: 第一版 */
  public static final String PROTOCOL_VERSION_2024_11_05 = "2024-11-05";
  /** MCP 协议版本: 第二版(streamable 代替 sse) */
  public static final String PROTOCOL_VERSION_2025_03_26 = "2025-03-26";
  /** MCP 协议版本: 第三版 */
  public static final String PROTOCOL_VERSION_2025_06_18 = "2025-06-18";
  /** 客户端支持的协议版本列表 */
  public static final List<String> PROTOCOL_VERSIONS = ImmutableList.of(
    PROTOCOL_VERSION_2024_11_05,
    PROTOCOL_VERSION_2025_03_26,
    PROTOCOL_VERSION_2025_06_18);

  /** JSON-RPC 协议版本 */
  public static final String JSONRPC_VERSION = "2.0";

  /** MCP 传输协议: stdio */
  public static final String TRANSPORT_STDIO = "stdio";
  /** MCP 传输协议: sse */
  public static final String TRANSPORT_SSE = "sse";
  /** MCP 传输协议: streamable HTTP */
  public static final String TRANSPORT_STREAMABLE = "streamable";

  /** SSE 事件类型: 消息 */
  public static final String EVENT_TYPE_MESSAGE = "message";
  /** SSE 事件类型: 服务发现 */
  public static final String EVENT_TYPE_ENDPOINT = "endpoint";

  /** 工具执行结果的内容类型: 文本 */
  public static final String CONTENT_TYPE_TEXT = "text";
  /** 工具执行结果的内容类型: 图片 */
  public static final String CONTENT_TYPE_IMAGE = "image";

  // ---------------------------
  // 方法名称
  // ---------------------------
  /** 初始化 */
  public static final String METHOD_INITIALIZE = "initialize";
  /** 初始化完成通知 */
  public static final String METHOD_NOTIFICATION_INITIALIZED = "notifications/initialized";
  /** ping */
  public static final String METHOD_PING = "ping";
  /** 获取工具列表 */
  public static final String METHOD_TOOLS_LIST = "tools/list";
  /** 调用工具 */
  public static final String METHOD_TOOLS_CALL = "tools/call";
  /** 工具列表变化 */
  public static final String METHOD_NOTIFICATION_TOOLS_LIST_CHANGED = "notifications/tools/list_changed";
  /** 设置日志级别 */
  public static final String METHOD_LOGGING_SET_LEVEL = "logging/setLevel";
  /** 通知 */
  public static final String METHOD_NOTIFICATION_MESSAGE = "notifications/message";
  /** 获取 root 列表 */
  public static final String METHOD_ROOTS_LIST = "roots/list";

}
