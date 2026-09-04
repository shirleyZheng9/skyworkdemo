package com.iwhalecloud.bote.llm.client.adapter;

import com.iwhalecloud.bote.llm.client.ModelClient;
import com.iwhalecloud.bote.llm.client.config.AbstractModelProperties;
import com.iwhalecloud.bote.llm.client.dto.HeaderItem;
import com.iwhalecloud.bote.llm.client.ext.ModelRequestHeaderResolver;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 大模型客户端抽象类
 *
 * @author bianjp
 * @since 2024-12-23
 */
public abstract class AbstractModelClient<T extends AbstractModelProperties> implements ModelClient {
  private static final ModelRequestHeaderResolver headerResolver = SpringUtil.getBeanOptional(ModelRequestHeaderResolver.class);

  /** 客户端配置 */
  protected final T properties;
  /** 文本嵌入接口地址 */
  protected final HttpUrl apiUrl;
  /** 请求头 */
  private final Headers headers;

  protected AbstractModelClient(T properties) {
    Assert.hasLength(properties.getUrl(), "接口地址不能为空");
    Assert.hasLength(properties.getModel(), "模型不能为空");
    this.properties = properties;
    this.apiUrl = HttpUrl.parse(properties.getUrl());
    this.headers = buildHeaders(properties);
  }

  /**
   * 构造请求头
   */
  private static <T extends AbstractModelProperties> Headers buildHeaders(T properties) {
    Headers.Builder builder = new Headers.Builder();
    if (StringUtils.isNotEmpty(properties.getApiKey())) {
      builder.add("Authorization", "Bearer " + properties.getApiKey());
    }
    // 调用 gpt-proxy 时自动加上 X-CHANNEL 请求头
    boolean isGptProxy = properties.getUrl().startsWith("https://lab.iwhalecloud.com") || properties.getUrl().startsWith("https://dev.iwhalecloud.com");
    if (isGptProxy && builder.get("X-CHANNEL") == null) {
      builder.add("X-CHANNEL", "BOTE-AGENT");
    }
    return builder.build();
  }

  /**
   * 解析请求头
   */
  @Nullable
  protected Headers resolveHeaders() {
    if (CollectionUtils.isEmpty(properties.getHeaders())) {
      return headers;
    }
    Headers.Builder builder = headers == null ? new Headers.Builder() : headers.newBuilder();
    if (headerResolver != null) {
      for (HeaderItem header : properties.getHeaders()) {
        String value = headerResolver.resolve(header.getValue());
        builder.add(header.getName(), value);
      }
    }
    else {
      for (HeaderItem header : properties.getHeaders()) {
        builder.add(header.getName(), header.getValue());
      }
    }
    return builder.build();
  }

  @Override
  public String defaultModel() {
    return properties.getModel();
  }
}
