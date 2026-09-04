package com.iwhalecloud.bote.service.search.internal.engine.impl;

import com.iwhalecloud.bote.service.search.internal.engine.BaseEngineAdapter;
import com.iwhalecloud.bote.service.search.internal.engine.HttpRequest;
import com.iwhalecloud.bote.service.search.internal.model.SearchQuery;
import com.iwhalecloud.bote.service.search.internal.model.SearchResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SogouEngine extends BaseEngineAdapter {
  private static final String BASE_URL = "https://www.sogou.com/web";

  public SogouEngine() {
    super();
    this.name = "sogou";
    this.categories = List.of("general");
    this.pagingSupported = true;
    this.timeout = 10.0;
  }

  @Override
  public HttpRequest buildRequest(SearchQuery query) {
    HttpRequest request = HttpRequest.get(BASE_URL);

    Map<String, String> params = new HashMap<>();
    params.put("query", query.getQuery());
    params.put("page", String.valueOf(query.getPage()));

    request.setQueryParams(params);
    request.setFollowRedirects(false);

    request.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
    request.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
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
      results = parseHtmlWithJsoup(body, positionOffset);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Failed to parse Sogou response: {}", e.getMessage());
      }
    }

    return results;
  }

  private List<SearchResult> parseHtmlWithJsoup(String body, int positionOffset) {
    List<SearchResult> results = new ArrayList<>();

    org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse(body);

    var items = doc.select("div.rb, div.vrwrap");

    int position = positionOffset;
    for (org.jsoup.nodes.Element item : items) {
      org.jsoup.nodes.Element titleLink = item.selectFirst("h3.pt a, h3.vr-title a");
      if (titleLink == null) {
        continue;
      }

      String title = titleLink.text();
      String url = titleLink.attr("href");

      if (title.isEmpty() || url.isEmpty()) {
        continue;
      }

      if (!url.startsWith("http")) {
        url = "https://www.sogou.com" + url;
      }

      org.jsoup.nodes.Element contentElem = item.selectFirst("div.str-text, div.str_info");
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

  public boolean checkCaptcha(String redirectUrl) {
    return redirectUrl != null && redirectUrl.contains("antispider");
  }
}
