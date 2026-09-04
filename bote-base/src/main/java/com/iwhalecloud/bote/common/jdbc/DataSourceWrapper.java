package com.iwhalecloud.bote.common.jdbc;

import java.sql.Connection;
import org.springframework.jdbc.datasource.AbstractDataSource;

/**
 * 数据源封装器
 *
 * <p>将一个数据库连接封装为数据源</p>
 *
 * @author bianjp
 * @since 2025-11-25
 */
public class DataSourceWrapper extends AbstractDataSource {
  private final Connection connection;

  public DataSourceWrapper(Connection connection) {
    this.connection = connection;
  }

  @Override
  public Connection getConnection() {
    return connection;
  }

  @Override
  public Connection getConnection(String username, String password) {
    return connection;
  }
}
