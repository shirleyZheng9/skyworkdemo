package com.iwhalecloud.bote.common.jdbc;

import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.LinkedCaseInsensitiveMap;

/**
 * 限制最大返回行数的查询结果集提取器
 *
 * <p>限制最大行数以避免查询结果占用过大内存，保护系统整体性能</p>
 *
 * @author bianjp
 * @see org.springframework.jdbc.core.RowMapperResultSetExtractor
 * @see org.springframework.jdbc.core.ColumnMapRowMapper
 * @since 2024-08-19
 */
public class LimitRowsResultSetExtractor implements ResultSetExtractor<List<Map<String, Object>>> {
  /** 默认最大行数限制 */
  public static final int DEFAULT_MAX_ROWS = 10000;

  /** 最大行数 */
  private final int maxRows;
  /** 字段名列表。用于规范查询结果中的字段名大小写（不同数据库类型返回的大小写不一致） */
  private final List<String> columnNames;

  /**
   * 构造结果集提取器，使用默认最大行数限制
   */
  public LimitRowsResultSetExtractor() {
    this(DEFAULT_MAX_ROWS, null);
  }

  /**
   * 构造结果集提取器
   */
  public LimitRowsResultSetExtractor(int maxRows) {
    this(maxRows, null);
  }

  public LimitRowsResultSetExtractor(int maxRows, @Nullable List<String> columnNames) {
    this.maxRows = maxRows;
    this.columnNames = CollectionUtils.isEmpty(columnNames) ? null : columnNames;
  }

  @Override
  public List<Map<String, Object>> extractData(ResultSet rs) throws SQLException {
    ResultSetMetaData metaData = rs.getMetaData();
    int columnCount = metaData.getColumnCount();
    // 列名数组，索引与列编号对应（忽略第一个元素）
    // 不直接使用 ColumnMapRowMapper 以避免循环内重复查找列名或列编号的开销
    String[] columnLabels = extractColumnLabels(columnCount, metaData);

    List<Map<String, Object>> records = new ArrayList<>();
    int rowNum = 0;
    Map<String, Object> map;
    while (rs.next()) {
      rowNum++;
      if (rowNum > maxRows) {
        throw new BssException("查询返回行数超出最大限制 " + maxRows);
      }
      map = new LinkedCaseInsensitiveMap<>(columnCount);
      for (int i = 1; i <= columnCount; i++) {
        Object value = JdbcUtils.getResultSetValue(rs, i);
        if (value instanceof Array) {
          value = parseSqlArray((Array) value);
        }
        map.putIfAbsent(columnLabels[i], value);
      }
      records.add(map);
    }
    return records;
  }

  /**
   * 提取字段名列表，并规范字段名的大小写
   */
  private String[] extractColumnLabels(int columnCount, ResultSetMetaData metaData) throws SQLException {
    String[] columnLabels = new String[columnCount + 1];
    if (columnNames == null) {
      for (int i = 1; i <= columnCount; i++) {
        // 未指定时使用小写形式
        columnLabels[i] = metaData.getColumnLabel(i).toLowerCase();
      }
    }
    else {
      String label;
      for (int i = 1; i <= columnCount; i++) {
        // 默认使用小写形式
        label = metaData.getColumnLabel(i).toLowerCase();
        for (String columnName : columnNames) {
          if (columnName.equalsIgnoreCase(label)) {
            label = columnName;
          }
        }
        columnLabels[i] = label;
      }
    }
    return columnLabels;
  }

  /**
   * 解析 SQL 数组
   */
  private List<Object> parseSqlArray(Array array) throws SQLException {
    List<Object> values = new ArrayList<>();
    try (ResultSet rs = array.getResultSet()) {
      while (rs.next()) {
        Object value = JdbcUtils.getResultSetValue(rs, 2);
        // 支持嵌套数组
        if (value instanceof Array) {
          value = parseSqlArray((Array) value);
        }
        values.add(value);
      }
    }
    return values;
  }
}
