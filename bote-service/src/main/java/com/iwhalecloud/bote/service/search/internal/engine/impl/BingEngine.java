package com.iwhalecloud.bote.service.search.internal.engine.impl;

import com.iwhalecloud.bote.service.search.internal.engine.BaseEngineAdapter;
import com.iwhalecloud.bote.service.search.internal.engine.HttpRequest;
import com.iwhalecloud.bote.service.search.internal.model.SearchQuery;
import com.iwhalecloud.bote.service.search.internal.model.SearchResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Bing search engine adapter.
 */
public class BingEngine extends BaseEngineAdapter {

  private static final Pattern SPACE_PATTERN = Pattern.compile("\\s+");

  public BingEngine() {
    super();
    this.name = "bing";
    this.categories = List.of("general");
    this.pagingSupported = true;
    this.timeout = 10.0;
    this.weight = 1.2;
  }

  @Override
  public HttpRequest buildRequest(SearchQuery query) {
    try {
      String url = buildBingUrl(query.getQuery(), getResultsPerPage(), (query.getPage() - 1) * getResultsPerPage());
      HttpRequest request = HttpRequest.get(url);

      // Set headers similar to the test class
      request.header("User-Agent",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36");
      request.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
      request.header("Accept-Language", "en-US,en;q=0.5");

      return request;
    } catch (Exception e) {
      throw new RuntimeException("Failed to build Bing request", e);
    }
  }

  @Override
  public List<SearchResult> parseResponse(String body, int positionOffset) {
    List<SearchResult> results = new ArrayList<>();

    if (body == null || body.isEmpty()) {
      return results;
    }

    try {
      List<SearchResult> bingResults = parseBingResponse(body);

      for (int i = 0; i < bingResults.size(); i++) {
        SearchResult bingResult = bingResults.get(i);
        SearchResult result = new SearchResult(
          bingResult.getTitle(),
          bingResult.getUrl(),
          bingResult.getContent(),
          name
        );

        // Set position
        result.addPosition(positionOffset + i + 1);

        results.add(result);
      }
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Failed to parse Bing response: {}", e.getMessage());
      }
    }

    return results;
  }

  private String buildBingUrl(String query, int count, int offset) {
    String baseUrl = "https://cn.bing.com/search";
    Map<String, String> params = new LinkedHashMap<>();
    params.put("q", query);
    params.put("form", "QBRE");
    params.put("sp", "-1");
    params.put("lq", "0");
    params.put("pq", query);
    params.put("sc", "3-14");
    params.put("qs", "n");
    params.put("first", String.valueOf(offset + 1));
    params.put("cnt", String.valueOf(count));
    params.put("adlt", "off");
    params.put("setmkt", "zh-CN");
    params.put("setlang", "zh-hans");

    StringBuilder sb = new StringBuilder(baseUrl).append("?");
    boolean first = true;
    for (Map.Entry<String, String> entry : params.entrySet()) {
      if (!first) {
        sb.append("&");
      }
      sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
      sb.append("=");
      sb.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
      first = false;
    }
    return sb.toString();
  }

  private List<SearchResult> parseBingResponse(String responseText) {
    List<SearchResult> results = new ArrayList<>();
    Document doc = Jsoup.parse(responseText);
    List<Element> items = doc.select("li.b_algo");

    for (Element item : items) {
      Element link = item.selectFirst("h2 a[href]");
      if (link == null) {
        continue;
      }

      String url = cleanupText(link.attr("href"));
      String title = cleanupText(link.text());

      String summary = "";
      Element summaryElem = item.selectFirst("p");
      if (summaryElem != null) {
        summary = cleanupText(summaryElem.text());
      }

      if (url.startsWith("http") && !title.isEmpty()) {
        SearchResult row = new SearchResult();
        row.setUrl(url);
        row.setTitle(title);
        row.setContent(summary);
        results.add(row);
      }
    }

    return results;
  }

  private String cleanupText(String input) {
    if (input == null) {
      return "";
    }
    String decoded = decodeHtmlEntities(input);
    decoded = decoded.replace('·', ' ');
    return SPACE_PATTERN.matcher(decoded).replaceAll(" ").trim();
  }

  private String decodeHtmlEntities(String input) {
    String text = input;
    text = text.replace("&nbsp;", " ");
    text = text.replace("&ensp;", " ");
    text = text.replace("&emsp;", " ");
    text = text.replace("&thinsp;", " ");
    text = text.replace("&amp;", "&");
    text = text.replace("&lt;", "<");
    text = text.replace("&gt;", ">");
    text = text.replace("&quot;", "\"");
    text = text.replace("&#39;", "'");

    Pattern decPattern = Pattern.compile("&#(\\d+);");
    Matcher decMatcher = decPattern.matcher(text);
    StringBuilder sb = new StringBuilder();
    while (decMatcher.find()) {
      String replacement = decMatcher.group(0);
      try {
        int codePoint = Integer.parseInt(decMatcher.group(1));
        replacement = new String(Character.toChars(codePoint));
      } catch (Exception ignored) {
        // ignored
      }
      decMatcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
    }
    decMatcher.appendTail(sb);

    Pattern hexPattern = Pattern.compile("&#x([0-9a-fA-F]+);");
    Matcher hexMatcher = hexPattern.matcher(sb.toString());
    StringBuilder out = new StringBuilder();
    while (hexMatcher.find()) {
      String replacement = hexMatcher.group(0);
      try {
        int codePoint = Integer.parseInt(hexMatcher.group(1), 16);
        replacement = new String(Character.toChars(codePoint));
      } catch (Exception ignored) {
        // ignored
      }
      hexMatcher.appendReplacement(out, Matcher.quoteReplacement(replacement));
    }
    hexMatcher.appendTail(out);
    return out.toString();
  }
}
