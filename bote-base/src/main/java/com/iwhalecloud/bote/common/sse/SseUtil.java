package com.iwhalecloud.bote.common.sse;

import com.iwhalecloud.bote.cache.SseEmitterCache;
import com.iwhalecloud.bote.common.consts.ChatConsts;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.enums.BaseSystemParameter;
import com.iwhalecloud.bote.common.sse.beyond.BeyondSseEmitter;
import com.iwhalecloud.bote.common.sse.beyond.BeyondSseEventBuilder;
import com.iwhalecloud.bote.common.sse.emitter.BoteSseEmitter;
import com.iwhalecloud.bote.common.sse.heartbeat.SseHeartbeatTask;
import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.io.IOException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

/**
 * SSE工具类
 *
 * @author Admin
 */
@SuppressWarnings("PMD.GuardLogStatement")
public final class SseUtil {
  private static final Logger logger = LoggerFactory.getLogger(SseUtil.class);
  /** SSE 超时时间(ms) */
  private static final long SSE_TIMEOUT_MILLIS = 10 * 60 * 1000;

  /** SseEmitter 缓存，用于中断请求 */
  private static final SseEmitterCache sseEmitterCache = SpringUtil.getBean(SseEmitterCache.class);
  /** 客户端 ID 线程本地变量 */
  private static final ThreadLocal<String> clientIdThreadLocal = new ThreadLocal<>();
  /** 请求监听器 */
  public static final Consumer<Object> requestListener = call -> sseEmitterCache.addResource(clientIdThreadLocal.get(), call);
  /** SSE 心跳任务 */
  private static final SseHeartbeatTask heartbeatRunner = SpringUtil.getBeanOptional(SseHeartbeatTask.class);

  private SseUtil() {
  }

  /**
   * 获取客户端 ID
   */
  @Nullable
  public static String getClientId() {
    return clientIdThreadLocal.get();
  }

  /**
   * 设置客户端 ID
   */
  public static void setClientId(@Nullable String clientId) {
    if (clientId != null) {
      clientIdThreadLocal.set(clientId);
    }
    else {
      clientIdThreadLocal.remove();
    }
  }

  /**
   * 删除客户端 ID
   */
  public static void removeClientId() {
    clientIdThreadLocal.remove();
  }

  /**
   * 创建 SseEmitter
   *
   * @param clientId 客户端 ID, 用于实现中断会话
   * @param consumer 消费函数，用于发送流式内容，异步在其它线程中执行
   */
  public static SseEmitter createSseEmitter(@Nullable String clientId, Consumer<SseEmitter> consumer) {
    return createSseEmitter(clientId, false, false, consumer);
  }

  /**
   * 创建 SseEmitter
   *
   * @param clientId 客户端 ID, 用于实现中断会话
   * @param useChat 是否用于博特正式对话
   * @param useBeyond 是否使用百应
   * @param consumer 消费函数，用于发送流式内容，异步在其它线程中执行
   */
  @SuppressWarnings("PMD.AvoidCatchingThrowable")
  public static SseEmitter createSseEmitter(@Nullable String clientId, boolean useChat, boolean useBeyond, Consumer<SseEmitter> consumer) {
    // SSE 的超时时间要大于工作流执行时间限制，避免场景还没执行完 SSE 就超时了
    long flowExecutionTimeLimit = BaseSystemParameter.FLOW_EXECUTION_TIME_LIMIT.getRequiredIntegerValueFromDb().longValue() * 1000L;
    long timeout = Math.max(flowExecutionTimeLimit, SSE_TIMEOUT_MILLIS);
    SseEmitter sseEmitter;
    if (useBeyond) {
      sseEmitter = new BeyondSseEmitter(timeout);
      // 对接百应只有所内环境使用，不太会遇到超时问题，暂不做心跳检查
    }
    else {
      sseEmitter = new BoteSseEmitter(timeout, clientId, useChat);
      if (heartbeatRunner != null) {
        heartbeatRunner.addEmitter((BoteSseEmitter) sseEmitter);
      }
    }
    // 缓存 SseEmitter 实例，用于中断会话
    if (clientId != null) {
      sseEmitterCache.addResource(clientId, sseEmitter);
      // 会话结束时删除缓存
      sseEmitter.onCompletion(() -> sseEmitterCache.invalidate(clientId));
    }
    sseEmitter.onError(t -> {
      if (t instanceof IOException) {
        logger.warn("SSE Error: clientId={}", clientId, t);
      }
      else {
        logger.error("SSE Error: clientId={}", clientId, t);
      }
    });
    sseEmitter.onTimeout(() -> logger.warn("SSE Timeout: clientId={}", clientId));

    // 使用线程池异步执行
    try {
      Future<?> future = ThreadPools.getSse().submit(() -> {
        setClientId(clientId);
        try {
          consumer.accept(sseEmitter);
        }
        catch (Throwable e) {
          if (isDisconnected(e)) {
            logger.warn("SSE is already disconnected: clientId={}, error={}", clientId, e.getMessage());
          }
          else {
            logger.error("Failed to process SSE event: clientId={}", clientId, e);
          }
          // 检测连接是否断开不一定完全可靠，要确保会关闭连接
          completeQuietly(sseEmitter);
        }
        finally {
          removeClientId();
        }
      });
      // 缓存 future 用于实现中断
      sseEmitterCache.addResource(clientId, future);
    }
    catch (Exception e) {
      logger.warn("Failed to submit SSE task: clientId={}", clientId, e);
      completeQuietly(sseEmitter);
      if (clientId != null) {
        sseEmitterCache.invalidate(clientId);
      }
      throw e;
    }

    return sseEmitter;
  }

  /**
   * 是否是连接断开异常
   */
  private static boolean isDisconnected(Throwable throwable) {
    // com.iwhalecloud.bote.common.sse.SseUtil#send
    if (throwable instanceof BssException) {
      return throwable.getMessage().contains("客户端已断开");
    }
    // org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter#send(java.lang.Object, org.springframework.http.MediaType)
    if (throwable instanceof IllegalStateException) {
      return Strings.CS.startsWith(throwable.getMessage(), "ResponseBodyEmitter has already completed");
    }
    // org.springframework.web.context.request.async.StandardServletAsyncWebRequest.LifecycleHttpServletResponse#handleIOException
    return throwable instanceof AsyncRequestNotUsableException;
  }

  /**
   * 发送文本消息
   *
   * @param sseEmitter SSE 触发器
   * @param msgType 消息类型
   * @param text 文本内容
   */
  public static void sendText(SseEmitter sseEmitter, ChatMessageType msgType, String text) {
    sendText(sseEmitter, msgType.getCode(), text);
  }

  /**
   * 发送文本消息
   *
   * @param sseEmitter SSE 触发器
   * @param event 事件名称
   * @param text 文本内容
   */
  public static void sendText(SseEmitter sseEmitter, @Nullable String event, String text) {
    SseEventBuilder builder = createEventBuilder(sseEmitter);
    if (event != null) {
      builder.name(event);
    }
    builder.data(text);
    send(sseEmitter, builder, ChatConsts.COMPLETIONS_DONE.equals(text));
  }

  /**
   * 发送 JSON 消息
   *
   * @param sseEmitter SSE 触发器
   * @param msgType 消息类型
   * @param data 消息内容
   */
  public static void sendJson(SseEmitter sseEmitter, ChatMessageType msgType, Object data) {
    sendJson(sseEmitter, msgType.getCode(), null, data);
  }

  /**
   * 发送 JSON 消息
   *
   * @param sseEmitter SSE 触发器
   * @param msgType 消息类型
   * @param id 事件 ID
   * @param data 消息内容
   */
  public static void sendJson(SseEmitter sseEmitter, ChatMessageType msgType, @Nullable String id, Object data) {
    sendJson(sseEmitter, msgType.getCode(), id, data);
  }

  /**
   * 发送 JSON 消息
   *
   * @param sseEmitter SSE 触发器
   * @param event 事件名称
   * @param id 事件 ID
   * @param data 消息内容
   */
  public static void sendJson(SseEmitter sseEmitter, @Nullable String event, @Nullable String id, Object data) {
    SseEventBuilder builder = buildEvent(createEventBuilder(sseEmitter), event, id, data);
    // 发送错误信息失败时忽略异常
    boolean ignoreFailure = ChatMessageType.ERROR.getCode().equals(event) || ChatMessageType.EXCEPTION.getCode().equals(event);
    send(sseEmitter, builder, ignoreFailure);
  }

  /**
   * 创建 SSE 事件构造器
   *
   * <p>对接百应时使用自定义的构造器，以便自定义的 emitter 能从构造器获取原始信息</p>
   */
  private static SseEventBuilder createEventBuilder(SseEmitter sseEmitter) {
    if (sseEmitter instanceof BeyondSseEmitter) {
      return new BeyondSseEventBuilder();
    }
    return SseEmitter.event();
  }

  private static SseEventBuilder buildEvent(SseEventBuilder builder, @Nullable String event, @Nullable String id, Object data) {
    if (id != null) {
      builder.id(id);
    }
    if (event != null) {
      builder.name(event);
    }
    // 如果是字符串类型，需要手动转换(即使指定了 MediaType.APPLICATION_JSON, data 方法内部也不会对字符串类型做转换)
    // 纯文本也要转为 JSON 的目的是为了保持文本中的换行符（如果不转, 换行符会被当作两个 SSE 事件的分隔符，导致换行符丢失）
    if (data instanceof String) {
      builder.data(JsonUtil.toJsonString(data));
    }
    else {
      builder.data(data, MediaType.APPLICATION_JSON);
    }
    return builder;
  }

  /**
   * 发送消息
   */
  @SuppressWarnings("PMD.PreserveStackTrace")
  private static void send(SseEmitter sseEmitter, SseEventBuilder builder, boolean ignoreFailure) {
    // 会话接口需要特殊处理，连接断开时继续执行
    String clientId;
    if (sseEmitter instanceof BoteSseEmitter boteSseEmitter && boteSseEmitter.isChatApi()) {
      clientId = boteSseEmitter.getClientId();
      if (boteSseEmitter.isClosed() || sseEmitterCache.isDisconnected(clientId)) {
        return;
      }
    }
    else {
      clientId = null;
    }

    try {
      sseEmitter.send(builder);
    }
    catch (Exception e) {
      // 忽略异常时只打印日志
      if (ignoreFailure) {
        logger.warn("Failed to send SSE message: {}", ExpUtil.getMsg(e));
      }
      // 连接断开时不打印异常堆栈，避免打印太多日志
      else if (isDisconnected(e)) {
        logger.warn("Failed to send SSE message, client disconnected: {}", e.getMessage());
        if (StringUtils.isNotEmpty(clientId)) {
          // 正式对话，客户端断开连接时，不抛出异常，让后端处理逻辑继续运行
          // 只是静默记录日志，不中断处理流程
          sseEmitterCache.setDisconnected(clientId);
        }
        else {
          throw new BssException("发送 SSE 消息失败: 客户端已断开");
        }
      }
      else {
        throw new BssException("发送 SSE 消息失败: " + ExpUtil.getMsg(e), e);
      }
    }
  }

  /**
   * 静默关闭 SseEmitter
   */
  public static void completeQuietly(SseEmitter sseEmitter) {
    try {
      sseEmitter.complete();
    }
    catch (Exception e) {
      logger.warn("Failed to complete SseEmitter", e);
    }
  }
}
