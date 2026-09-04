package com.iwhalecloud.bote.llm.client.util;

import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

/**
 * 大模型链路追踪工具类。
 * <p>
 * 同一对话轮次内通过 {@code traceId} 聚合到一条 Langfuse Trace；
 * 通过 {@code rootObservationId} / {@code currentObservationId} 形成调用树：
 * <pre>
 *   agent-turn (root)
 *     ├─ generation (LLM)
 *     │    ├─ mcp:tool
 *     │    └─ knowledge-recall
 *     └─ generation (LLM)
 * </pre>
 *
 * @author bianjp
 * @since 2025-11-10
 */
public final class LlmTraceUtil {
  private LlmTraceUtil() {
  }

  /** 链路追踪标识（对应 Langfuse traceId） */
  private static final ThreadLocal<String> traceIdThreadLocal = new ThreadLocal<>();
  /** 本轮对话根 observation（agent-turn span） */
  private static final ThreadLocal<String> rootObservationIdThreadLocal = new ThreadLocal<>();
  /** 当前活跃 observation（通常为最近一次 generation，供 MCP/Knowledge 作为 parent） */
  private static final ThreadLocal<String> currentObservationIdThreadLocal = new ThreadLocal<>();

  /**
   * 获取链路追踪标识
   *
   * @return 链路追踪标识
   */
  @Nullable
  public static String getTraceId() {
    return traceIdThreadLocal.get();
  }

  /**
   * 设置链路追踪标识
   *
   * @param traceId 链路追踪标识，为空时清除
   */
  public static void setTraceId(@Nullable String traceId) {
    if (traceId == null) {
      traceIdThreadLocal.remove();
    }
    else {
      traceIdThreadLocal.set(traceId);
    }
  }

  /**
   * 若当前无线程 traceId 则生成一个并设置
   *
   * @return 非空 traceId
   */
  public static String ensureTraceId() {
    String traceId = traceIdThreadLocal.get();
    if (StringUtils.isBlank(traceId)) {
      traceId = UUID.randomUUID().toString();
      traceIdThreadLocal.set(traceId);
    }
    return traceId;
  }

  /**
   * 获取本轮对话根 observationId
   */
  @Nullable
  public static String getRootObservationId() {
    return rootObservationIdThreadLocal.get();
  }

  /**
   * 设置本轮对话根 observationId
   */
  public static void setRootObservationId(@Nullable String observationId) {
    if (observationId == null) {
      rootObservationIdThreadLocal.remove();
    }
    else {
      rootObservationIdThreadLocal.set(observationId);
    }
  }

  /**
   * 获取当前活跃 observationId（如最近一次 LLM generation）
   */
  @Nullable
  public static String getCurrentObservationId() {
    return currentObservationIdThreadLocal.get();
  }

  /**
   * 设置当前活跃 observationId，后续 MCP/Knowledge 会挂到其下
   */
  public static void setCurrentObservationId(@Nullable String observationId) {
    if (observationId == null) {
      currentObservationIdThreadLocal.remove();
    }
    else {
      currentObservationIdThreadLocal.set(observationId);
    }
  }

  /**
   * 解析新建子 observation 应使用的 parentObservationId：
   * 优先当前活跃节点（LLM generation），否则回落到本轮根节点。
   */
  @Nullable
  public static String getParentObservationId() {
    String current = currentObservationIdThreadLocal.get();
    if (StringUtils.isNotBlank(current)) {
      return current;
    }
    return rootObservationIdThreadLocal.get();
  }

  /**
   * 清除链路追踪相关线程本地变量
   */
  public static void clearTraceId() {
    traceIdThreadLocal.remove();
    rootObservationIdThreadLocal.remove();
    currentObservationIdThreadLocal.remove();
  }
}
