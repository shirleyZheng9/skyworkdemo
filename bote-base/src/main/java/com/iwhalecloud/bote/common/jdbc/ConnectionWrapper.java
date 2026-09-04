package com.iwhalecloud.bote.common.jdbc;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * 数据库连接封装器
 *
 * <p>屏蔽 close 方法，以避免 JdbcTemplate 自动关闭连接</p>
 *
 * @author bianjp
 * @since 2025-11-25
 */
@SuppressFBWarnings({"SECCONFCTRL", "SECSQLIJDBC"})
public class ConnectionWrapper implements Connection {
  private final Connection target;

  public ConnectionWrapper(Connection target) {
    this.target = target;
  }


  @Override
  public Statement createStatement() throws SQLException {
    return target.createStatement();
  }

  @Override
  public PreparedStatement prepareStatement(String sql) throws SQLException {
    return target.prepareStatement(sql);
  }

  @Override
  public CallableStatement prepareCall(String sql) throws SQLException {
    return target.prepareCall(sql);
  }

  @Override
  public String nativeSQL(String sql) throws SQLException {
    return target.nativeSQL(sql);
  }

  @Override
  public void setAutoCommit(boolean autoCommit) throws SQLException {
    target.setAutoCommit(autoCommit);
  }

  @Override
  @SuppressWarnings("PMD.BooleanGetMethodName")
  public boolean getAutoCommit() throws SQLException {
    return target.getAutoCommit();
  }

  @Override
  public void commit() throws SQLException {
    target.commit();
  }

  @Override
  public void rollback() throws SQLException {
    target.rollback();
  }

  @Override
  public void close() {
    // 避免 JdbcTemplate 自动关闭连接
  }

  /**
   * 关闭连接
   */
  public void closeTargetConnection() throws SQLException {
    target.close();
  }

  @Override
  public boolean isClosed() throws SQLException {
    return target.isClosed();
  }

  @Override
  public DatabaseMetaData getMetaData() throws SQLException {
    return target.getMetaData();
  }

  @Override
  public void setReadOnly(boolean readOnly) throws SQLException {
    target.setReadOnly(readOnly);
  }

  @Override
  public boolean isReadOnly() throws SQLException {
    return target.isReadOnly();
  }

  @Override
  public void setCatalog(String catalog) throws SQLException {
    target.setCatalog(catalog);
  }

  @Override
  public String getCatalog() throws SQLException {
    return target.getCatalog();
  }

  @Override
  public void setTransactionIsolation(int level) throws SQLException {
    target.setTransactionIsolation(level);
  }

  @Override
  public int getTransactionIsolation() throws SQLException {
    return target.getTransactionIsolation();
  }

  @Override
  public SQLWarning getWarnings() throws SQLException {
    return target.getWarnings();
  }

  @Override
  public void clearWarnings() throws SQLException {
    target.clearWarnings();
  }

  @Override
  public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
    return target.createStatement(resultSetType, resultSetConcurrency);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
    return target.prepareStatement(sql, resultSetType, resultSetConcurrency);
  }

  @Override
  public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
    return target.prepareCall(sql, resultSetType, resultSetConcurrency);
  }

  @Override
  public Map<String, Class<?>> getTypeMap() throws SQLException {
    return target.getTypeMap();
  }

  @Override
  public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
    target.setTypeMap(map);
  }

  @Override
  public void setHoldability(int holdability) throws SQLException {
    target.setHoldability(holdability);
  }

  @Override
  public int getHoldability() throws SQLException {
    return target.getHoldability();
  }

  @Override
  public Savepoint setSavepoint() throws SQLException {
    return target.setSavepoint();
  }

  @Override
  public Savepoint setSavepoint(String name) throws SQLException {
    return target.setSavepoint(name);
  }

  @Override
  public void rollback(Savepoint savepoint) throws SQLException {
    target.rollback(savepoint);
  }

  @Override
  public void releaseSavepoint(Savepoint savepoint) throws SQLException {
    target.releaseSavepoint(savepoint);
  }

  @Override
  public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
    return target.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
    return target.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
  }

  @Override
  public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
    return target.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
    return target.prepareStatement(sql, autoGeneratedKeys);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
    return target.prepareStatement(sql, columnIndexes);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
    return target.prepareStatement(sql, columnNames);
  }

  @Override
  public Clob createClob() throws SQLException {
    return target.createClob();
  }

  @Override
  public Blob createBlob() throws SQLException {
    return target.createBlob();
  }

  @Override
  public NClob createNClob() throws SQLException {
    return target.createNClob();
  }

  @Override
  public SQLXML createSQLXML() throws SQLException {
    return target.createSQLXML();
  }

  @Override
  public boolean isValid(int timeout) throws SQLException {
    return target.isValid(timeout);
  }

  @Override
  public void setClientInfo(String name, String value) throws SQLClientInfoException {
    target.setClientInfo(name, value);
  }

  @Override
  public void setClientInfo(Properties properties) throws SQLClientInfoException {
    target.setClientInfo(properties);
  }

  @Override
  public String getClientInfo(String name) throws SQLException {
    return target.getClientInfo(name);
  }

  @Override
  public Properties getClientInfo() throws SQLException {
    return target.getClientInfo();
  }

  @Override
  public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
    return target.createArrayOf(typeName, elements);
  }

  @Override
  public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
    return target.createStruct(typeName, attributes);
  }

  @Override
  public void setSchema(String schema) throws SQLException {
    target.setSchema(schema);
  }

  @Override
  public String getSchema() throws SQLException {
    return target.getSchema();
  }

  @Override
  public void abort(Executor executor) throws SQLException {
    target.abort(executor);
  }

  @Override
  public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
    target.setNetworkTimeout(executor, milliseconds);
  }

  @Override
  public int getNetworkTimeout() throws SQLException {
    return target.getNetworkTimeout();
  }

  @Override
  public <T> T unwrap(Class<T> clazz) throws SQLException {
    return target.unwrap(clazz);
  }

  @Override
  public boolean isWrapperFor(Class<?> clazz) throws SQLException {
    return target.isWrapperFor(clazz);
  }
}
