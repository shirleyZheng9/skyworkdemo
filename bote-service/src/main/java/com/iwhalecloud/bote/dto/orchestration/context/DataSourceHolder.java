package com.iwhalecloud.bote.dto.orchestration.context;

import com.iwhalecloud.bote.common.jdbc.ConnectionWrapper;
import com.iwhalecloud.bss.litchi.database.consts.DatabaseType;
import javax.sql.DataSource;
import lombok.Getter;
import lombok.Setter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.Nullable;

/**
 * 数据源持有者
 *
 * <p>用于支持在编排服务中同时使用多个数据源</p>
 *
 * @author bianjp
 * @since 2025-11-25
 */
@Getter
@Setter
public class DataSourceHolder {
  /** 数据源实例 */
  private final DataSource dataSource;
  /** 数据源类型 */
  private final DatabaseType dataSourceType;
  /** 数据库连接实例 */
  @Nullable
  private final ConnectionWrapper connection;
  /** 使用命名参数的 JDBC 模板 */
  private final NamedParameterJdbcTemplate namedJdbcTemplate;
  /** JDBC 模板 */
  private final JdbcTemplate jdbcTemplate;

  /**
   * 构造数据源持有者实例
   *
   * @param dataSource 数据源
   * @param dataSourceType 数据源类型
   * @param connection 连接实例
   * @param namedJdbcTemplate 命名参数版 JdbcTemplate 实例
   */
  public DataSourceHolder(DataSource dataSource, DatabaseType dataSourceType, @Nullable ConnectionWrapper connection, NamedParameterJdbcTemplate namedJdbcTemplate) {
    this.dataSource = dataSource;
    this.dataSourceType = dataSourceType;
    this.connection = connection;
    this.namedJdbcTemplate = namedJdbcTemplate;
    this.jdbcTemplate = namedJdbcTemplate.getJdbcTemplate();
  }

}
