package com.iwhalecloud.bote.llm.client.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 大模型 HTTP 客户端配置
 *
 * @author bianjp
 * @since 2024-08-01
 */
@Component
@ConfigurationProperties("bote.llm.http.client")
@Getter
@Setter
@ToString
public class ModelHttpClientProperties {
  /** 请求超时时间(s)，指的是请求的完全生命周期 */
  private int callTimeout = 300;
  /** 连接超时时间(s) */
  private int connectTimeout = 10;
  /** 读取超时时间(s) */
  private int readTimeout = 300;
  /** 写入超时时间(s) */
  private int writeTimeout = 60;
  /** 最大空闲连接数。设置为 0 可以禁用连接复用 */
  private int maxIdleConnections = 20;
  /** 连接保活时间(s) */
  private int keepAliveTimeout = 60;
  /** 是否启用不安全的 SSL */
  private boolean unsafeSsl = false;
}
