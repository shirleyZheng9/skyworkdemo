package com.iwhalecloud.bote.config.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import java.util.List;

/**
 * ElasticSearch配置属性
 */
@ConfigurationProperties("bote.elasticsearch")
@Getter
@Setter
@ToString
@Validated
public class ElasticSearchProperties {

  /** 是否启用ElasticSearch */
  private Boolean enabled = false;
  /** ElasticSearch URI */
  private List<String> url;
  /** ElasticSearch用户名 */
  private String username;
  /** ElasticSearch密码 */
  private String password;
  /** 索引名前缀 - 伪nameSpace 标识环境名 */
  @NotBlank(message = "ElasticSearch命名空间不能为空")
  private String namespace;
  /** ElasticSearch连接超时 */
  private Duration connectionTimeout = Duration.ofSeconds(1L);
  /** ElasticSearch读取超时 */
  private Duration socketTimeout = Duration.ofSeconds(30L);
  /** 是否启用不安全的 SSL */
  private Boolean unsafeSsl = false;
}
