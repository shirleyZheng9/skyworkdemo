package com.iwhalecloud.bote.service.search.internal.engine;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ToString
public class HttpRequest {
  private String url;
  private String method = "GET";
  private Map<String, String> headers = new HashMap<>();
  private Map<String, String> cookies = new HashMap<>();
  private Map<String, String> queryParams = new HashMap<>();
  private String body;
  private int timeout = 10;
  private boolean followRedirects = true;

  /**
   * Creates a new GET request for the given URL.
   *
   * @param url the URL
   * @return a new HttpRequest
   */
  public static HttpRequest get(String url) {
    HttpRequest request = new HttpRequest();
    request.setUrl(url);
    return request;
  }

  public HttpRequest header(String key, String value) {
    this.headers.put(key, value);
    return this;
  }
}
