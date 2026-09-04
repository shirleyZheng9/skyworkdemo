package com.iwhalecloud.bote.doc.common.tenant;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 多租户配置
 */
@ConfigurationProperties(prefix = "bote.dc.tenant")
@Data
public class TenantProperties {

  /**
   * 租户是否开启
   */
  private static final Boolean ENABLE_DEFAULT = true;

  /**
   * 是否开启
   */
  private Boolean enable = ENABLE_DEFAULT;

  /**
   * 需要忽略多租户的请求
   */
  private Set<String> ignoreUrls = Collections.emptySet();

  /**
   * 忽略的Mapper方法列表（支持通配符）
   * 例如：
   * - com.example.mapper.UserMapper.selectAll
   * - com.example.mapper.*.selectById
   * - *.selectByExample
   */
  private List<String> ignoreMethods;

  /**
   * 多租户插件启用的包路径
   */
  private List<String> enablePackages;

  /**
   * 需要忽略多租户的表
   * <p>
   * 即默认所有表都开启多租户的功能，此字段配置忽略表名
   */
  private Set<String> ignoreTables = Collections.emptySet();

}
