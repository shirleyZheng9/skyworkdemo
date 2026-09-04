package com.iwhalecloud.bote.common.sql.script;

import com.iwhalecloud.bote.common.datasource.SingleConnectionCompatibleDataSource;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.datasource.init.ScriptException;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.lang.Nullable;

/**
 * SQL 脚本工具类
 *
 * <p>用于执行 SQL 脚本</p>
 *
 * <p>参考 {@link org.springframework.boot.autoconfigure.jdbc.DataSourceInitializer}</p>
 *
 * @author bianjp
 * @since 2020-08-22
 */
@SuppressWarnings({"JavadocReference", "PMD.AvoidCatchingThrowable"})
public final class SqlScriptUtil {
  private static final Logger logger = LoggerFactory.getLogger(SqlScriptUtil.class);

  private SqlScriptUtil() {
  }

  /**
   * 执行 SQL 脚本
   *
   * <p>支持 DDL, DML, 但不建议将 DDL, DML 混在同一个脚本中</p>
   *
   * <p>整个脚本放在一个事务中执行。但不同数据库对事务的支持不太相同</p>
   *
   * <h3>DDL 事务</h3>
   *
   * <ol>
   *   <li>多数数据库都不支持 DDL 事务。PostgreSQL 支持，H2, MySQL, Oracle 不支持</li>
   *   <li>为了保证 DDL 脚本执行失败时可以重试，脚本最好做兼容处理（比如创建表之前先执行 <code>drop table if exists</code></li>
   *   <li>如果数据库不支持 DDL 事务，也无法兼容处理，那么部分执行失败时只能手动处理</li>
   * </ol>
   *
   * <p>注意: 部分执行成功时执行成功的 SQL 可能会被回滚，不代表事务已提交</p>
   *
   * @param url 数据库地址
   * @param username 数据库用户名，可为空
   * @param password 数据库密码，可为空
   * @param scriptContent SQL 脚本，可以包含多条 SQL, 使用 ";" 分隔
   * @return 执行结果。开始执行 SQL 语句前的失败（比如解析脚本失败、连接数据库失败）抛异常，只有 SQL 语句全部或部分执行成功时正常返回
   */
  public static ScriptExecuteResult executeScript(String url, @Nullable String username, @Nullable String password, String scriptContent) {
    if (StringUtils.isEmpty(scriptContent)) {
      throw BaseErrorConstant.SQL_SCRIPT_NOT_EMPTY.toException();
    }
    // 解析 SQL 语句
    List<String> statements = readScript(scriptContent);
    return executeScript(url, username, password, statements);
  }

  public static ScriptExecuteResult executeScript(String url, @Nullable String username, @Nullable String password, Resource scriptResource) {
    if (!scriptResource.exists()) {
      throw BaseErrorConstant.SQL_SCRIPT_NOT_EXIST.toException(scriptResource);
    }
    // 解析 SQL 语句
    List<String> statements = readScript(scriptResource);
    return executeScript(url, username, password, statements);
  }

  /**
   * 执行 SQL 语句列表
   *
   * @param url 数据库地址
   * @param username 数据库用户名，可为空
   * @param password 数据库密码，可为空
   * @param statements SQL 语句列表
   * @return 执行结果。开始执行 SQL 语句前的失败抛异常
   */
  public static ScriptExecuteResult executeScript(String url, @Nullable String username, @Nullable String password, List<String> statements) {
    // 校验连接地址
    validateUrl(url);
    if (statements.isEmpty()) {
      return new ScriptExecuteResult(true, Collections.emptyList());
    }
    // 获取连接，应关闭自动提交，手动管理事务
    try (SingleConnectionDataSource dataSource = new SingleConnectionCompatibleDataSource(url, username, password, true)) {
      // 提交模式配置为手动，以便手动管理事务，将整个脚本放在一个事务中执行，避免部分失败
      dataSource.setAutoCommit(false);

      try (Connection connection = dataSource.getConnection()) {
        return executeStatements(connection, url, statements);
      }
      catch (SQLException e) {
        logger.error("Failed to get database connection: url={}, username={}", url, username, e);
        throw BaseErrorConstant.GET_DATABASE_CONNECTION_FAILED.toException(e);
      }
    }
  }

  /**
   * 执行脚本中的语句列表
   */
  private static ScriptExecuteResult executeStatements(Connection connection, String url, List<String> statements) {
    // 执行结果
    List<StatementExecuteResult> statementResults = new ArrayList<>(statements.size());

    boolean allSuccess;
    try (Statement stmt = connection.createStatement()) {
      executeStatements(stmt, statements, statementResults);
      allSuccess = statementResults.size() == statements.size() && statementResults.stream().allMatch(s -> Boolean.TRUE.equals(s.getSuccess()));
      if (allSuccess) {
        // 提交事务
        connection.commit();
      }
      else {
        rollback(connection, url);
      }
    }
    catch (Throwable e) {
      logger.error("Failed to execute SQL script: url={}, script={}", url, statements, e);
      rollback(connection, url);
      throw BaseErrorConstant.EXECUTE_SQL_SCRIPT_FAILED.toException(e);
    }

    // 填充未执行的语句
    for (int i = statementResults.size(); i < statements.size(); i++) {
      statementResults.add(new StatementExecuteResult(statements.get(i)));
    }

    return new ScriptExecuteResult(allSuccess, statementResults);
  }

  /**
   * 校验数据库地址
   */
  private static void validateUrl(String url) {
    if (StringUtils.isEmpty(url)) {
      throw BaseErrorConstant.DATABASE_URL_NOT_EMPTY.toException();
    }
    if (!Strings.CI.startsWith(url, "jdbc:")) {
      throw BaseErrorConstant.INVALID_DATABASE_URL_NOT_EMPTY.toException(url);
    }
  }

  /**
   * 执行 SQL 语句列表
   */
  @SuppressFBWarnings("SQL_INJECTION_JDBC")
  @SuppressWarnings("PMD.GuardLogStatement")
  private static void executeStatements(Statement stmt, List<String> statements, List<StatementExecuteResult> statementResults) {
    for (String statement : statements) {
      try {
        stmt.execute(statement);
        int rowsAffected = stmt.getUpdateCount();
        statementResults.add(new StatementExecuteResult(statement, rowsAffected));
        logger.debug("Execute SQL statement success: rowsAffected={}, sql={}", rowsAffected, statement);
        try {
          SQLWarning warningToLog = stmt.getWarnings();
          while (warningToLog != null) {
            logger.warn("SQLWarning ignored: SQL state '{}', error code '{}', message [{}]", warningToLog.getSQLState(), warningToLog.getErrorCode(),
              warningToLog.getMessage());
            warningToLog = warningToLog.getNextWarning();
          }
        }
        catch (Exception e) {
          logger.error("Failed to log statement warnings", e);
        }
      }
      catch (Exception ex) {
        logger.error("Execute SQL statement failed: {}", statement, ex);
        statementResults.add(new StatementExecuteResult(statement, ex.getMessage()));
        break;
      }
    }
  }

  /**
   * 读取 SQL 脚本资源
   */
  public static List<String> readScript(String scriptContent) {
    try (LineNumberReader reader = new LineNumberReader(new StringReader(scriptContent))) {
      String script = ScriptReadSplitUtils.readScript(reader, ScriptUtils.DEFAULT_COMMENT_PREFIXES, ScriptUtils.DEFAULT_STATEMENT_SEPARATOR,
        ScriptUtils.DEFAULT_BLOCK_COMMENT_END_DELIMITER);
      return ScriptReadSplitUtils.splitSqlScript(script);
    }
    catch (IOException e) {
      logger.error("Failed to read SQL script: scriptContent={}", scriptContent, e);
      throw BaseErrorConstant.READ_SQL_SCRIPT_FAILED.toException(e);
    }
    catch (ScriptException e) {
      logger.error("Failed to parse SQL script: scriptContent={}", scriptContent, e);
      throw BaseErrorConstant.PARSE_SQL_SCRIPT_FAILED.toException(e);
    }
  }

  /**
   * 读取 SQL 脚本资源
   */
  public static List<String> readScript(Resource scriptResource) {
    EncodedResource encodedResource = new EncodedResource(scriptResource, StandardCharsets.UTF_8);
    try (LineNumberReader reader = new LineNumberReader(encodedResource.getReader())) {
      String script = ScriptReadSplitUtils.readScript(reader, ScriptUtils.DEFAULT_COMMENT_PREFIXES, ScriptUtils.DEFAULT_STATEMENT_SEPARATOR,
        ScriptUtils.DEFAULT_BLOCK_COMMENT_END_DELIMITER);
      List<String> statements = new ArrayList<>();
      // mysql 序列函数不进行分割
      if (script.startsWith("create function")) {
        statements.add(script);
        return statements;
      }
      statements = ScriptReadSplitUtils.splitSqlScript(script);
      return statements;
    }
    catch (IOException e) {
      logger.error("Failed to read SQL script: resource={}", scriptResource, e);
      throw BaseErrorConstant.READ_SQL_SCRIPT_FAILED.toException(e);
    }
    catch (ScriptException e) {
      logger.error("Failed to parse SQL script: resource={}", scriptResource, e);
      throw BaseErrorConstant.PARSE_SQL_SCRIPT_FAILED.toException(e);
    }
  }

  /**
   * 回滚事务
   */
  private static void rollback(Connection connection, String url) {
    try {
      connection.rollback();
    }
    catch (Throwable throwable) {
      logger.error("Failed to rollback database connection: url={}", url, throwable);
    }
  }

  /**
   * 脚本执行结果
   */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ScriptExecuteResult {
    /** 是否执行成功（true 表示所有语句都执行成功，false 表示部分成功） */
    private boolean success;
    /** 各语句执行结果列表 */
    private List<StatementExecuteResult> statements;

    /**
     * 获取第一条执行失败的语句的错误信息
     */
    @Nullable
    public String getFirstError() {
      if (CollectionUtils.isEmpty(this.statements)) {
        return null;
      }
      String error = null;
      for (StatementExecuteResult executeResult : this.statements) {
        if (Boolean.FALSE.equals(executeResult.getSuccess())) {
          error = executeResult.getError();
          break;
        }
      }
      return error;
    }
  }

  /**
   * 语句执行结果
   */
  @Data
  @NoArgsConstructor
  public static class StatementExecuteResult {
    /** SQL 语句 */
    private String sql;
    /** 是否执行成功（null 表示未执行） */
    private Boolean success;
    /** 影响行数 */
    private Integer affectedRows;
    /** 错误信息 */
    private String error;

    public StatementExecuteResult(String sql) {
      this.sql = sql;
      this.success = null;
    }

    /**
     * 执行成功
     */
    public StatementExecuteResult(String sql, int affectedRows) {
      this.sql = sql;
      this.success = true;
      this.affectedRows = affectedRows;
    }

    /**
     * 执行失败
     */
    public StatementExecuteResult(String sql, String error) {
      this.sql = sql;
      this.success = false;
      this.error = error;
    }
  }
}
