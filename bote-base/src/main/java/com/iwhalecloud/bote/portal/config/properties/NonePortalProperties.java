package com.iwhalecloud.bote.portal.config.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * none 门户配置
 *
 * @author bianjp
 * @since 2024-11-14
 */
@ConfigurationProperties("bote.none-portal")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class NonePortalProperties {
  /** 新用户的默认租户 ID */
  private Long defaultTenantId;
  /** 新用户的默认角色 */
  private String defaultRole;
}
