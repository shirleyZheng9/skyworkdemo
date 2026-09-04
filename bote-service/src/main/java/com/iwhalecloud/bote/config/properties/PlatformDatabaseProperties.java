package com.iwhalecloud.bote.config.properties;

import com.iwhalecloud.bote.dto.base.DataSourceProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 平台数据库配置：用于平台创建数据库的主数据库配置
 *
 * @author wangtingyun
 * @since 2025-11-21
 */
@Getter
@Setter
@ToString
@Component
@ConfigurationProperties("bote.platform.database")
public class PlatformDatabaseProperties {

  /** 数据库类型 */
  private String type;
  /** JDBC 连接地址 */
  private String url;
  /** 用户名 */
  private String username;
  /** 密码 */
  private String password;
  /** 表命名空间 */
  private String tablespace;

  public DataSourceProperties toDataSourceProperties() {
    DataSourceProperties properties = new DataSourceProperties();
    properties.setUrl(url);
    properties.setUsername(username);
    properties.setPassword(password);
    properties.setTablespace(tablespace);
    return properties;
  }

}

