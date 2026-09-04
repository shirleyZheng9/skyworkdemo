package com.iwhalecloud.bote.service.search.internal.engine.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.iwhalecloud.bote.service.search.internal.engine.BaseEngineAdapter;
import com.iwhalecloud.bote.service.search.internal.engine.HttpRequest;
import com.iwhalecloud.bote.service.search.internal.model.SearchQuery;
import com.iwhalecloud.bote.service.search.internal.model.SearchResult;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import org.springframework.lang.Nullable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BilibiliEngine extends BaseEngineAdapter {
  private static final String BASE_URL = "https://api.bilibili.com/x/web-interface/search/type";

  public BilibiliEngine() {
    super();
    this.name = "bilibili";
    this.categories = List.of("videos");
    this.pagingSupported = true;
    this.timeout = 10.0;
  }

  @Override
  public HttpRequest buildRequest(SearchQuery query) {
    HttpRequest request = HttpRequest.get(BASE_URL);

    Map<String, String> params = new HashMap<>();
    params.put("__refresh__", "true");
    params.put("page", String.valueOf(query.getPage()));
    params.put("page_size", "20");
    params.put("single_column", "0");
    params.put("keyword", query.getQuery());
    params.put("search_type", "video");

    request.setQueryParams(params);

    request.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
    request.header("Referer", "https://www.bilibili.com");
    request.header("Accept", "application/json");
    request.header("Accept-Language", "zh-CN,zh;q=0.9");

    Map<String, String> cookies = new HashMap<>();
    cookies.put("innersign", "0");
    cookies.put("b_ut", "7");
    cookies.put("home_feed_column", "4");
    request.setCookies(cookies);

    return request;
  }

  @Override
  public List<SearchResult> parseResponse(String body, int positionOffset) {
    List<SearchResult> results = new ArrayList<>();

    if (body == null || body.isEmpty()) {
      return results;
    }

    try {
      JsonNode root = JsonUtil.getObjectMapper().readTree(body);
      JsonNode resultList = root.path("data").path("result");

      if (!resultList.isArray()) {
        return results;
      }

      int position = positionOffset;
      for (JsonNode item : resultList) {
        position++;

        SearchResult result = getSearchResult(item, position);
        if (result == null) {
          continue;
        }
        results.add(result);
      }

    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Failed to parse Bilibili response: {}", e.getMessage());
      }
    }

    return results;
  }

  private @Nullable SearchResult getSearchResult(JsonNode item, int position) {
    String title = htmlToText(item.path("title").asText(""));
    String url = item.path("arcurl").asText("");
    String thumbnail = item.path("pic").asText("");
    String description = item.path("description").asText("");
    String author = item.path("author").asText("");
    String videoId = item.path("aid").asText("");
    long pubdate = item.path("pubdate").asLong(0);
    String duration = item.path("duration").asText("");

    if (title.isEmpty() || url.isEmpty()) {
      return null;
    }

    SearchResult result = new SearchResult();
    result.setTitle(title);
    result.setUrl(url);
    result.setContent(description);
    result.setThumbnail(thumbnail);
    result.setAuthor(author);
    result.setTemplate("videos.html");
    result.setEngine(name);

    if (pubdate > 0) {
      result.setPublishedDate(Instant.ofEpochSecond(pubdate));
    }

    if (!duration.isEmpty()) {
      result.setLength(duration);
    }

    if (!videoId.isEmpty()) {
      result.setIframeSrc("https://player.bilibili.com/player.html?aid=" +
        videoId + "&high_quality=1&autoplay=false&danmaku=0");
    }

    result.addPosition(position);
    return result;
  }

  private String htmlToText(String html) {
    if (html == null || html.isEmpty()) {
      return "";
    }
    return html.replaceAll("<[^>]+>", " ").trim();
  }

  @Override
  protected int getResultsPerPage() {
    return 20;
  }
}
