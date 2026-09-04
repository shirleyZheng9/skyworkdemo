package com.iwhalecloud.bote.doc.config.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * nodejs 服务的调用配置
 *
 * @author Aiqing
 * @since 2025/9/24
 */
@ConfigurationProperties(prefix = "bote.dc.nodejs")
@Getter
@Setter
@ToString
public class NodeJsServerProperties {

  /**
   * 访问地址
   */
  private String endpoint;
  /**
   * 接口认证key
   */
  private String apiKey;
}
