package com.iwhalecloud.bote.service.search.internal.engine.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.iwhalecloud.bote.service.search.internal.engine.BaseEngineAdapter;
import com.iwhalecloud.bote.service.search.internal.engine.HttpRequest;
import com.iwhalecloud.bote.service.search.internal.model.SearchQuery;
import com.iwhalecloud.bote.service.search.internal.model.SearchResult;
import com.iwhalecloud.bss.litchi.util.JsonUtil;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaiduEngine extends BaseEngineAdapter {
  private String category = "general";

  public BaiduEngine() {
    this.name = "baidu";
    this.categories = List.of("general", "images", "it");
    this.pagingSupported = true;
    this.timeout = 10.0;
  }

  @Override
  public HttpRequest buildRequest(SearchQuery query) {
    Map<String, String> params = new HashMap<>();
    String endpoint;

    switch (category) {
      case "images":
        endpoint = "https://image.baidu.com/search/acjson";
        params.put("word", query.getQuery());
        params.put("rn", "10");
        params.put("pn", String.valueOf((query.getPage() - 1) * 10));
        params.put("tn", "resultjson_com");
        break;
      case "it":
        endpoint = "https://kaifa.baidu.com/rest/v1/search";
        params.put("wd", query.getQuery());
        params.put("pageSize", "10");
        params.put("pageNum", String.valueOf(query.getPage()));
        params.put("position", "0");
        break;
      default:
        endpoint = "https://www.baidu.com/s";
        params.put("wd", query.getQuery());
        params.put("rn", "10");
        params.put("pn", String.valueOf((query.getPage() - 1) * 10));
        params.put("tn", "json");
    }

    HttpRequest request = HttpRequest.get(endpoint);
    request.setQueryParams(params);
    request.setFollowRedirects(false);

    request.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
    request.header("Accept", "application/json, text/html");
    request.header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");

    return request;
  }

  @Override
  public List<SearchResult> parseResponse(String body, int positionOffset) {
    List<SearchResult> results = new ArrayList<>();

    if (body == null || body.isEmpty()) {
      return results;
    }

    try {
      switch (category) {
        case "images":
          String fixedBody = body.replace("\\/", "/").replace("\\'", "'");
          return parseImages(fixedBody);
        case "it":
          return parseIt(body);
        default:
          return parseGeneral(body);
      }
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Failed to parse Baidu response: {}", e.getMessage());
      }
      return results;
    }
  }

  private List<SearchResult> parseGeneral(String body) throws Exception {
    List<SearchResult> results = new ArrayList<>();

    JsonNode root = JsonUtil.getObjectMapper().readTree(body);
    JsonNode feed = root.path("feed");
    JsonNode entryList = feed.path("entry");

    if (entryList.isMissingNode() || !entryList.isArray()) {
      return results;
    }

    for (JsonNode entry : entryList) {
      String title = entry.path("title").asText();
      String url = entry.path("url").asText();

      if (title.isEmpty() || url.isEmpty()) {
        continue;
      }

      String content = unescapeHtml(entry.path("abs").asText(""));

      SearchResult result = new SearchResult();
      result.setTitle(unescapeHtml(title));
      result.setUrl(url);
      result.setContent(content);
      result.setEngine(name);

      if (entry.has("time")) {
        long timestamp = entry.path("time").asLong(0);
        if (timestamp > 0) {
          result.setPublishedDate(Instant.ofEpochSecond(timestamp));
        }
      }

      results.add(result);
    }

    return results;
  }

  private List<SearchResult> parseImages(String body) throws Exception {
    List<SearchResult> results = new ArrayList<>();

    JsonNode root = JsonUtil.getObjectMapper().readTree(body);
    JsonNode data = root.path("data");

    if (!data.isArray()) {
      return results;
    }

    for (JsonNode item : data) {
      if (item.isNull()) {
        continue;
      }

      JsonNode replaceUrl = item.path("replaceUrl");
      String fromUrl = replaceUrl.path(0).path("FromURL").asText("");

      if (fromUrl.isEmpty()) {
        continue;
      }

      SearchResult result = new SearchResult();
      result.setTitle(htmlToText(item.path("fromPageTitle").asText("")));
      result.setUrl(fromUrl);
      result.setThumbnail(item.path("thumbURL").asText());
      result.setTemplate("images.html");
      result.setEngine(name);

      String width = item.path("width").asText();
      String height = item.path("height").asText();
      if (!width.isEmpty() && !height.isEmpty()) {
        result.setContent(width + " x " + height);
      }

      String type = item.path("type").asText();
      if (!type.isEmpty()) {
        result.setAuthor(type);
      }

      handleDate(item, result);

      results.add(result);
    }

    return results;
  }

  private static void handleDate(JsonNode item, SearchResult result) {
    String dateStr = item.path("bdImgnewsDate").asText();
    if (!dateStr.isEmpty()) {
      try {
        result.setPublishedDate(Instant.parse(dateStr + "T00:00:00Z"));
      } catch (Exception e) {
        if (logger.isDebugEnabled()) {
          logger.debug("Failed to parse date: {}", e.getMessage());
        }
      }
    }
  }

  private List<SearchResult> parseIt(String body) throws Exception {
    List<SearchResult> results = new ArrayList<>();

    JsonNode root = JsonUtil.getObjectMapper().readTree(body);
    JsonNode documents = root.path("data").path("documents").path("data");

    if (!documents.isArray()) {
      return results;
    }

    for (JsonNode entry : documents) {
      JsonNode techDoc = entry.path("techDocDigest");

      SearchResult result = new SearchResult();
      result.setTitle(techDoc.path("title").asText());
      result.setUrl(techDoc.path("url").asText());
      result.setContent(techDoc.path("summary").asText());
      result.setEngine(name);

      results.add(result);
    }

    return results;
  }

  private String unescapeHtml(String text) {
    if (text == null || text.isEmpty()) {
      return "";
    }
    return text.replace("&amp;", "&")
      .replace("&#39;", "'")
      .replace("&quot;", "\"")
      .replace("&lt;", "<")
      .replace("&gt;", ">");
  }

  private String htmlToText(String html) {
    if (html == null || html.isEmpty()) {
      return "";
    }
    return html.replaceAll("<[^>]+>", "");
  }

  public void setCategory(String category) {
    this.category = category;
  }
}
