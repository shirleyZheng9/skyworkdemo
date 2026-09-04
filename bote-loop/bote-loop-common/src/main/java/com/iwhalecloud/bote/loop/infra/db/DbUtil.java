package com.iwhalecloud.bote.loop.infra.db;

import java.util.List;

/**
 * 数据库工具类
 * 迁移对应关系: Go语言db包中的工具方法
 * - 功能: 提供数据库操作工具方法
 * - 方法定义: 各种WHERE条件构建方法
 * <p>
 * Java实现说明:
 * - 对应Go的db包中的工具方法
 * - 使用Java静态方法提供工具功能
 * - 提供WHERE条件构建功能
 * <p>
 * 技术栈迁移:
 * - Go方法 -> Java静态方法
 * - Go切片 -> Java列表
 * - Go指针 -> Java对象引用
 */
public final class DbUtil {

  private DbUtil() {
    // 工具类，禁止实例化
  }

  /**
   * 可能添加等值条件
   * 迁移对应关系: Go语言db.MaybeAddEqToWhere
   * - 功能: 如果值不为空则添加等值条件
   * - 参数: builder - 条件构建器, value - 值, column - 列名, withIndex - 是否使用索引
   * - 用途: 构建等值查询条件
   */
  public static void maybeAddEqToWhere(WhereBuilder builder, Object value, String column, boolean withIndex) {
    if (value != null) {
      builder.addEq(column, value, withIndex);
    }
  }

  /**
   * 可能添加等值条件（不使用索引）
   * 迁移对应关系: Go语言db.MaybeAddEqToWhere
   * - 功能: 如果值不为空则添加等值条件
   * - 参数: builder - 条件构建器, value - 值, column - 列名
   * - 用途: 构建等值查询条件
   */
  public static void maybeAddEqToWhere(WhereBuilder builder, Object value, String column) {
    maybeAddEqToWhere(builder, value, column, false);
  }

  /**
   * 可能添加IN条件
   * 迁移对应关系: Go语言db.MaybeAddInToWhere
   * - 功能: 如果列表不为空则添加IN条件
   * - 参数: builder - 条件构建器, values - 值列表, column - 列名
   * - 用途: 构建IN查询条件
   */
  public static void maybeAddInToWhere(WhereBuilder builder, List<?> values, String column) {
    if (values != null && !values.isEmpty()) {
      builder.addIn(column, values);
    }
  }

  /**
   * 可能添加大于条件
   * 迁移对应关系: Go语言db.MaybeAddGtToWhere
   * - 功能: 如果值不为空则添加大于条件
   * - 参数: builder - 条件构建器, value - 值, column - 列名
   * - 用途: 构建大于查询条件
   */
  public static void maybeAddGtToWhere(WhereBuilder builder, Object value, String column) {
    if (value != null) {
      builder.addGt(column, value);
    }
  }

  /**
   * 可能添加小于等于条件
   * 迁移对应关系: Go语言db.MaybeAddLteToWhere
   * - 功能: 如果值不为空则添加小于等于条件
   * - 参数: builder - 条件构建器, value - 值, column - 列名
   * - 用途: 构建小于等于查询条件
   */
  public static void maybeAddLteToWhere(WhereBuilder builder, Object value, String column) {
    if (value != null) {
      builder.addLte(column, value);
    }
  }

  /**
   * 可能添加LIKE条件
   * 迁移对应关系: Go语言db.MaybeAddLikeToWhere
   * - 功能: 如果值不为空则添加LIKE条件
   * - 参数: builder - 条件构建器, value - 值, column - 列名
   * - 用途: 构建模糊查询条件
   */
  public static void maybeAddLikeToWhere(WhereBuilder builder, String value, String column) {
    if (value != null && !value.isEmpty()) {
      builder.addLike(column, value);
    }
  }

  /**
   * 可能添加多值LIKE条件
   * 迁移对应关系: Go语言db.MaybeAddMultiLikeToWhere
   * - 功能: 如果列表不为空则添加多值LIKE条件
   * - 参数: builder - 条件构建器, values - 值列表, column - 列名
   * - 用途: 构建多值模糊查询条件
   */
  public static void maybeAddMultiLikeToWhere(WhereBuilder builder, List<String> values, String column) {
    if (values != null && !values.isEmpty()) {
      builder.addMultiLike(column, values);
    }
  }

  /**
   * 创建WHERE构建器
   * 迁移对应关系: Go语言db.NewWhereBuilder
   * - 功能: 创建新的WHERE构建器
   * - 返回: WHERE构建器实例
   * - 用途: 创建WHERE构建器
   */
  public static WhereBuilder newWhereBuilder() {
    return new WhereBuilder();
  }

  /**
   * 设置使用索引
   * 迁移对应关系: Go语言db.WhereWithIndex
   * - 功能: 设置使用索引
   * - 参数: builder - 条件构建器
   * - 用途: 标记使用索引
   */
  public static void whereWithIndex(WhereBuilder builder) {
    if (builder != null) {
      builder.withIndex();
    }
  }
}
