package com.iwhalecloud.bote.common.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.sql.Connection;
import org.junit.jupiter.api.Test;

/**
 * {@link DataSourceWrapper} 单元测试
 *
 * <p>验证将单个 {@link Connection} 封装为数据源：无参/带账密的 getConnection() 均原样返回被封装连接。</p>
 */
class DataSourceWrapperTest {

  @Test
  void getConnection_returnsWrappedConnection() {
    Connection connection = mock(Connection.class);
    DataSourceWrapper wrapper = new DataSourceWrapper(connection);

    assertThat(wrapper.getConnection()).isSameAs(connection);
  }

  @Test
  void getConnectionWithCredentials_ignoresCredentials_returnsWrappedConnection() {
    Connection connection = mock(Connection.class);
    DataSourceWrapper wrapper = new DataSourceWrapper(connection);

    assertThat(wrapper.getConnection("user", "pass")).isSameAs(connection);
  }
}
