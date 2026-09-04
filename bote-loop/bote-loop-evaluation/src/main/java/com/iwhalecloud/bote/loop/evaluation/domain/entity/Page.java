package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页实体
 * 迁移对应关系: Go语言Page
 * - 功能: 分页数据结构
 * - 字段: offset, limit
 * <p>
 * Java实现说明:
 * - 对应Go的Page结构体
 * - 使用Lombok注解简化代码
 * - JSON序列化使用camelCase命名（研发规范2）
 * - 实现NewPage、Offset、Limit方法
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go int -> Java int
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Page {
  private int offset;

  private int limit;

  private static final int DEFAULT_PAGE = 1;
  private static final int DEFAULT_LIMIT = 20;
  private static final int DEFAULT_MAX_LIMIT = 200;

  /**
   * 创建新分页
   * 迁移对应关系: Go语言NewPage()
   */
  public static Page newPage(int offset, int limit) {
    if (limit <= 0) {
      limit = DEFAULT_LIMIT;
    }
    if (limit > DEFAULT_MAX_LIMIT) {
      limit = DEFAULT_MAX_LIMIT;
    }
    if (offset <= 0) {
      offset = DEFAULT_PAGE;
    }

    return Page.builder()
      .offset(offset)
      .limit(limit)
      .build();
  }

  /**
   * 获取偏移量
   * 迁移对应关系: Go语言Page.Offset()
   */
  public int getOffset() {
    return (this.offset - 1) * this.limit;
  }

  /**
   * 获取限制
   * 迁移对应关系: Go语言Page.Limit()
   */
  public int getLimit() {
    return this.limit;
  }
}
