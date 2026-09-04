package com.iwhalecloud.bote.loop.infra.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * WHERE条件构建器
 * 迁移对应关系: Go语言db.WhereBuilder
 * - 功能: 构建SQL WHERE条件
 * - 字段定义: 索引标志和条件列表
 * <p>
 * Java实现说明:
 * - 对应Go的db.WhereBuilder结构体
 * - 使用Java类定义，包含WHERE条件构建功能
 * - 提供各种条件构建方法
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go切片 -> Java列表
 * - Go接口 -> Java接口
 * - Go泛型 -> Java泛型
 */
public class WhereBuilder {

  /**
   * 是否使用索引
   * 迁移对应关系: Go语言WhereBuilder.withIndex
   * - 功能: 标记是否使用了索引字段
   * - 类型: Go的bool对应Java的boolean
   * - 用途: 确保查询使用索引
   */
  private boolean withIndex;

  /**
   * WHERE条件列表
   * 迁移对应关系: Go语言WhereBuilder.where
   * - 功能: 存储WHERE条件表达式
   * - 类型: Go的*clause.Where对应Java的List<WhereCondition>
   * - 用途: 存储所有WHERE条件
   */
  private final List<WhereCondition> conditions;

  /**
   * 构造函数
   * 迁移对应关系: Go语言NewWhereBuilder
   * - 功能: 创建新的WHERE构建器
   * - 返回: WHERE构建器实例
   * - 用途: 初始化WHERE构建器
   */
  public WhereBuilder() {
    this.conditions = new ArrayList<>();
    this.withIndex = false;
  }

  /**
   * 添加等值或IN条件
   * 迁移对应关系: Go语言WhereBuilder.EqOrIn
   * - 功能: 根据值数量添加等值或IN条件
   * - 参数: column - 列名, values - 值列表
   * - 用途: 智能选择等值或IN条件
   */
  public void eqOrIn(String column, Object... values) {
    if (values == null || values.length == 0) {
      return;
    }

    if (values.length == 1) {
      addEq(column, values[0]);
    }
    else {
      addIn(column, List.of(values));
    }
  }

  /**
   * 添加等值条件
   * 迁移对应关系: Go语言WhereBuilder.AddWhere
   * - 功能: 添加等值条件
   * - 参数: column - 列名, value - 值
   * - 用途: 添加等值查询条件
   */
  public void addEq(String column, Object value) {
    if (value == null) {
      return;
    }
    conditions.add(new WhereCondition(column, "=", value));
  }

  /**
   * 添加等值条件（带索引标志）
   * 迁移对应关系: Go语言WhereBuilder.AddWhere
   * - 功能: 添加等值条件
   * - 参数: column - 列名, value - 值, withIndex - 是否使用索引
   * - 用途: 添加等值查询条件
   */
  public void addEq(String column, Object value, boolean withIndex) {
    if (value == null) {
      return;
    }
    conditions.add(new WhereCondition(column, "=", value));
    if (withIndex) {
      this.withIndex = true;
    }
  }

  /**
   * 添加IN条件
   * 迁移对应关系: Go语言WhereBuilder.AddWhere
   * - 功能: 添加IN条件
   * - 参数: column - 列名, values - 值列表
   * - 用途: 添加IN查询条件
   */
  public void addIn(String column, List<?> values) {
    if (values == null || values.isEmpty()) {
      return;
    }
    conditions.add(new WhereCondition(column, "IN", values));
  }

  /**
   * 添加大于条件
   * 迁移对应关系: Go语言WhereBuilder.AddWhere
   * - 功能: 添加大于条件
   * - 参数: column - 列名, value - 值
   * - 用途: 添加大于查询条件
   */
  public void addGt(String column, Object value) {
    if (value == null) {
      return;
    }
    conditions.add(new WhereCondition(column, ">", value));
  }

  /**
   * 添加小于等于条件
   * 迁移对应关系: Go语言WhereBuilder.AddWhere
   * - 功能: 添加小于等于条件
   * - 参数: column - 列名, value - 值
   * - 用途: 添加小于等于查询条件
   */
  public void addLte(String column, Object value) {
    if (value == null) {
      return;
    }
    conditions.add(new WhereCondition(column, "<=", value));
  }

  /**
   * 添加LIKE条件
   * 迁移对应关系: Go语言WhereBuilder.AddWhere
   * - 功能: 添加LIKE条件
   * - 参数: column - 列名, value - 值
   * - 用途: 添加模糊查询条件
   */
  public void addLike(String column, String value) {
    if (value == null || value.isEmpty()) {
      return;
    }
    String escapedValue = escapeLikeWildcard(value);
    conditions.add(new WhereCondition(column, "LIKE", "%" + escapedValue + "%"));
  }

  /**
   * 添加多值LIKE条件
   * 迁移对应关系: Go语言WhereBuilder.AddWhere
   * - 功能: 添加多值LIKE条件（OR连接）
   * - 参数: column - 列名, values - 值列表
   * - 用途: 添加多值模糊查询条件
   */
  public void addMultiLike(String column, List<String> values) {
    if (values == null || values.isEmpty()) {
      return;
    }

    List<String> filteredValues = values.stream()
      .filter(Objects::nonNull)
      .filter(s -> !s.isEmpty())
      .toList();

    if (filteredValues.isEmpty()) {
      return;
    }

    for (String value : filteredValues) {
      String escapedValue = escapeLikeWildcard(value);
      conditions.add(new WhereCondition(column, "LIKE", "%" + escapedValue + "%", "OR"));
    }
  }

  /**
   * 添加自定义条件
   * 迁移对应关系: Go语言WhereBuilder.AddWhere
   * - 功能: 添加自定义WHERE条件
   * - 参数: condition - 条件对象
   * - 用途: 添加自定义查询条件
   */
  public void addWhere(WhereCondition condition) {
    if (condition != null) {
      conditions.add(condition);
    }
  }

  /**
   * 设置使用索引
   * 迁移对应关系: Go语言WhereBuilder.WithIndex
   * - 功能: 标记使用了索引字段
   * - 用途: 确保查询使用索引
   */
  public void withIndex() {
    this.withIndex = true;
  }

  /**
   * 构建WHERE条件字符串
   * 迁移对应关系: Go语言WhereBuilder.Build
   * - 功能: 构建最终的WHERE条件字符串
   * - 返回: WHERE条件字符串
   * - 用途: 生成SQL WHERE子句
   */
  public String build() {
    if (!withIndex) {
      throw new IllegalArgumentException("at least one of the query params using index must be set");
    }

    if (conditions.isEmpty()) {
      return "";
    }

    StringBuilder sql = new StringBuilder();
    for (int i = 0; i < conditions.size(); i++) {
      WhereCondition condition = conditions.get(i);

      if (i > 0) {
        String operator = condition.getLogicalOperator();
        if (operator == null || operator.isEmpty()) {
          operator = "AND";
        }
        sql.append(" ").append(operator).append(" ");
      }

      sql.append(condition.toSql());
    }

    return sql.toString();
  }

  /**
   * 转义LIKE通配符
   * 迁移对应关系: Go语言escapeLikeWildcard
   * - 功能: 转义LIKE子句中的通配符
   * - 参数: s - 原始字符串
   * - 返回: 转义后的字符串
   * - 用途: 防止LIKE查询中的通配符被误用
   */
  private String escapeLikeWildcard(String s) {
    if (s == null) {
      return "";
    }
    return s.replace("%", "\\%").replace("_", "\\_");
  }

  /**
   * 获取条件数量
   * 迁移对应关系: Go语言WhereBuilder条件数量
   * - 功能: 获取当前条件数量
   * - 返回: 条件数量
   * - 用途: 检查是否有条件
   */
  public int size() {
    return conditions.size();
  }

  /**
   * 是否为空
   * 迁移对应关系: Go语言WhereBuilder是否为空
   * - 功能: 检查是否有条件
   * - 返回: 是否为空
   * - 用途: 检查是否有条件
   */
  public boolean isEmpty() {
    return conditions.isEmpty();
  }
}
