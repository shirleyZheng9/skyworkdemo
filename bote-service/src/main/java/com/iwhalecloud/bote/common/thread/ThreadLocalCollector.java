package com.iwhalecloud.bote.common.thread;

import com.iwhalecloud.bote.common.sse.SseUtil;
import com.iwhalecloud.bote.common.util.ChatContextUtil;
import com.iwhalecloud.bote.common.util.SceneContextUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.common.util.TenantIdUtil;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bote.llm.client.util.LlmTraceUtil;
import java.util.function.Supplier;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * 线程本地变量收集器
 *
 * @author bianjp
 * @since 2025-06-09
 */
public class ThreadLocalCollector {
  /** 提交任务的线程 ID */
  private final long originalThreadId = Thread.currentThread().threadId();
  /** HTTP 请求 */
  private final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
  /** 登录会话 ID */
  private final String sessionId = SessionUtil.getSessionId();
  /** 登录信息 */
  private final LoginInfo loginInfo = SessionUtil.getOptionalLoginInfo();
  /** 租户 ID */
  private final Long tenantId = TenantIdUtil.getTenantIdOptional();
  /** 编排引擎上下文 */
  private final SceneOrchestrationContext orchestrationContext = SceneContextUtil.getOptionalContext();
  /** SSE 客户端 ID */
  private final String sseClientId = SseUtil.getClientId();
  /** 大模型链路追踪标识 */
  private final String llmTraceId = LlmTraceUtil.getTraceId();
  private final String llmRootObservationId = LlmTraceUtil.getRootObservationId();
  private final String llmCurrentObservationId = LlmTraceUtil.getCurrentObservationId();
  private final String chatSessionId = ChatContextUtil.getChatSessionId();

  /**
   * 运行任务，自动处理线程本地变量
   */
  public void run(Runnable runnable) {
    // 如果执行任务和提交任务是同一个线程，不需要处理
    if (Thread.currentThread().threadId() == originalThreadId) {
      runnable.run();
      return;
    }
    try {
      set();
      runnable.run();
    }
    finally {
      clear();
    }
  }

  /**
   * 运行任务，自动处理线程本地变量
   */
  @Nullable
  public <T> T run(Supplier<T> supplier) {
    // 如果执行任务和提交任务是同一个线程，不需要处理
    if (Thread.currentThread().threadId() == originalThreadId) {
      return supplier.get();
    }
    try {
      set();
      return supplier.get();
    }
    finally {
      clear();
    }
  }

  /**
   * 设置线程本地变量
   */
  private void set() {
    RequestContextHolder.setRequestAttributes(requestAttributes);
    SessionUtil.setSessionId(sessionId);
    SessionUtil.setLoginInfo(loginInfo);
    TenantIdUtil.setTenantId(tenantId);
    SseUtil.setClientId(sseClientId);
    LlmTraceUtil.setTraceId(llmTraceId);
    LlmTraceUtil.setRootObservationId(llmRootObservationId);
    LlmTraceUtil.setCurrentObservationId(llmCurrentObservationId);
    if (orchestrationContext != null) {
      SceneContextUtil.setContext(orchestrationContext);
    }
    ChatContextUtil.setChatSessionId(chatSessionId);
  }

  /**
   * 清理线程本地变量
   */
  private void clear() {
    RequestContextHolder.resetRequestAttributes();
    SessionUtil.clearThreadLocal();
    TenantIdUtil.clearThreadLocal();
    if (orchestrationContext != null) {
      SceneContextUtil.removeContext();
    }
    ChatContextUtil.clear();
    SseUtil.removeClientId();
    LlmTraceUtil.clearTraceId();
  }
}
