package com.iwhalecloud.bote.loop.data.pkg.pagination;

/**
 * 分页器选项接口
 * 迁移对应关系: Go语言pagination.PaginatorOption
 * - 功能: 分页器配置选项
 * - 方法定义: 应用选项的方法
 * <p>
 * Java实现说明:
 * - 对应Go的pagination.PaginatorOption函数类型
 * - 使用Java函数式接口定义
 * - 提供分页器配置选项
 * <p>
 * 技术栈迁移:
 * - Go函数类型 -> Java函数式接口
 * - Go函数选项模式 -> Java函数式接口
 */
@FunctionalInterface
public interface PaginatorOption {
  void apply(Paginator paginator);
}
