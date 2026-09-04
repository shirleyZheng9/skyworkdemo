package com.iwhalecloud.bote.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.thread.ThreadPools.ThreadPoolShutdownHook;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.dto.mcp.SimpleMcpServiceDTO;
import com.iwhalecloud.bote.llm.client.dto.HeaderItem;
import com.iwhalecloud.bote.mapper.mcp.McpServerManageMapper;
import com.iwhalecloud.bote.mcp.client.McpClient;
import com.iwhalecloud.bote.mcp.client.McpClientBuilder;
import com.iwhalecloud.bote.mcp.consts.McpConsts;
import com.iwhalecloud.bote.mcp.dto.StdioServerParameters;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * MCP 客户端缓存
 *
 * <p>使用 DependsOn 注解以确保先关闭 MCP 客户端再关闭线程池（stdio 模式的客户端使用了线程池，先关闭线程池会产生报错）</p>
 *
 * @author bianjp
 * @since 2024-08-01
 */
@Component
@DependsOn(ThreadPoolShutdownHook.BEAN_NAME)
@RequiredArgsConstructor
@SuppressFBWarnings("CRLF_INJECTION_LOGS")
@SuppressWarnings("PMD.GuardLogStatement")
public class McpClientCache implements TenantCacheMarker, DisposableBean {
  private static final Logger logger = LoggerFactory.getLogger(McpClientCache.class);
  /** 缓存实例 */
  private final LoadingCache<@NonNull Pair<Long, Long>, @NonNull McpClient> cache = CacheBuilder.newBuilder()
    .maximumSize(100)
    .expireAfterAccess(Duration.ofHours(24))
    .removalListener(this::closeMcpClient)
    .build(CacheLoader.from(this::buildMcpClient));
  /** 服务状态缓存（是否已发布） */
  private final LoadingCache<@NonNull Pair<Long, Long>, @NonNull Boolean> serverStatusCache = CacheBuilder.newBuilder()
    .maximumSize(1000)
    .expireAfterAccess(Duration.ofHours(24))
    .build(CacheLoader.from(this::queryServerStatus));

  private final McpServerManageMapper mcpServerManageMapper;

  @Override
  public String getCacheName() {
    return CacheConsts.CACHE_NAME_MCP_CLIENT;
  }

  /**
   * 获取 MCP 客户端
   *
   * @param tenantId 租户 ID
   * @param serverId MCP 服务器 ID
   * @param requirePublished 是否要求服务已发布。管理、测试 MCP 服务时不要求，智能体、工作流中使用时要求
   * @return MCP 客户端
   */
  @SuppressWarnings("PMD.PreserveStackTrace")
  public McpClient getMcpClient(Long tenantId, Long serverId, boolean requirePublished) {
    Assert.notNull(tenantId, "tenantId 不能为空");
    Assert.notNull(serverId, "serverId 不能为空");
    Pair<Long, Long> key = Pair.of(tenantId, serverId);
    // 检查服务状态
    if (requirePublished) {
      try {
        Boolean isPublished = serverStatusCache.get(Pair.of(tenantId, serverId));
        if (!isPublished) {
          throw new BssException("MCP 服务未发布，请发布后使用: id=" + serverId);
        }
      }
      catch (ExecutionException e) {
        if (e.getCause() instanceof BssException) {
          throw (BssException) e.getCause();
        }
        throw new BssException("查询 MCP 服务状态失败: " + e.getMessage(), e.getCause());
      }
    }

    McpClient client = getClientFromCache(key);

    // 检查客户端状态，实现自动重连
    try {
      client.waitInitialized();
    }
    catch (Exception e) {
      // 自动重连失败时，关闭客户端，重新创建一个实例
      logger.warn("Failed to wait mcp client initialized, try recreating: tenantId={}, serverId={}", tenantId, serverId, e);
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
      throw new BssException("获取 MCP 客户端失败: " + e.getMessage(), e.getCause());
    }
  }

  /**
   * 查询 MCP 服务是否生效
   */
  private boolean queryServerStatus(Pair<Long, Long> key) {
    Long tenantId = key.getLeft();
    Long serverId = key.getRight();
    String serverEffect = mcpServerManageMapper.selectServerEffectById(tenantId, serverId);
    if (serverEffect == null) {
      throw new BssException("MCP 服务不存在: id=" + serverId);
    }
    return "1".equals(serverEffect);
  }

  /**
   * 构造 MCP 客户端
   */
  private McpClient buildMcpClient(Pair<Long, Long> key) {
    Long tenantId = key.getLeft();
    Long serverId = key.getRight();
    SimpleMcpServiceDTO server = mcpServerManageMapper.selectSimpleMcpServerById(tenantId, serverId);
    Assert.notNull(server, "MCP 不存在或未启用");
    server.parseJsonConfig();
    return buildMcpClient(server);
  }

  /**
   * 构造 MCP 客户端
   */
  public static McpClient buildMcpClient(SimpleMcpServiceDTO server) {
    McpClientBuilder builder = McpClient.builder(server.getServerName());
    if (McpConsts.TRANSPORT_STDIO.equals(server.getServerType())) {
      StdioServerParameters serverParameters = buildStdioServerParameters(server);
      builder.stdio(serverParameters);
    }
    else if (McpConsts.TRANSPORT_SSE.equals(server.getServerType())) {
      Assert.hasLength(server.getServerUrl(), "MCP 服务的 URL 不能为空");
      builder.sse(server.getServerUrl(), server.getServerEndpoint(), buildHeaders(server));
    }
    else if (McpConsts.TRANSPORT_STREAMABLE.equals(server.getServerType())) {
      Assert.hasLength(server.getServerUrl(), "MCP 服务的 URL 不能为空");
      builder.streamable(server.getServerUrl(), buildHeaders(server));
    }
    else {
      throw new BssException("未知的 MCP 服务类型: " + server.getServerType());
    }
    McpClient client = builder.build();
    try {
      client.initialize();
    }
    catch (BssException e) {
      logger.warn("Failed to start mcp client: server={}, error={}", server, e.getMessage());
      client.close();
      throw e;
    }
    catch (Exception e) {
      logger.warn("Failed to start mcp client: server={}", server, e);
      client.close();
      throw new BssException("初始化 MCP 客户端失败: " + ExpUtil.getMsg(e), e);
    }
    return client;
  }

  /**
   * 构造 MCP 客户端请求头
   */
  @Nullable
  private static Headers buildHeaders(SimpleMcpServiceDTO server) {
    String token = server.getServerToken();
    List<HeaderItem> headers = server.getHeaders();
    if (StringUtils.isEmpty(token) && CollectionUtils.isEmpty(headers)) {
      return null;
    }
    Headers.Builder builder = new Headers.Builder();
    // 鉴权请求头
    if (StringUtils.isNotEmpty(token)) {
      // 允许用户指定鉴权模式，比如 Bearer, Basic, Token。难以穷举所有鉴权模式，因此使用空格检测
      // 未指定时默认为 Bearer
      String value = StringUtils.containsWhitespace(token) ? token.trim() : "Bearer " + token;
      builder.set(HttpHeaders.AUTHORIZATION, value);
    }
    // 自定义请求头
    if (CollectionUtils.isNotEmpty(headers)) {
      for (HeaderItem header : headers) {
        String name = StringUtils.trimToNull(header.getName());
        String value = StringUtils.trimToNull(header.getValue());
        if (name != null && value != null) {
          builder.set(name, value);
        }
      }
    }
    return builder.build();
  }

  /**
   * 构造 stdio 服务器参数
   */
  private static StdioServerParameters buildStdioServerParameters(SimpleMcpServiceDTO server) {
    Assert.hasLength(server.getServerCommand(), "MCP 服务的命令不能为空");
    // 解析命令和参数
    List<String> command = new ArrayList<>();
    command.add(server.getServerCommand());
    if (StringUtils.isNotEmpty(server.getServerArgs())) {
      List<String> args = JsonUtil.parseJsonRequired(server.getServerArgs(), new TypeReference<List<String>>() {
      });
      args.stream().map(StringUtils::trim).filter(StringUtils::isNotEmpty).filter(a -> !a.startsWith("#")).forEach(command::add);
    }

    // 解析环境变量
    Map<String, String> env = null;
    if (StringUtils.isNotEmpty(server.getServerEnv())) {
      env = new HashMap<>();
      Map<String, String> variables = JsonUtil.parseJsonRequired(server.getServerEnv(), new TypeReference<Map<String, String>>() {
      });
      for (Entry<String, String> entry : variables.entrySet()) {
        String key = StringUtils.trim(entry.getKey());
        String value = StringUtils.trim(entry.getValue());
        if (StringUtils.isNotEmpty(key) && !key.startsWith("#")) {
          env.put(key, value);
        }
      }
    }
    return new StdioServerParameters(command, env);
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
      logger.debug("Releasing mcp client: tenantId={}, serverId={}, cause={}", tenantId, serverId, notification.getCause());
      client.close();
    }
  }

  @Override
  public boolean isDistributedCacheEnabled() {
    return false;
  }

  @Override
  public void refreshLocalCache() {
    logger.debug("Refresh local: cacheName={}", getCacheName());
    cache.invalidateAll();
    serverStatusCache.invalidateAll();
  }

  @Override
  public void refreshLocalCache(List<String> keys) {
    logger.debug("Refresh local by keys: cacheName={}, keys={}", getCacheName(), keys);
    if (CollectionUtils.isEmpty(keys)) {
      return;
    }
    invalidateLocalCache(cache, keys);
    invalidateLocalCache(serverStatusCache, keys);
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
      logger.debug("Closing mcp clients ...");
      clients.stream().parallel().forEach(McpClient::close);
    }
  }
}
