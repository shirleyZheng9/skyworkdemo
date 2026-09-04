package com.iwhalecloud.bote.service.search.internal.engine;

import com.iwhalecloud.bote.common.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpClientService {
  private static final Logger logger = LoggerFactory.getLogger(HttpClientService.class);

  private final RestTemplate restTemplate;

  public HttpClientService() {
    this.restTemplate = HttpUtil.getRestTemplate();
  }

  public HttpResponse execute(HttpRequest request) {
    long startTime = System.currentTimeMillis();

    try {
      String uri = buildUri(request);

      HttpHeaders headers = new HttpHeaders();
      headers.setAccept(List.of(MediaType.APPLICATION_JSON, MediaType.TEXT_HTML));

      for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
        headers.add(entry.getKey(), entry.getValue());
      }

      if (!request.getCookies().isEmpty()) {
        StringBuilder cookieStr = new StringBuilder();
        for (Map.Entry<String, String> entry : request.getCookies().entrySet()) {
          if (!cookieStr.isEmpty()) {
            cookieStr.append("; ");
          }
          cookieStr.append(entry.getKey()).append("=").append(entry.getValue());
        }
        headers.add("Cookie", cookieStr.toString());
      }

      HttpEntity<Void> entity = new HttpEntity<>(headers);

      ResponseEntity<String> response = restTemplate.exchange(
        uri,
        HttpMethod.GET,
        entity,
        String.class
      );

      HttpResponse result = new HttpResponse();
      result.setResponseTime(System.currentTimeMillis() - startTime);

      result.setStatusCode(response.getStatusCode().value());
      result.setBody(response.getBody());

      if (response.getStatusCode().is3xxRedirection()) {
        String location = response.getHeaders().getFirst("Location");
        result.setRedirectUrl(location);
      }

      result.setHeaders(extractHeaders(response.getHeaders()));

      return result;

    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("HTTP request failed: {}", e.getMessage());
      }
      HttpResponse errorResponse = new HttpResponse();
      errorResponse.setStatusCode(0);
      errorResponse.setBody(null);
      errorResponse.setResponseTime(System.currentTimeMillis() - startTime);
      return errorResponse;
    }
  }
  private String buildUri(HttpRequest request) {
    StringBuilder uri = new StringBuilder(request.getUrl());

    if (!request.getQueryParams().isEmpty()) {
      boolean first = !request.getUrl().contains("?");
      for (Map.Entry<String, String> entry : request.getQueryParams().entrySet()) {
        uri.append(first ? "?" : "&");
        uri.append(entry.getKey()).append("=").append(entry.getValue());
        first = false;
      }
    }

    return uri.toString();
  }

  private Map<String, String> extractHeaders(org.springframework.http.HttpHeaders httpHeaders) {
    Map<String, String> headers = new HashMap<>();
    httpHeaders.forEach((key, value) -> {
      if (!value.isEmpty()) {
        headers.put(key, value.get(0));
      }
    });
    return headers;
  }
}
