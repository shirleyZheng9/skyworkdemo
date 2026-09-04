package com.iwhalecloud.bote.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.dto.mcp.SimpleMcpServiceDTO;
import com.iwhalecloud.bote.mcp.client.McpClient;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.cache.refresh.Refreshable;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

/**
 * 动态 MCP 客户端缓存
 *
 * @author bianjp
 * @since 2025-06-02
 */
@Component
@SuppressWarnings("PMD.GuardLogStatement")
public class DynamicMcpClientCache implements Refreshable, DisposableBean {
  private static final Logger logger = LoggerFactory.getLogger(DynamicMcpClientCache.class);

  /** 客户端缓存。key 为 (serverType, serverUrl) */
  private final Cache<@NonNull Pair<String, String>, @NonNull McpClient> cache = CacheBuilder.newBuilder()
    .maximumSize(100)
    .expireAfterAccess(Duration.ofHours(24))
    .removalListener(this::closeMcpClient)
    .build();

  @Override
  public String getCacheName() {
    return CacheConsts.CACHE_NAME_DYNAMIC_MCP_CLIENT;
  }

  @Override
  public boolean isDistributedCacheEnabled() {
    return false;
  }

  /**
   * 获取 MCP 客户端
   *
   * @param serverName 服务名称
   * @param serverType 服务类型
   * @param serverUrl 服务地址
   * @return MCP 客户端
   */
  public McpClient getClient(String serverName, String serverType, String serverUrl) {
    Pair<String, String> key = Pair.of(serverType, serverUrl);
    McpClient client = getClientFromCache(key, serverName);
    // 检查客户端状态，实现自动重连
    try {
      client.waitInitialized();
    }
    catch (Exception e) {
      // 自动重连失败时，关闭客户端，重新创建一个实例
      logger.warn("Failed to wait mcp client initialized, try recreating: serverName={}, serverType={}, serverUrl={}", serverName, serverType, serverUrl, e);
      client.close();
      cache.invalidate(key);
      return getClientFromCache(key, serverName);
    }
    return client;
  }

  @SuppressWarnings("PMD.PreserveStackTrace")
  private McpClient getClientFromCache(Pair<String, String> key, String serverName) {
    String serverType = key.getLeft();
    String serverUrl = key.getRight();
    try {
      return cache.get(key, () -> {
        SimpleMcpServiceDTO service = new SimpleMcpServiceDTO();
        service.setServerType(serverType);
        service.setServerUrl(serverUrl);
        service.setServerName(serverName);
        return McpClientCache.buildMcpClient(service);
      });
    }
    catch (ExecutionException | UncheckedExecutionException e) {
      if (e.getCause() instanceof BssException) {
        throw (BssException) e.getCause();
      }
      logger.error("Failed to create mcp client: serverName={}, serverType={}, serverUrl={}", serverName, serverType, serverUrl, e);
      throw new BssException("构造 MCP 客户端失败: " + ExpUtil.getMsg(e.getCause()), e.getCause());
    }
    catch (Exception e) {
      logger.error("Failed to create mcp client: serverName={}, serverType={}, serverUrl={}", serverName, serverType, serverUrl, e);
      throw new BssException("构造 MCP 客户端失败: " + ExpUtil.getMsg(e), e);
    }
  }

  /**
   * 释放缓存时关闭 MCP 客户端
   */
  private void closeMcpClient(@SuppressWarnings("NullableProblems") RemovalNotification<Pair<String, String>, McpClient> notification) {
    McpClient client = notification.getValue();
    if (client != null) {
      Pair<String, String> key = notification.getKey();
      String serverType = key != null ? key.getLeft() : null;
      String serverUrl = key != null ? key.getRight() : null;
      logger.debug("Releasing mcp client: serverType={}, serverUrl={}, cause={}", serverType, serverUrl, notification.getCause());
      client.close();
    }
  }

  @Override
  public void refreshLocalCache() {
    // 不支持全量刷新
  }

  @Override
  public void refreshLocalCache(List<String> keys) {
    logger.debug("Refresh local by keys: cacheName={}, keys={}", getCacheName(), keys);
    if (CollectionUtils.isEmpty(keys)) {
      return;
    }
    for (String key : keys) {
      String[] pieces = StringUtils.split(key, ":", 2);
      if (pieces != null && pieces.length == 2) {
        cache.invalidate(Pair.of(pieces[0], pieces[1]));
      }
    }
  }

  @Override
  public void refresh() {
    // 不支持全量刷新
  }

  @Override
  public void refresh(List<String> keys) {
    refreshLocalCache(keys);
  }

  @Override
  public void destroy() {
    // 应用关闭时关闭所有 MCP 客户端
    Collection<McpClient> clients = cache.asMap().values();
    if (!clients.isEmpty()) {
      logger.debug("Closing dynamic mcp clients ...");
      clients.stream().parallel().forEach(McpClient::close);
    }
  }
}
