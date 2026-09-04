package com.iwhalecloud.bote.doc.config.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 多维表格服务对接配置项
 *
 * @author Aiqing
 * @since 2026/1/9
 */
@Getter
@Setter
@ToString
@ConfigurationProperties(prefix = "bote.dc.dim-table")
public class DimTableServerProperties {

  /**
   * 服务访问基础地址
   */
  private String serverUrl;

  /**
   * 接口认证key
   */
  private String apiKey;
}
