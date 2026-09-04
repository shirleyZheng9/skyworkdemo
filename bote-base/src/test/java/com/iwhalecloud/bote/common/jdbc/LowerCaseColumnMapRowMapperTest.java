package com.iwhalecloud.bote.common.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.util.LinkedCaseInsensitiveMap;

/**
 * {@link LowerCaseColumnMapRowMapper} 单元测试
 *
 * <p>覆盖 mapRow 将列名转为小写键、putIfAbsent 保留首列值、以及受保护方法
 * getColumnKey/createColumnMap/getColumnValue 的行为。</p>
 */
class LowerCaseColumnMapRowMapperTest {

  private final LowerCaseColumnMapRowMapper mapper = new LowerCaseColumnMapRowMapper();

  @Test
  void mapRow_lowercasesColumnKeys() throws SQLException {
    ResultSet rs = mock(ResultSet.class);
    ResultSetMetaData md = mock(ResultSetMetaData.class);
    when(rs.getMetaData()).thenReturn(md);
    when(md.getColumnCount()).thenReturn(2);
    when(md.getColumnLabel(1)).thenReturn("Name");
    when(md.getColumnLabel(2)).thenReturn("AGE");
    when(rs.getObject(1)).thenReturn("Alice");
    when(rs.getObject(2)).thenReturn(30);

    Map<String, Object> row = mapper.mapRow(rs, 1);

    assertThat(row).hasSize(2)
        .containsEntry("name", "Alice")
        .containsEntry("age", 30);
  }

  @Test
  void mapRow_duplicateKeys_putIfAbsentKeepsFirst() throws SQLException {
    ResultSet rs = mock(ResultSet.class);
    ResultSetMetaData md = mock(ResultSetMetaData.class);
    when(rs.getMetaData()).thenReturn(md);
    when(md.getColumnCount()).thenReturn(2);
    // 两列小写后同为 "name"
    when(md.getColumnLabel(1)).thenReturn("Name");
    when(md.getColumnLabel(2)).thenReturn("NAME");
    when(rs.getObject(1)).thenReturn("first");
    when(rs.getObject(2)).thenReturn("second");

    Map<String, Object> row = mapper.mapRow(rs, 0);

    assertThat(row).hasSize(1).containsEntry("name", "first");
  }

  @Test
  void mapRow_emptyColumns_returnsEmptyMap() throws SQLException {
    ResultSet rs = mock(ResultSet.class);
    ResultSetMetaData md = mock(ResultSetMetaData.class);
    when(rs.getMetaData()).thenReturn(md);
    when(md.getColumnCount()).thenReturn(0);

    assertThat(mapper.mapRow(rs, 0)).isEmpty();
  }

  // ==================== 受保护方法 ====================

  @Test
  void getColumnKey_lowercases() {
    assertThat(mapper.getColumnKey("FooBar")).isEqualTo("foobar");
    assertThat(mapper.getColumnKey("already_lower")).isEqualTo("already_lower");
  }

  @Test
  void createColumnMap_returnsLinkedCaseInsensitiveMap() {
    Map<String, Object> map = mapper.createColumnMap(3);
    assertThat(map).isInstanceOf(LinkedCaseInsensitiveMap.class);
  }

  @Test
  void getColumnValue_delegatesToJdbcUtils() throws SQLException {
    ResultSet rs = mock(ResultSet.class);
    when(rs.getObject(1)).thenReturn("value");
    assertThat(mapper.getColumnValue(rs, 1)).isEqualTo("value");
  }

  @Test
  void getColumnValue_returnsNullForNullValue() throws SQLException {
    ResultSet rs = mock(ResultSet.class);
    when(rs.getObject(1)).thenReturn(null);
    assertThat(mapper.getColumnValue(rs, 1)).isNull();
  }

  // ==================== 可子类化验证（putIfAbsent 语义可在子类覆盖） ====================

  @Test
  void subclassCanOverrideCreateColumnMap() throws SQLException {
    LowerCaseColumnMapRowMapper custom = new LowerCaseColumnMapRowMapper() {
      @Override
      protected Map<String, Object> createColumnMap(int columnCount) {
        return new LinkedHashMap<>();
      }
    };
    ResultSet rs = mock(ResultSet.class);
    ResultSetMetaData md = mock(ResultSetMetaData.class);
    when(rs.getMetaData()).thenReturn(md);
    when(md.getColumnCount()).thenReturn(1);
    when(md.getColumnLabel(1)).thenReturn("X");
    when(rs.getObject(1)).thenReturn("v");

    Map<String, Object> row = custom.mapRow(rs, 0);
    assertThat(row).isInstanceOf(LinkedHashMap.class).containsEntry("x", "v");
  }
}
