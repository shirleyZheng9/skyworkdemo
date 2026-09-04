package com.iwhalecloud.bote.loop.data.pkg.pagination;

/**
 * 分页常量
 * 迁移对应关系: Go语言pagination包常量
 * - 功能: 定义分页相关的常量
 * - 字段定义: 各种分页常量
 * <p>
 * Java实现说明:
 * - 对应Go的pagination包常量
 * - 使用Java类定义，包含分页常量
 * - 提供常量值访问
 * <p>
 * 技术栈迁移:
 * - Go常量 -> Java常量
 * - Go包级别常量 -> Java类常量
 */
public class PaginationConstants {

  public static final String COLUMN_UPDATED_AT = "updated_at";
  public static final String COLUMN_CREATED_AT = "created_at";
  public static final String COLUMN_ID = "id";
  public static final int DEFAULT_LIMIT = 10;
}
