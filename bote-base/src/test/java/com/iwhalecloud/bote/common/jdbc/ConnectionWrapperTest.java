package com.iwhalecloud.bote.common.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * {@link ConnectionWrapper} 单元测试
 *
 * <p>该类是 {@link Connection} 的纯委托封装：除 {@link ConnectionWrapper#close()} 屏蔽以避免
 * JdbcTemplate 自动关闭连接、新增 {@link ConnectionWrapper#closeTargetConnection()} 外，
 * 其余方法均原样转发给被封装的 target。测试逐一验证委托关系及 close 屏蔽语义。</p>
 */
class ConnectionWrapperTest {

  private Connection target;
  private ConnectionWrapper wrapper;

  @BeforeEach
  void setUp() {
    target = mock(Connection.class);
    wrapper = new ConnectionWrapper(target);
  }

  // ==================== close 屏蔽语义 ====================

  @Test
  void close_doesNotDelegateToTarget() {
    wrapper.close();
    verifyNoInteractions(target);
  }

  @Test
  void closeTargetConnection_delegatesToTarget() throws SQLException {
    wrapper.closeTargetConnection();
    verify(target).close();
  }

  // ==================== Statement 方法 ====================

  @Test
  void createStatement_delegates() throws SQLException {
    Statement stmt = mock(Statement.class);
    when(target.createStatement()).thenReturn(stmt);
    assertThat(wrapper.createStatement()).isSameAs(stmt);
    verify(target).createStatement();
  }

  @Test
  void createStatement_twoArgs_delegates() throws SQLException {
    Statement stmt = mock(Statement.class);
    when(target.createStatement(ResultSetType.TYPE_FORWARD_ONLY, ResultSetConcurrency.CONCUR_READ_ONLY)).thenReturn(stmt);
    assertThat(wrapper.createStatement(ResultSetType.TYPE_FORWARD_ONLY, ResultSetConcurrency.CONCUR_READ_ONLY)).isSameAs(stmt);
  }

  @Test
  void createStatement_threeArgs_delegates() throws SQLException {
    Statement stmt = mock(Statement.class);
    when(target.createStatement(1, 2, 3)).thenReturn(stmt);
    assertThat(wrapper.createStatement(1, 2, 3)).isSameAs(stmt);
  }

  @Test
  void prepareStatement_delegates() throws SQLException {
    PreparedStatement ps = mock(PreparedStatement.class);
    when(target.prepareStatement("select 1")).thenReturn(ps);
    assertThat(wrapper.prepareStatement("select 1")).isSameAs(ps);
  }

  @Test
  void prepareStatement_variants_delegate() throws SQLException {
    PreparedStatement ps = mock(PreparedStatement.class);
    when(target.prepareStatement("sql", 1, 2)).thenReturn(ps);
    when(target.prepareStatement("sql", 1, 2, 3)).thenReturn(ps);
    when(target.prepareStatement("sql", 1)).thenReturn(ps);
    when(target.prepareStatement("sql", new int[] {1})).thenReturn(ps);
    when(target.prepareStatement("sql", new String[] {"c"})).thenReturn(ps);

    assertThat(wrapper.prepareStatement("sql", 1, 2)).isSameAs(ps);
    assertThat(wrapper.prepareStatement("sql", 1, 2, 3)).isSameAs(ps);
    assertThat(wrapper.prepareStatement("sql", 1)).isSameAs(ps);
    assertThat(wrapper.prepareStatement("sql", new int[] {1})).isSameAs(ps);
    assertThat(wrapper.prepareStatement("sql", new String[] {"c"})).isSameAs(ps);
  }

  @Test
  void prepareCall_variants_delegate() throws SQLException {
    CallableStatement cs = mock(CallableStatement.class);
    when(target.prepareCall("sql")).thenReturn(cs);
    when(target.prepareCall("sql", 1, 2)).thenReturn(cs);
    when(target.prepareCall("sql", 1, 2, 3)).thenReturn(cs);

    assertThat(wrapper.prepareCall("sql")).isSameAs(cs);
    assertThat(wrapper.prepareCall("sql", 1, 2)).isSameAs(cs);
    assertThat(wrapper.prepareCall("sql", 1, 2, 3)).isSameAs(cs);
  }

  @Test
  void nativeSQL_delegates() throws SQLException {
    when(target.nativeSQL("sql")).thenReturn("native");
    assertThat(wrapper.nativeSQL("sql")).isEqualTo("native");
  }

  // ==================== 事务方法 ====================

  @Test
  void autoCommit_delegates() throws SQLException {
    wrapper.setAutoCommit(true);
    verify(target).setAutoCommit(true);
    when(target.getAutoCommit()).thenReturn(true);
    assertThat(wrapper.getAutoCommit()).isTrue();
  }

  @Test
  void commit_delegates() throws SQLException {
    wrapper.commit();
    verify(target).commit();
  }

  @Test
  void rollback_delegates() throws SQLException {
    wrapper.rollback();
    verify(target).rollback();
    Savepoint sp = mock(Savepoint.class);
    wrapper.rollback(sp);
    verify(target).rollback(sp);
  }

  @Test
  void releaseSavepoint_delegates() throws SQLException {
    Savepoint sp = mock(Savepoint.class);
    wrapper.releaseSavepoint(sp);
    verify(target).releaseSavepoint(sp);
  }

  @Test
  void savepoint_delegates() throws SQLException {
    Savepoint sp = mock(Savepoint.class);
    when(target.setSavepoint()).thenReturn(sp);
    when(target.setSavepoint("name")).thenReturn(sp);
    assertThat(wrapper.setSavepoint()).isSameAs(sp);
    assertThat(wrapper.setSavepoint("name")).isSameAs(sp);
  }

  // ==================== 状态查询方法 ====================

  @Test
  void stateQueries_delegate() throws SQLException {
    when(target.isClosed()).thenReturn(true);
    when(target.isReadOnly()).thenReturn(true);
    when(target.getCatalog()).thenReturn("cat");
    when(target.getTransactionIsolation()).thenReturn(Connection.TRANSACTION_READ_COMMITTED);
    when(target.getHoldability()).thenReturn(1);
    assertThat(wrapper.isClosed()).isTrue();
    assertThat(wrapper.isReadOnly()).isTrue();
    assertThat(wrapper.getCatalog()).isEqualTo("cat");
    assertThat(wrapper.getTransactionIsolation()).isEqualTo(Connection.TRANSACTION_READ_COMMITTED);
    assertThat(wrapper.getHoldability()).isEqualTo(1);
  }

  @Test
  void stateSetters_delegate() throws SQLException {
    wrapper.setReadOnly(true);
    wrapper.setCatalog("cat");
    wrapper.setTransactionIsolation(2);
    wrapper.setHoldability(3);
    verify(target).setReadOnly(true);
    verify(target).setCatalog("cat");
    verify(target).setTransactionIsolation(2);
    verify(target).setHoldability(3);
  }

  @Test
  void warnings_delegate() throws SQLException {
    SQLWarning warn = mock(SQLWarning.class);
    when(target.getWarnings()).thenReturn(warn);
    // SQLWarning 是 Throwable，直接 assertThat 会与 ThrowableAssert/Iterable 重载歧义，转 Object
    assertThat((Object) wrapper.getWarnings()).isSameAs(warn);
    wrapper.clearWarnings();
    verify(target).clearWarnings();
  }

  @Test
  void metadata_and_typeMap_delegate() throws SQLException {
    DatabaseMetaData md = mock(DatabaseMetaData.class);
    when(target.getMetaData()).thenReturn(md);
    assertThat(wrapper.getMetaData()).isSameAs(md);

    Map<String, Class<?>> typeMap = Map.of();
    when(target.getTypeMap()).thenReturn(typeMap);
    assertThat(wrapper.getTypeMap()).isSameAs(typeMap);
    wrapper.setTypeMap(typeMap);
    verify(target).setTypeMap(typeMap);
  }

  // ==================== create* 方法 ====================

  @Test
  void createLobAndStruct_delegates() throws SQLException {
    Clob clob = mock(Clob.class);
    Blob blob = mock(Blob.class);
    NClob nclob = mock(NClob.class);
    SQLXML sqlxml = mock(SQLXML.class);
    Array array = mock(Array.class);
    Struct struct = mock(Struct.class);

    when(target.createClob()).thenReturn(clob);
    when(target.createBlob()).thenReturn(blob);
    when(target.createNClob()).thenReturn(nclob);
    when(target.createSQLXML()).thenReturn(sqlxml);
    when(target.createArrayOf("t", new Object[] {1})).thenReturn(array);
    when(target.createStruct("t", new Object[] {1})).thenReturn(struct);

    assertThat(wrapper.createClob()).isSameAs(clob);
    assertThat(wrapper.createBlob()).isSameAs(blob);
    assertThat(wrapper.createNClob()).isSameAs(nclob);
    assertThat(wrapper.createSQLXML()).isSameAs(sqlxml);
    assertThat(wrapper.createArrayOf("t", new Object[] {1})).isSameAs(array);
    assertThat(wrapper.createStruct("t", new Object[] {1})).isSameAs(struct);
  }

  // ==================== clientInfo / schema / network / isValid ====================

  @Test
  void clientInfo_delegates() throws SQLException, SQLClientInfoException {
    when(target.getClientInfo("k")).thenReturn("v");
    Properties props = new Properties();
    when(target.getClientInfo()).thenReturn(props);
    assertThat(wrapper.getClientInfo("k")).isEqualTo("v");
    assertThat(wrapper.getClientInfo()).isSameAs(props);

    wrapper.setClientInfo("k", "v");
    wrapper.setClientInfo(props);
    verify(target).setClientInfo("k", "v");
    verify(target).setClientInfo(props);
  }

  @Test
  void schema_delegates() throws SQLException {
    when(target.getSchema()).thenReturn("s");
    assertThat(wrapper.getSchema()).isEqualTo("s");
    wrapper.setSchema("s");
    verify(target).setSchema("s");
  }

  @Test
  void networkTimeout_delegates() throws SQLException {
    Executor exec = Runnable::run;
    when(target.getNetworkTimeout()).thenReturn(100);
    assertThat(wrapper.getNetworkTimeout()).isEqualTo(100);
    wrapper.setNetworkTimeout(exec, 200);
    verify(target).setNetworkTimeout(exec, 200);
  }

  @Test
  void abort_delegates() throws SQLException {
    Executor exec = Runnable::run;
    wrapper.abort(exec);
    verify(target).abort(exec);
  }

  @Test
  void isValid_delegates() throws SQLException {
    when(target.isValid(5)).thenReturn(true);
    assertThat(wrapper.isValid(5)).isTrue();
  }

  @Test
  void unwrap_delegates() throws SQLException {
    when(target.unwrap(Connection.class)).thenReturn(target);
    assertThat(wrapper.unwrap(Connection.class)).isSameAs(target);
    when(target.isWrapperFor(Connection.class)).thenReturn(true);
    assertThat(wrapper.isWrapperFor(Connection.class)).isTrue();
  }

  // ResultSet 类型常量别名，提升可读性
  private static final class ResultSetType {
    static final int TYPE_FORWARD_ONLY = java.sql.ResultSet.TYPE_FORWARD_ONLY;
  }

  private static final class ResultSetConcurrency {
    static final int CONCUR_READ_ONLY = java.sql.ResultSet.CONCUR_READ_ONLY;
  }
}
