package com.iwhalecloud.bote.service.base;

import com.iwhalecloud.bote.cache.TenantCacheMarker;
import com.iwhalecloud.bote.common.jdbc.ConnectionWrapper;
import com.iwhalecloud.bss.litchi.database.consts.DatabaseType;
import javax.sql.DataSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.Nullable;

/**
 * 数据源提供者接口
 *
 * @author qian.sisheng
 * @since 2024/8/8
 */
public interface IDataSourceProviderService extends TenantCacheMarker {
  /**
   * 获取数据源实例
   *
   * @param tenantId 租户 ID
   * @param dataSourceId 数据源 ID
   * @return 数据源实例。不返回 null, 找不到数据源时抛异常
   */
  DataSource getDataSource(Long tenantId, Long dataSourceId);

  /**
   * 获取数据库类型
   *
   * @param tenantId 租户 ID
   * @param dataSourceId 数据源ID
   * @return 数据库类型
   */
  DatabaseType getDataSourceType(Long tenantId, Long dataSourceId);

  /**
   * 获取数据库方言类型
   *
   * <p>用于解析 SQL 时指定的数据库类型（使用衍生品类型如 UDAL 会导致 Druid 无法解析 MySQL 特有语法），以及 MyBatis 的 databaseId</p>
   *
   * @param dataSourceId 数据源ID
   * @return 数据库方言类型，全小写
   */
  String getDataSourceDialect(Long tenantId, Long dataSourceId);

  /**
   * 获取数据源连接
   *
   * <p>事务需手动管理；关闭连接使用 {@link #closeConnection}</p>
   *
   * @param dataSource 数据源
   * @return 连接封装器实例
   */
  ConnectionWrapper getConnectionWrapper(DataSource dataSource);

  /**
   * 创建命名参数 JDBC 模板
   *
   * @param connectionWrapper 数据源连接封装器
   * @return 命名参数 JDBC 模板实例
   */
  NamedParameterJdbcTemplate createNamedJdbcTemplate(ConnectionWrapper connectionWrapper);

  /**
   * 关闭数据源连接
   *
   * <p>失败时只记录日志，不要抛异常</p>
   *
   * @param connectionWrapper 连接封装器实例
   */
  void closeConnection(@Nullable ConnectionWrapper connectionWrapper);
}
