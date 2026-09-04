package com.iwhalecloud.bote.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.dto.plugin.SimplePluginAuthParam;
import com.iwhalecloud.bote.dto.plugin.response.PluginDefinition;
import com.iwhalecloud.bote.dto.plugin.response.PluginDefinition.PluginGatewayDTO;
import com.iwhalecloud.bote.mcp.client.McpClient;
import com.iwhalecloud.bote.mcp.client.McpClientBuilder;
import com.iwhalecloud.bote.mcp.consts.McpConsts;
import com.iwhalecloud.bote.service.plugin.IPluginManageService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import okhttp3.Headers;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 插件市场 MCP 客户端缓存
 *
 * @author chen.linfa
 * @since 2025-12-24
 */
@Component
@RequiredArgsConstructor
@SuppressFBWarnings("CRLF_INJECTION_LOGS")
@SuppressWarnings("PMD.GuardLogStatement")
public class PluginHubMcpClientCache implements TenantCacheMarker, DisposableBean {
  private static final Logger logger = LoggerFactory.getLogger(PluginHubMcpClientCache.class);

  /** 缓存实例 */
  private final LoadingCache<@NonNull Pair<Long, Long>, @NonNull McpClient> cache = CacheBuilder.newBuilder()
    .maximumSize(100)
    .expireAfterAccess(Duration.ofHours(1))
    .removalListener(this::closeMcpClient)
    .build(CacheLoader.from(this::buildMcpClient));

  private final IPluginManageService pluginManageService;

  @Override
  public String getCacheName() {
    return CacheConsts.CACHE_NAME_PLUGIN_HUB_MCP;
  }

  /**
   * 获取 MCP 客户端
   */
  @SuppressWarnings("PMD.PreserveStackTrace")
  public McpClient getMcpClient(Long tenantId, Long pluginId) {
    Pair<Long, Long> key = Pair.of(tenantId, pluginId);
    McpClient client = getClientFromCache(key);
    // 检查客户端状态，实现自动重连
    try {
      client.waitInitialized();
    }
    catch (Exception e) {
      // 自动重连失败时，关闭客户端，重新创建一个实例
      if (logger.isWarnEnabled()) {
        logger.warn("Failed to wait mcp client initialized, try recreating: tenantId={}, pluginId={}", tenantId, pluginId, e);
      }
      client.close();
      cache.invalidate(key);
      return getClientFromCache(key);
    }
    return client;
  }

  /**
   * 从缓存获取客户端实例，缓存不存在时自动创建实例
   */
  @SuppressWarnings("PMD.PreserveStackTrace")
  private McpClient getClientFromCache(Pair<Long, Long> key) {
    try {
      return cache.get(key);
    }
    catch (ExecutionException e) {
      if (e.getCause() instanceof BssException) {
        throw (BssException) e.getCause();
      }
      throw new BssException("获取插件 MCP 客户端失败: " + e.getMessage(), e.getCause());
    }
  }

  /**
   * 构造 MCP 客户端
   */
  private McpClient buildMcpClient(Pair<Long, Long> key) {
    Long tenantId = key.getLeft();
    Long pluginId = key.getRight();

    ResultVO<PluginDefinition> result = pluginManageService.getPluginDefinition(tenantId, pluginId, true);
    Assert.isTrue(result.isSuccess(), () -> "查询插件定义出现异常，pluginId=" + pluginId);
    PluginDefinition plugin = result.getResultObject();
    PluginGatewayDTO gateway = plugin.getGateway();
    Assert.notNull(gateway, () -> "查询不到有效网关定义，pluginId=" + pluginId);
    String baseUrl = gateway.getUrl();
    String serverUrl = StringUtils.stripEnd(plugin.getRelativePath(), "/");
    serverUrl = StringUtils.stripStart(serverUrl, "/");
    if (PluginConsts.GATEWAY_TYPE_HIGRESS.equals(gateway.getGatewayType())) {
      // Higress 类型，需要调整服务地址
      baseUrl = gateway.getHigressRuntimeUrl();
    }
    serverUrl = StringUtils.stripEnd(baseUrl, "/") + "/" + serverUrl + buildQuerys(plugin);

    McpClientBuilder builder = McpClient.builder(plugin.getPluginName());
    if (McpConsts.TRANSPORT_SSE.equals(plugin.getPluginSubType())) {
      builder.sse(serverUrl, null, buildHeaders(plugin));
    }
    else if (McpConsts.TRANSPORT_STREAMABLE.equals(plugin.getPluginSubType())) {
      builder.streamable(serverUrl, buildHeaders(plugin));
    }
    else {
      throw new BssException("未知的 MCP 服务类型: " + plugin.getPluginSubType());
    }
    McpClient client = builder.build();
    try {
      client.initialize();
    }
    catch (BssException e) {
      if (logger.isWarnEnabled()) {
        logger.warn("Failed to start plugin mcp client: plugin={}, error={}", plugin, e.getMessage());
      }
      client.close();
      throw e;
    }
    catch (Exception e) {
      if (logger.isWarnEnabled()) {
        logger.warn("Failed to start plugin mcp client: plugin={}", plugin, e);
      }
      client.close();
      throw new BssException("初始化插件 MCP 客户端失败: " + ExpUtil.getMsg(e), e);
    }
    return client;
  }

  /**
   * 构造 MCP 客户端请求头
   */
  @Nullable
  private static Headers buildHeaders(PluginDefinition plugin) {
    if (CollectionUtils.isEmpty(plugin.getAuthParams())) {
      return null;
    }
    Headers.Builder builder = new Headers.Builder();
    for (SimplePluginAuthParam param : plugin.getAuthParams()) {
      if (!"header".equals(param.getType())) {
        continue;
      }
      builder.set(param.getCode(), Objects.toString(param.getValue(), ""));
    }
    return builder.build();
  }

  /**
   * 构造 MCP 客户端 URL 参数
   */
  private static String buildQuerys(PluginDefinition plugin) {
    if (CollectionUtils.isEmpty(plugin.getAuthParams())) {
      return "";
    }
    List<String> querys = new ArrayList<>();
    for (SimplePluginAuthParam param : plugin.getAuthParams()) {
      if (!"query".equals(param.getType())) {
        continue;
      }
      querys.add(param.getCode() + "=" + Objects.toString(param.getValue(), ""));
    }
    if (CollectionUtils.isNotEmpty(querys)) {
      return "?" + StringUtils.join(querys, "&");
    }
    return "";
  }

  /**
   * 释放缓存时关闭 MCP 客户端
   */
  private void closeMcpClient(@SuppressWarnings("NullableProblems") RemovalNotification<Pair<Long, Long>, McpClient> notification) {
    McpClient client = notification.getValue();
    if (client != null) {
      Pair<Long, Long> key = notification.getKey();
      Long tenantId = key != null ? key.getLeft() : null;
      Long serverId = key != null ? key.getRight() : null;
      if (logger.isDebugEnabled()) {
        logger.debug("Releasing mcp client: tenantId={}, serverId={}, cause={}", tenantId, serverId, notification.getCause());
      }
      client.close();
    }
  }

  @Override
  public boolean isDistributedCacheEnabled() {
    return false;
  }

  @Override
  public void refreshLocalCache() {
    if (logger.isDebugEnabled()) {
      logger.debug("Refresh local: cacheName={}", getCacheName());
    }
    cache.invalidateAll();
  }

  @Override
  public void refreshLocalCache(List<String> keys) {
    if (logger.isDebugEnabled()) {
      logger.debug("Refresh local by keys: cacheName={}, keys={}", getCacheName(), keys);
    }
    if (CollectionUtils.isEmpty(keys)) {
      return;
    }
    invalidateLocalCache(cache, keys);
  }

  @Override
  public void refresh() {
    refreshLocalCache();
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
      if (logger.isDebugEnabled()) {
        logger.debug("Closing mcp clients ...");
      }
      clients.stream().parallel().forEach(McpClient::close);
    }
  }
}
