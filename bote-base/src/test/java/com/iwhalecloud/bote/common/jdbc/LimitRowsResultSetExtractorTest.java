package com.iwhalecloud.bote.common.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * {@link LimitRowsResultSetExtractor} 单元测试
 *
 * <p>覆盖三个构造函数（含 columnNames 空集合归一为 null）、extractData 的正常/空结果集/超限抛错
 * 分支、extractColumnLabels 的大小写归一（无 columnNames 全小写 vs 指定 columnNames 按原名匹配）、
 * 以及 parseSqlArray 对 Array 列与嵌套 Array 的解析。</p>
 */
class LimitRowsResultSetExtractorTest {

  // ==================== 构造函数 ====================

  @Test
  void constructor_defaultMaxRows() throws SQLException {
    // 不抛异常即通过；DEFAULT_MAX_ROWS = 10000
    assertThat(new LimitRowsResultSetExtractor().extractData(emptyResultSet())).isEmpty();
  }

  @Test
  void constructor_emptyColumnNames_treatedAsNull() throws SQLException {
    // CollectionUtils.isEmpty -> columnNames=null，走全小写分支
    ResultSet rs = mock(ResultSet.class);
    ResultSetMetaData md = mock(ResultSetMetaData.class);
    when(rs.getMetaData()).thenReturn(md);
    when(md.getColumnCount()).thenReturn(1);
    when(md.getColumnLabel(1)).thenReturn("NAME");
    when(rs.next()).thenReturn(true, false);
    when(rs.getObject(1)).thenReturn("v");

    List<Map<String, Object>> result = new LimitRowsResultSetExtractor(100, Collections.emptyList())
        .extractData(rs);
    assertThat(result.get(0)).containsEntry("name", "v");
  }

  // ==================== extractData 正常路径 ====================

  @Test
  void extractData_noColumnNames_lowercasesLabels() throws SQLException {
    ResultSet rs = mock(ResultSet.class);
    ResultSetMetaData md = mock(ResultSetMetaData.class);
    when(rs.getMetaData()).thenReturn(md);
    when(md.getColumnCount()).thenReturn(2);
    when(md.getColumnLabel(1)).thenReturn("NAME");
    when(md.getColumnLabel(2)).thenReturn("Age");
    when(rs.next()).thenReturn(true, true, false);
    when(rs.getObject(1)).thenReturn("Alice");
    when(rs.getObject(2)).thenReturn(30);

    List<Map<String, Object>> result = new LimitRowsResultSetExtractor().extractData(rs);

    assertThat(result).hasSize(2);
    assertThat(result.get(0)).containsEntry("name", "Alice").containsEntry("age", 30);
    assertThat(result.get(1)).containsEntry("name", "Alice").containsEntry("age", 30);
  }

  @Test
  void extractData_withColumnNames_preservesSpecifiedCase() throws SQLException {
    ResultSet rs = mock(ResultSet.class);
    ResultSetMetaData md = mock(ResultSetMetaData.class);
    when(rs.getMetaData()).thenReturn(md);
    when(md.getColumnCount()).thenReturn(2);
    // 数据库返回小写，期望按 columnNames 中的原名还原大小写
    when(md.getColumnLabel(1)).thenReturn("name");
    when(md.getColumnLabel(2)).thenReturn("age");
    when(rs.next()).thenReturn(true, false);
    when(rs.getObject(1)).thenReturn("Alice");
    when(rs.getObject(2)).thenReturn(30);

    List<Map<String, Object>> result =
        new LimitRowsResultSetExtractor(100, List.of("Name", "AGE")).extractData(rs);

    assertThat(result.get(0)).containsEntry("Name", "Alice").containsEntry("AGE", 30);
  }

  @Test
  void extractData_emptyResultSet_returnsEmptyList() throws SQLException {
    ResultSet rs = mock(ResultSet.class);
    ResultSetMetaData md = mock(ResultSetMetaData.class);
    when(rs.getMetaData()).thenReturn(md);
    when(md.getColumnCount()).thenReturn(1);
    when(md.getColumnLabel(1)).thenReturn("c");
    when(rs.next()).thenReturn(false);

    assertThat(new LimitRowsResultSetExtractor().extractData(rs)).isEmpty();
  }

  @Test
  void extractData_exceedsMaxRows_throwsBssException() throws SQLException {
    ResultSet rs = mock(ResultSet.class);
    ResultSetMetaData md = mock(ResultSetMetaData.class);
    when(rs.getMetaData()).thenReturn(md);
    when(md.getColumnCount()).thenReturn(1);
    when(md.getColumnLabel(1)).thenReturn("c");
    when(rs.next()).thenReturn(true, true, true, false);
    when(rs.getObject(1)).thenReturn("v");

    // maxRows=2，第三行 rowNum=3 > 2 抛错
    assertThatThrownBy(() -> new LimitRowsResultSetExtractor(2).extractData(rs))
        .isInstanceOf(BssException.class)
        .hasMessageContaining("2");
  }

  @Test
  void extractData_atMaxRowsBoundary_doesNotThrow() throws SQLException {
    ResultSet rs = mock(ResultSet.class);
    ResultSetMetaData md = mock(ResultSetMetaData.class);
    when(rs.getMetaData()).thenReturn(md);
    when(md.getColumnCount()).thenReturn(1);
    when(md.getColumnLabel(1)).thenReturn("c");
    when(rs.next()).thenReturn(true, true, false);
    when(rs.getObject(1)).thenReturn("v");

    // maxRows=2，恰好两行不抛错
    assertThat(new LimitRowsResultSetExtractor(2).extractData(rs)).hasSize(2);
  }

  // ==================== parseSqlArray（Array 列） ====================

  @Test
  void extractData_arrayColumn_parsedToList() throws SQLException {
    ResultSet rs = mock(ResultSet.class);
    ResultSetMetaData md = mock(ResultSetMetaData.class);
    when(rs.getMetaData()).thenReturn(md);
    when(md.getColumnCount()).thenReturn(1);
    when(md.getColumnLabel(1)).thenReturn("arr");
    when(rs.next()).thenReturn(true, false);

    Array array = mock(Array.class);
    ResultSet arrayRs = mock(ResultSet.class);
    when(rs.getObject(1)).thenReturn(array);
    when(array.getResultSet()).thenReturn(arrayRs);
    when(arrayRs.next()).thenReturn(true, true, false);
    when(arrayRs.getObject(2)).thenReturn("a", "b");

    List<Map<String, Object>> result = new LimitRowsResultSetExtractor().extractData(rs);

    assertThat(result.get(0)).containsEntry("arr", List.of("a", "b"));
  }

  @Test
  void extractData_nestedArray_parsedRecursively() throws SQLException {
    ResultSet rs = mock(ResultSet.class);
    ResultSetMetaData md = mock(ResultSetMetaData.class);
    when(rs.getMetaData()).thenReturn(md);
    when(md.getColumnCount()).thenReturn(1);
    when(md.getColumnLabel(1)).thenReturn("arr");
    when(rs.next()).thenReturn(true, false);

    Array outer = mock(Array.class);
    Array inner = mock(Array.class);
    ResultSet outerRs = mock(ResultSet.class);
    ResultSet innerRs = mock(ResultSet.class);
    when(rs.getObject(1)).thenReturn(outer);
    when(outer.getResultSet()).thenReturn(outerRs);
    when(outerRs.next()).thenReturn(true, false);
    when(outerRs.getObject(2)).thenReturn(inner); // 元素本身是 Array

    when(inner.getResultSet()).thenReturn(innerRs);
    when(innerRs.next()).thenReturn(true, false);
    when(innerRs.getObject(2)).thenReturn("nested");

    List<Map<String, Object>> result = new LimitRowsResultSetExtractor().extractData(rs);

    assertThat(result.get(0)).containsEntry("arr", List.of(List.of("nested")));
  }

  @Test
  void extractData_emptyArray_returnsEmptyList() throws SQLException {
    ResultSet rs = mock(ResultSet.class);
    ResultSetMetaData md = mock(ResultSetMetaData.class);
    when(rs.getMetaData()).thenReturn(md);
    when(md.getColumnCount()).thenReturn(1);
    when(md.getColumnLabel(1)).thenReturn("arr");
    when(rs.next()).thenReturn(true, false);

    Array array = mock(Array.class);
    ResultSet arrayRs = mock(ResultSet.class);
    when(rs.getObject(1)).thenReturn(array);
    when(array.getResultSet()).thenReturn(arrayRs);
    when(arrayRs.next()).thenReturn(false);

    List<Map<String, Object>> result = new LimitRowsResultSetExtractor().extractData(rs);
    assertThat(result.get(0)).containsEntry("arr", Collections.emptyList());
  }

  // ==================== 辅助 ====================

  private ResultSet emptyResultSet() {
    try {
      ResultSet rs = mock(ResultSet.class);
      ResultSetMetaData md = mock(ResultSetMetaData.class);
      when(rs.getMetaData()).thenReturn(md);
      when(md.getColumnCount()).thenReturn(0);
      when(rs.next()).thenReturn(false);
      return rs;
    }
    catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
