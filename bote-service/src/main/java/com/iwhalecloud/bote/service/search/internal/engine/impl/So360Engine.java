package com.iwhalecloud.bote.service.search.internal.engine.impl;

import com.iwhalecloud.bote.service.search.internal.engine.BaseEngineAdapter;
import com.iwhalecloud.bote.service.search.internal.engine.HttpRequest;
import com.iwhalecloud.bote.service.search.internal.engine.HttpResponse;
import com.iwhalecloud.bote.service.search.internal.model.SearchQuery;
import com.iwhalecloud.bote.service.search.internal.model.SearchResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class So360Engine extends BaseEngineAdapter {
  private static final String BASE_URL = "https://www.so.com/s";
  private static final Map<String, String> cookieCache = new ConcurrentHashMap<>();

  public So360Engine() {
    super();
    this.name = "so360";
    this.categories = List.of("general");
    this.pagingSupported = true;
    this.timeout = 10.0;
  }

  @Override
  public HttpRequest buildRequest(SearchQuery query) {
    HttpRequest request = HttpRequest.get(BASE_URL);

    Map<String, String> params = new HashMap<>();
    params.put("pn", String.valueOf(query.getPage()));
    params.put("q", query.getQuery());

    request.setQueryParams(params);

    request.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
    request.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
    request.header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");

    String cookie = getCookie();
    if (cookie != null && !cookie.isEmpty()) {
      request.setCookies(Map.of("qc", cookie));
    }

    return request;
  }

  private synchronized String getCookie() {
    String cached = cookieCache.get("cookie");
    if (cached != null) {
      return cached;
    }

    try {
      HttpRequest cookieReq = HttpRequest.get("https://www.so.com/");
      cookieReq.setFollowRedirects(false);
      cookieReq.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

      if (httpClient != null) {
        HttpResponse resp = httpClient.execute(cookieReq);
        if (resp.getHeaders() != null) {
          String setCookie = resp.getHeaders().get("Set-Cookie");
          if (setCookie != null) {
            String qc = setCookie.split(";")[0].replace("qc=", "");
            cookieCache.put("cookie", qc);
            return qc;
          }
        }
      }
    } catch (Exception e) {
      if (logger.isWarnEnabled()) {
        logger.warn("Failed to get 360 cookie: {}", e.getMessage());
      }
    }

    return null;
  }

  @Override
  public List<SearchResult> parseResponse(String body, int positionOffset) {
    List<SearchResult> results = new ArrayList<>();

    if (body == null || body.isEmpty()) {
      return results;
    }

    try {
      results = parseHtmlWithJsoup(body, positionOffset);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Failed to parse 360 response: {}", e.getMessage());
      }
    }

    return results;
  }

  private List<SearchResult> parseHtmlWithJsoup(String body, int positionOffset) {
    List<SearchResult> results = new ArrayList<>();

    org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse(body);

    var items = doc.select("li.res-list, div.result");

    int position = positionOffset;
    for (org.jsoup.nodes.Element item : items) {
      org.jsoup.nodes.Element titleLink = item.selectFirst("h3 a");
      if (titleLink == null) {
        continue;
      }

      String title = titleLink.text();
      String url = titleLink.attr("href");

      if (title.isEmpty() || url.isEmpty()) {
        continue;
      }

      if (!url.startsWith("http")) {
        continue;
      }

      org.jsoup.nodes.Element contentElem = item.selectFirst("p.description, div.res-desc");
      String content = contentElem != null ? contentElem.text() : "";

      SearchResult result = new SearchResult();
      result.setTitle(title);
      result.setUrl(url);
      result.setContent(content.length() > 200 ? content.substring(0, 200) : content);
      result.setEngine(name);

      result.addPosition(++position);
      results.add(result);
    }

    return results;
  }
}
