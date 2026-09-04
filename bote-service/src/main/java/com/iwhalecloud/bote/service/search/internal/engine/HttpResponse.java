package com.iwhalecloud.bote.service.search.internal.engine;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@ToString
public class HttpResponse {
  private int statusCode;
  private String body;
  private Map<String, String> headers;
  private String redirectUrl;
  private long responseTime;
}
