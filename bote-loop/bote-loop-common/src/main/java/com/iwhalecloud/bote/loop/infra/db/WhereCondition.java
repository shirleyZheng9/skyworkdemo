package com.iwhalecloud.bote.loop.infra.db;

import java.util.List;

/**
 * WHERE条件
 * 迁移对应关系: Go语言clause.Expression
 * - 功能: 表示单个WHERE条件
 * - 字段定义: 列名、操作符、值等
 * <p>
 * Java实现说明:
 * - 对应Go的clause.Expression接口
 * - 使用Java类定义，包含条件信息
 * - 提供SQL生成功能
 * <p>
 * 技术栈迁移:
 * - Go接口 -> Java类
 * - Go结构体 -> Java类
 * - Go切片 -> Java列表
 */
public class WhereCondition {

  /**
   * 列名
   * 迁移对应关系: Go语言clause.Expression的Column
   * - 功能: 数据库列名
   * - 类型: Go的string对应Java的String
   * - 用途: 指定查询列
   */
  private final String column;

  /**
   * 操作符
   * 迁移对应关系: Go语言clause.Expression的Operator
   * - 功能: 比较操作符
   * - 类型: Go的string对应Java的String
   * - 用途: 指定比较操作
   */
  private final String operator;

  /**
   * 值
   * 迁移对应关系: Go语言clause.Expression的Value
   * - 功能: 比较值
   * - 类型: Go的interface{}对应Java的Object
   * - 用途: 指定比较值
   */
  private final Object value;

  /**
   * 逻辑操作符
   * 迁移对应关系: Go语言clause.Expression的LogicalOperator
   * - 功能: 逻辑连接符
   * - 类型: Go的string对应Java的String
   * - 用途: 指定逻辑连接符（AND/OR）
   */
  private final String logicalOperator;

  /**
   * 构造函数
   * 迁移对应关系: Go语言clause.Expression构造函数
   * - 功能: 创建WHERE条件
   * - 参数: column - 列名, operator - 操作符, value - 值
   * - 用途: 初始化WHERE条件
   */
  public WhereCondition(String column, String operator, Object value) {
    this(column, operator, value, "AND");
  }

  /**
   * 构造函数
   * 迁移对应关系: Go语言clause.Expression构造函数
   * - 功能: 创建WHERE条件
   * - 参数: column - 列名, operator - 操作符, value - 值, logicalOperator - 逻辑操作符
   * - 用途: 初始化WHERE条件
   */
  public WhereCondition(String column, String operator, Object value, String logicalOperator) {
    this.column = column;
    this.operator = operator;
    this.value = value;
    this.logicalOperator = logicalOperator;
  }

  /**
   * 生成SQL
   * 迁移对应关系: Go语言clause.Expression的SQL生成
   * - 功能: 生成SQL条件字符串
   * - 返回: SQL条件字符串
   * - 用途: 生成SQL WHERE子句
   */
  public String toSql() {
    if (value instanceof List) {
      List<?> values = (List<?>) value;
      if (values.isEmpty()) {
        return "";
      }

      StringBuilder sql = new StringBuilder();
      sql.append("`").append(column).append("` ");

      if ("IN".equalsIgnoreCase(operator)) {
        sql.append("IN (");
        for (int i = 0; i < values.size(); i++) {
          if (i > 0) {
            sql.append(", ");
          }
          sql.append("?");
        }
        sql.append(")");
      }
      else if ("LIKE".equalsIgnoreCase(operator)) {
        sql.append("LIKE ? ESCAPE '\\\\'");
      }
      else {
        sql.append(operator).append(" ?");
      }

      return sql.toString();
    }
    else {
      StringBuilder sql = new StringBuilder();
      sql.append("`").append(column).append("` ");

      if ("LIKE".equalsIgnoreCase(operator)) {
        sql.append("LIKE ? ESCAPE '\\\\'");
      }
      else {
        sql.append(operator).append(" ?");
      }

      return sql.toString();
    }
  }

  /**
   * 获取列名
   * 迁移对应关系: Go语言clause.Expression的Column
   * - 功能: 获取列名
   * - 返回: 列名
   * - 用途: 获取列名
   */
  public String getColumn() {
    return column;
  }

  /**
   * 获取操作符
   * 迁移对应关系: Go语言clause.Expression的Operator
   * - 功能: 获取操作符
   * - 返回: 操作符
   * - 用途: 获取操作符
   */
  public String getOperator() {
    return operator;
  }

  /**
   * 获取值
   * 迁移对应关系: Go语言clause.Expression的Value
   * - 功能: 获取值
   * - 返回: 值
   * - 用途: 获取值
   */
  public Object getValue() {
    return value;
  }

  /**
   * 获取逻辑操作符
   * 迁移对应关系: Go语言clause.Expression的LogicalOperator
   * - 功能: 获取逻辑操作符
   * - 返回: 逻辑操作符
   * - 用途: 获取逻辑操作符
   */
  public String getLogicalOperator() {
    return logicalOperator;
  }
}
