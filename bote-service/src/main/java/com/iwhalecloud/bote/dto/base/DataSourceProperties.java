package com.iwhalecloud.bote.dto.base;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 数据源配置
 *
 * @author liuxiansheng
 * @since 2021-01-15
 */
@Getter
@Setter
@ToString
public class DataSourceProperties {
  /** JDBC 地址 */
  private String url;
  /** 用户名 */
  private String username;
  /** 密码 */
  private String password;

  /** 表命名空间 */
  private String tablespace;
  /** 数据源ID */
  private Long dataSourceId;
  /** 租户ID */
  private Long tenantId;

  public DataSourceProperties() {
  }

  public DataSourceProperties(String url, String username, String password) {
    this.url = url;
    this.username = username;
    this.password = password;
  }

}

