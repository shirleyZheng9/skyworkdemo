package com.iwhalecloud.bote.agent.tools;

import com.iwhalecloud.bote.agent.annotation.Tool;
import com.iwhalecloud.bote.agent.annotation.ToolParam;
import com.iwhalecloud.bote.agent.tool.exception.ToolExecutionException;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.dto.search.WebPageInfo;
import com.iwhalecloud.bote.service.search.IWebSearchService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 联网搜索工具。
 * <p>
 * 通过统一的 {@link IWebSearchService} 接口调用底层适配器（支持 Bocha、Tavily、Brave 等多种搜索引擎）。
 * 返回结构化结果（query、provider、results）供模型归纳与引用。
 * </p>
 *
 * @author wangtingyun
 */
public final class WebSearchTools {
  private static final int DEFAULT_COUNT = 5;
  private static final int MAX_COUNT = 10;
  private static final int CACHE_TTL_MS = 10 * 60 * 1000;
  private static final int CACHE_MAX_ENTRIES = 50;
  private static final Map<String, CacheEntry> SEARCH_CACHE = new ConcurrentHashMap<>();

  private WebSearchTools() {
  }

  @Tool(
    name = "web_search",
    description = "Search the web for current information. Use this when you need up-to-date facts, news, or documentation. Returns titles, URLs, and snippets. Parameters: query (required), optional count (1-10, default 5)."
  )
  public static String webSearch(@ToolParam(description = "Search query string") String query,
                                 @ToolParam(description = "Number of results to return (1-10), default 5") @Nullable Integer count) {
    String trimmedQuery = StringUtils.trimToNull(query);
    Assert.notNull(trimmedQuery, "Error: query is required");
    int n = normalizeCount(count);
    
    // 使用缓存避免重复请求
    String cacheKey = "search:" + trimmedQuery.toLowerCase() + ":" + n;
    CacheEntry cached = SEARCH_CACHE.get(cacheKey);
    if (cached != null && System.currentTimeMillis() < cached.expiresAt) {
      return cached.payload;
    }
    
    // 调用统一的搜索服务
    String result = runSearch(trimmedQuery);
    
    // 更新缓存
    evictCacheIfNeeded();
    SEARCH_CACHE.put(cacheKey, new CacheEntry(result, System.currentTimeMillis() + CACHE_TTL_MS));
    return result;
  }

  /**
   * 执行搜索（通过 IWebSearchService）
   * @param query 查询词
   * @return JSON 格式结果
   */
  private static String runSearch(String query) {
    IWebSearchService webSearchService = SpringUtil.getBean(IWebSearchService.class);
    ResultVO<List<WebPageInfo>> searchResult = webSearchService.run(query);
    if (!searchResult.isSuccess()) {
      String msg = StringUtils.isNotBlank(searchResult.getResultMsg()) ? searchResult.getResultMsg() : "web_search failed";
      throw new ToolExecutionException("Error: " + msg);
    }
    String searchProvider = SystemParameter.WEB_SEARCH_STRATEGY.getValueFromDb();
    if (searchResult.getResultObject() == null || searchResult.getResultObject().isEmpty()) {
      // 返回空结果结构
      return buildResult(query, searchProvider, new ArrayList<>());
    }
    return buildResultFromWebPageInfo(query, searchResult.getResultObject(), searchProvider);
  }

  private static int normalizeCount(@Nullable Integer count) {
    if (count == null) {
      return DEFAULT_COUNT;
    }
    int c = count;
    if (c < 1) {
      c = 1;
    }
    if (c > MAX_COUNT) {
      c = MAX_COUNT;
    }
    return c;
  }

  private static String buildResult(String query, String provider, List<Map<String, Object>> results) {
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("query", query);
    out.put("provider", provider);
    out.put("count", results.size());
    out.put("results", results);
    return JsonUtil.toJsonString(out);
  }

  private static String buildResultFromWebPageInfo(String query, List<WebPageInfo> pages, String provider) {
    List<Map<String, Object>> results = new ArrayList<>();
    for (WebPageInfo p : pages) {
      Map<String, Object> row = new LinkedHashMap<>();
      row.put("title", p.getName());
      row.put("url", p.getUrl());
      row.put("description", StringUtils.isNotBlank(p.getSnippet()) ? p.getSnippet() : p.getSummary());
      row.put("siteName", p.getSiteName());
      results.add(row);
    }
    return buildResult(query, provider, results);
  }

  private static void evictCacheIfNeeded() {
    if (SEARCH_CACHE.size() < CACHE_MAX_ENTRIES) {
      return;
    }
    long now = System.currentTimeMillis();
    SEARCH_CACHE.entrySet().removeIf(e -> e.getValue().expiresAt <= now);
    if (SEARCH_CACHE.size() >= CACHE_MAX_ENTRIES) {
      SEARCH_CACHE.keySet().stream().limit(10).toList().forEach(SEARCH_CACHE::remove);
    }
  }

  private record CacheEntry(String payload, long expiresAt) {
  }
}
