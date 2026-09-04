package com.iwhalecloud.bote.doc.common.space;

import java.util.Collections;
import java.util.Set;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 企业空间配置
 */
@ConfigurationProperties(prefix = "bote.dc.space")
@Data
public class SpaceProperties {

  /**
   * 企业空间是否开启
   */
  private static final Boolean ENABLE_DEFAULT = true;

  /**
   * 是否开启
   */
  private Boolean enable = ENABLE_DEFAULT;

  /**
   * 需要忽略企业空间的请求
   */
  private Set<String> ignoreUrls = Collections.emptySet();

}
