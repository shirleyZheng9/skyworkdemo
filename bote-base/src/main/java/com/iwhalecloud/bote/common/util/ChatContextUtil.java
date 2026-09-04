package com.iwhalecloud.bote.common.util;

import org.springframework.lang.Nullable;

/**
 * 会话标识上下文持有器。
 *
 * <p>用于在调用下游服务（如 MCP）时，将当前对话/会话的唯一标识透传到
 * 请求头等位置，避免基础工具类直接依赖上层场景编排等模块。</p>
 *
 * <p>约定：上层在进入一次完整调用链时设置会话标识，调用结束后清理。</p>
 *
 * @author auto
 * @since 2026-01-27
 */
public final class ChatContextUtil {

  private static final ThreadLocal<String> CHAT_SESSION_ID = new ThreadLocal<>();

  private ChatContextUtil() {
  }

  /**
   * 设置当前线程的会话标识。
   *
   * @param sessionId 会话标识，允许为 null
   */
  public static void setChatSessionId(@Nullable String sessionId) {
    if (sessionId == null) {
      CHAT_SESSION_ID.remove();
    }
    else {
      CHAT_SESSION_ID.set(sessionId);
    }
  }

  /**
   * 获取当前线程的会话标识。
   *
   * @return 会话标识，可能为 null
   */
  @Nullable
  public static String getChatSessionId() {
    return CHAT_SESSION_ID.get();
  }

  /**
   * 清理当前线程的会话标识。
   */
  public static void clear() {
    CHAT_SESSION_ID.remove();
  }
}

