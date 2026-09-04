package com.iwhalecloud.bote.common.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.datasource.SingleConnectionCompatibleDataSource;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.entity.base.DcPublicEntity;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.database.inspect.DatabaseInspector;
import com.iwhalecloud.bss.litchi.database.inspect.definition.Column;
import com.iwhalecloud.bss.litchi.database.inspect.definition.Table;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javax.sql.DataSource;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 数据库工具类
 *
 * @author qian.sisheng
 * @since 2024/11/14
 */
public final class DatabaseUtil {
  private static final Logger logger = LoggerFactory.getLogger(DatabaseUtil.class);

  /** 表是否存在某字段的缓存 */
  private static final Cache<@NonNull String, @NonNull Boolean> COLUMN_EXIST_CACHE = CacheBuilder.newBuilder().maximumSize(500).build();

  /** 常见 SQL 关键字 */
  private static final Set<String> SQL_KEYWORDS = new HashSet<>(Arrays.asList(
    "SELECT", "INSERT", "UPDATE", "DELETE", "FROM", "WHERE", "TABLE", "CREATE", "DROP",
    "ALTER", "INDEX", "VIEW", "JOIN", "ORDER", "GROUP", "BY", "HAVING", "LIMIT", "OFFSET",
    "AND", "OR", "NOT", "NULL", "IS", "AS", "DISTINCT", "UNION", "ALL", "EXISTS", "IN",
    "SET", "VALUES", "INTO", "ON", "USING", "CASE", "WHEN", "THEN", "ELSE", "END", "SHOW", "ALERT"
  ));

  private DatabaseUtil() {
  }

  /**
   * 检测表是否存在指定字段
   *
   * @param dataSource 数据源
   * @param tableName 表名
   * @param column 检测的字段名
   * @return true-存在；false-不存在
   */
  public static Boolean isExistColumn(DataSource dataSource, String tableName, String column) {
    String key = dataSource.hashCode() + tableName.toLowerCase() + "#" + column.toLowerCase();
    try {
      return COLUMN_EXIST_CACHE.get(key, () -> {
        Table table = DatabaseInspector.inspectTable(dataSource, tableName);
        Assert.notNull(table, tableName + " 表不存在");
        List<Column> columns = table.getColumns();
        return CollectionUtils.emptyIfNull(columns).stream().anyMatch(c -> column.equalsIgnoreCase(c.getName()));
      });
    }
    catch (ExecutionException e) {
      logger.error("Failed to inspect table schema: table={}", tableName, e);
      throw BaseErrorConstant.INSPECT_TABLE_SCHEMA_FAIL.toException(e, tableName);
    }
  }

  /**
   * 检测表的主键字段（单主键、联合主键）是否存在指定的字段名称
   *
   * @param dataSource 数据源
   * @param tableName 表名
   * @param column 检测的字段名
   * @return true-存在；false-不存在
   */
  public static Boolean isExistTargetColumnInPrimaryKey(DataSource dataSource, String tableName, String column) {
    String key = dataSource.hashCode() + tableName.toLowerCase() + "#pk#" + column.toLowerCase();
    try {
      return COLUMN_EXIST_CACHE.get(key, () -> {
        Table table = DatabaseInspector.inspectTable(dataSource, tableName);
        Assert.notNull(table, tableName + " 表不存在");
        List<Column> columns = table.getPrimaryKeyColumns();
        return CollectionUtils.emptyIfNull(columns).stream().anyMatch(c -> column.equalsIgnoreCase(c.getName()));
      });
    }
    catch (ExecutionException e) {
      logger.error("Failed to inspect table schema: table={}", tableName, e);
      throw BaseErrorConstant.INSPECT_TABLE_SCHEMA_FAIL.toException(e, tableName);
    }
  }

  /**
   * 测试数据链接
   *
   * @param url 链接地址
   * @param username 用户名
   * @param password 密码
   * @return 结果
   */
  public static ResultVO<String> testAppDataSource(String url, String username, String password) {
    SingleConnectionDataSource dataSource = null; //NOPMD - suppressed CloseResource - 已使用 finally 关闭
    Connection connection = null; //NOPMD - suppressed CloseResource - 已使用 finally 关闭
    try {
      dataSource = new SingleConnectionCompatibleDataSource(url, username, password, true);
      connection = dataSource.getConnection();
      return ResultVO.success("测试通过");
    }
    catch (Exception e) {
      logger.error("测试不通过 url={}, username={}, password={}", url, username, password, e);
      return BaseErrorConstant.TEST_APP_DATASOURCE_FAILED.toResult(e, e.getMessage());
    }
    finally {
      try {
        if (connection != null) {
          connection.close();
        }
        if (dataSource != null) {
          dataSource.destroy();
        }
      }
      catch (SQLException e) {
        logger.warn("Failed to close database connection", e);
      }
    }
  }

  /**
   * 探测判断表是否"不存在"，不存在的时候返回true。存在返回false
   *
   * @param url 链接地址
   * @param username 用户名
   * @param password 密码
   * @param tableName 表名
   */
  public static boolean isTableNoExist(String url, String username, String password, String tableName) {
    try (SingleConnectionDataSource dataSource = new SingleConnectionCompatibleDataSource(url, username, password, true)) {
      Table table = DatabaseInspector.inspectTable(dataSource, tableName);
      return table == null;
    }
  }

  /**
   * 判断数据库类型是否为 oracle
   *
   * @param databaseType 数据库类型
   * @return 结果
   */
  public static boolean isOracle(String databaseType) {
    // 从配置表中读取
    List<DcPublicEntity> list = DcPublicUtil.getList(DcPublicUtil.DATABASE_ID_MATCH);
    Map<String, String> dbTypeMap = new HashMap<>(16);
    list.stream().filter(p -> StringUtils.isNotBlank(p.getCodea())).forEach(p -> {
      String[] split = p.getCodea().trim().split(",");
      for (String dbType : split) {
        if (StringUtils.isNotBlank(dbType)) {
          dbTypeMap.putIfAbsent(dbType.trim().toLowerCase(), p.getPcode());
        }
      }
    });
    return BaseConsts.DB_TYPE_ORACLE.equalsIgnoreCase(dbTypeMap.get(databaseType.toLowerCase()));
  }

  /**
   * 获取应用数据源实际使用的方言类型
   * <p>如 oceanbase 支持 oracle/mysql 模式，达梦支持 oracle/dm 模式</p>
   *
   * @param dataSourceType 应用数据源类型
   * @return 实际使用的数据库类型
   */
  @Nullable
  public static String getDatabaseDialect(String dataSourceType, @Nullable String defaultDialect) {
    List<DcPublicEntity> list = DcPublicUtil.getList(DcPublicUtil.DATABASE_ID_MATCH);
    String dialect = defaultDialect;
    for (DcPublicEntity dcPublic : list) {
      if (StringUtils.isBlank(dcPublic.getCodea())) {
        continue;
      }
      String[] split = dcPublic.getCodea().trim().split(",");
      for (String type : split) {
        if (type.trim().equalsIgnoreCase(dataSourceType)) {
          dialect = dcPublic.getPcode();
          break;
        }
      }
    }
    return dialect;
  }

  /**
   * 判断数据库/schema名称是否存在
   * @param url 链接地址
   * @param username 用户名
   * @param password 密码
   * @param querySql 查询SQL
   * @param databaseName database/schema 名称
   * @return 是否存在
   */
  @SuppressFBWarnings("SQL_INJECTION_JDBC")
  @SuppressWarnings("java:S2095")
  public static boolean databaseNameExist(String url, String username, String password, String querySql, String databaseName) {
    // 获取数据库连接
    SingleConnectionDataSource dataSource = new SingleConnectionCompatibleDataSource(url, username, password, true);

    // 执行查询
    try (Connection connection = dataSource.getConnection();
         PreparedStatement statement = connection.prepareStatement(querySql)) {
      statement.setString(1, databaseName);
      try (ResultSet resultSet = statement.executeQuery()) {
        return resultSet.next() && resultSet.getInt(1) > 0;
      }
    }
    catch (SQLException e) {
      logger.error("Failed to check database existence: url={}, sql={}", url, querySql, e);
      throw BaseErrorConstant.CHECK_DATABASE_NAME_ERROR.toException(e);
    }
    finally {
      try {
        dataSource.destroy();
      }
      catch (Exception e) {
        logger.warn("Failed to close dataSource.", e);
      }
    }
  }

  /**
   * 校验编码是否为SQL关键字
   *
   * @param code 编码
   * @return 是否为SQL关键字
   */
  public static boolean checkSqlKeyword(String code) {
    return SQL_KEYWORDS.contains(code);
  }

}
