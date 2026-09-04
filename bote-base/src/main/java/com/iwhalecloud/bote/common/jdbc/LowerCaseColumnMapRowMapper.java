package com.iwhalecloud.bote.common.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.LinkedCaseInsensitiveMap;

/**
 * 小写格式 Map 结果集
 * {@link org.springframework.jdbc.core.ColumnMapRowMapper}
 *
 * @author chen.linfa
 * @since 2024-10-22
 */
public class LowerCaseColumnMapRowMapper implements RowMapper<Map<String, Object>> {

  @Override
  public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
    ResultSetMetaData rsmd = rs.getMetaData();
    int columnCount = rsmd.getColumnCount();
    Map<String, Object> mapOfColumnValues = createColumnMap(columnCount);
    for (int i = 1; i <= columnCount; i++) {
      String column = JdbcUtils.lookupColumnName(rsmd, i);
      mapOfColumnValues.putIfAbsent(getColumnKey(column), getColumnValue(rs, i));
    }
    return mapOfColumnValues;
  }

  /**
   * Create a Map instance to be used as column map.
   * <p>By default, a linked case-insensitive Map will be created.
   *
   * @param columnCount the column count, to be used as initial capacity for the Map
   * @return the new Map instance
   * @see LinkedCaseInsensitiveMap
   */
  protected Map<String, Object> createColumnMap(int columnCount) {
    return new LinkedCaseInsensitiveMap<>(columnCount);
  }

  /**
   * Determine the key to use for the given column in the column Map.
   *
   * @param columnName the column name as returned by the ResultSet
   * @return the column key to use
   * @see ResultSetMetaData#getColumnName
   */
  protected String getColumnKey(String columnName) {
    return columnName.toLowerCase();
  }

  /**
   * Retrieve a JDBC object value for the specified column.
   * <p>The default implementation uses the {@code getObject} method.
   * Additionally, this implementation includes a "hack" to get around Oracle returning a non standard object for their TIMESTAMP datatype.
   *
   * @param rs is the ResultSet holding the data
   * @param index is the column index
   * @return the Object returned
   * @see JdbcUtils#getResultSetValue
   */
  @Nullable
  protected Object getColumnValue(ResultSet rs, int index) throws SQLException {
    return JdbcUtils.getResultSetValue(rs, index);
  }

}
