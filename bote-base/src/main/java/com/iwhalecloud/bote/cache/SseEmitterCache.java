package com.iwhalecloud.bote.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.cache.refresh.Refreshable;
import java.time.Duration;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import okhttp3.Call;
import okhttp3.WebSocket;
import okhttp3.sse.EventSource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE 资源缓存
 *
 * @author chen.linfa
 * @author bianjp
 * @since 2024-12-03
 */
@Component
public class SseEmitterCache implements Refreshable {
  private static final Logger logger = LoggerFactory.getLogger(SseEmitterCache.class);

  /** 资源缓存(key 为 clientId, value 为资源列表，按添加顺序倒序存储，释放资源时正序释放) */
  private final Cache<@NonNull String, @NonNull Deque<Object>> resourcesCache = CacheBuilder.newBuilder()
    .maximumSize(1000)
    .expireAfterAccess(Duration.ofMinutes(30))
    .build();
  /** 取消标志缓存(key 为 clientId, value 固定为 true), 记录主动中断请求（调用 chat/cancel 接口）的客户端，以便在智能体执行失败时做不同处理 */
  private final Cache<@NonNull String, @NonNull Boolean> cancelFlagCache = CacheBuilder.newBuilder()
    .maximumSize(1000)
    .expireAfterWrite(Duration.ofMinutes(30))
    .build();
  /** 连接断开缓存(key 为 clientId, value 固定为 true), 记录连接断开的客户端，以便在智能体跳过 SSE 消息发送并继续执行 */
  private final Cache<@NonNull String, @NonNull Boolean> disconnectedCache = CacheBuilder.newBuilder()
    .maximumSize(1000)
    .expireAfterWrite(Duration.ofMinutes(30))
    .build();

  @Override
  public String getCacheName() {
    return CacheConsts.CACHE_NAME_SSE_EMITTER;
  }

  /**
   * 添加资源
   */
  public void addResource(@Nullable String clientId, @Nullable Object resource) {
    if (clientId == null || resource == null) {
      return;
    }
    Deque<Object> resources;
    try {
      resources = resourcesCache.get(clientId, ConcurrentLinkedDeque::new);
    }
    catch (ExecutionException e) {
      throw new BssException("获取客户端的资源缓存失败", e);
    }
    // 按添加顺序倒序存储
    resources.push(resource);
  }

  /**
   * 检查客户端是否主动关闭了连接
   */
  public boolean isCancelled(@Nullable String clientId) {
    if (clientId == null) {
      return false;
    }
    return Boolean.TRUE.equals(cancelFlagCache.getIfPresent(clientId));
  }

  /**
   * 记录客户端是否断开连接
   */
  public boolean isDisconnected(@Nullable String clientId) {
    if (clientId == null) {
      return false;
    }
    return Boolean.TRUE.equals(disconnectedCache.getIfPresent(clientId));
  }

  /**
   * 检查客户端是否断开连接
   */
  public void setDisconnected(@Nullable String clientId) {
    if (StringUtils.isNotEmpty(clientId)) {
      disconnectedCache.put(clientId, true);
    }
  }

  /**
   * 删除缓存（正常结束时使用，不管 emitter, task 的状态）
   */
  public void invalidate(String clientId) {
    resourcesCache.invalidate(clientId);
  }

  @Override
  public boolean isDistributedCacheEnabled() {
    return false;
  }

  /**
   * 借助缓存刷新机制，广播通知各个服务器节点，按需结束对话
   *
   * <p>TODO RestTemplate 的请求暂时无法中断</p>
   */
  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public void refreshLocalCache(List<String> keys) {
    // 只支持单个刷新
    if (CollectionUtils.size(keys) != 1) {
      return;
    }

    String clientId = keys.get(0);
    Deque<Object> resources = resourcesCache.getIfPresent(clientId);
    if (resources == null) {
      logger.trace("No SSE resources found: clientId={}", clientId);
      return;
    }

    // 标记客户端主动取消
    cancelFlagCache.put(clientId, true);

    // 释放资源、中断任务
    for (Object resource : resources) {
      try {
        closeResource(resource);
        logger.trace("Closed SSE resource: clientId={}, type={}", clientId, resource.getClass().getName());
      }
      catch (Exception e) {
        logger.warn("Failed to close SSE resource: clientId={}, type={}", clientId, resource.getClass().getName(), e);
      }
    }

    // 删除缓存
    resourcesCache.invalidate(clientId);
  }

  /**
   * 释放资源
   */
  @SuppressWarnings("PMD.CloseResource")
  private static void closeResource(Object resource) throws Exception {
    switch (resource) {
      case Future<?> future -> future.cancel(true);
      case EventSource eventSource -> eventSource.cancel();
      case SseEmitter emitter -> emitter.complete();
      case Call call -> call.cancel();
      case WebSocket webSocket -> webSocket.cancel();
      case AutoCloseable autoCloseable -> autoCloseable.close();
      default -> logger.warn("Unsupported resource type: {}", resource.getClass().getCanonicalName());
    }
  }

  @Override
  public void refresh(List<String> keys) {
    refreshLocalCache(keys);
  }
}
