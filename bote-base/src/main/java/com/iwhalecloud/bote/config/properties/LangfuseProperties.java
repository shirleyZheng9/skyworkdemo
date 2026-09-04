package com.iwhalecloud.bote.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Langfuse 可观测性配置
 *
 * @author cursor
 * @since 2026-07-23
 */
@Component
@ConfigurationProperties("bote.langfuse")
@Getter
@Setter
public class LangfuseProperties {
  /** 是否启用 */
  private boolean enabled = false;
  /** Langfuse 服务地址，例如 http://10.0.13.65:3000 */
  private String baseUrl;
  /** Public Key */
  private String publicKey;
  /** Secret Key */
  private String secretKey;

  public boolean isConfigured() {
    return enabled && StringUtils.isNotBlank(baseUrl) && StringUtils.isNotBlank(publicKey) && StringUtils.isNotBlank(secretKey);
  }

  public String getIngestionUrl() {
    String base = StringUtils.trimToEmpty(baseUrl);
    while (base.endsWith("/")) {
      base = base.substring(0, base.length() - 1);
    }
    return base + "/api/public/ingestion";
  }
}
