package com.iwhalecloud.bote.loop.prompt.domain.repo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页查询提交版本参数
 * 迁移对应关系: Go语言ListCommitParam
 * - 功能: 分页查询提交版本的参数类
 * - 字段定义:
 * * promptId: Long - Prompt ID
 * * cursor: Long - 游标
 * * limit: Integer - 限制数量
 * * asc: Boolean - 是否升序
 * <p>
 * Java实现说明:
 * - 对应Go的ListCommitParam结构体
 * - 使用Lombok注解简化代码
 * - 用于分页查询提交版本时的参数传递
 * <p>
 * 技术栈迁移:
 * - Go int64 -> Java Long
 * - Go *int64 -> Java Long
 * - Go int -> Java Integer
 * - Go bool -> Java Boolean
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListCommitParam {

  /**
   * Prompt ID
   * 迁移对应关系: Go语言ListCommitParam.PromptID
   * - 功能: Prompt标识
   * - 类型: Go的int64对应Java的Long
   */
  private Long promptId;

  /**
   * 游标
   * 迁移对应关系: Go语言ListCommitParam.Cursor
   * - 功能: 分页游标
   * - 类型: Go的*int64对应Java的Long
   */
  private Long cursor;

  /**
   * 限制数量
   * 迁移对应关系: Go语言ListCommitParam.Limit
   * - 功能: 分页限制数量
   * - 类型: Go的int对应Java的Integer
   */
  private Integer limit;

  /**
   * 是否升序
   * 迁移对应关系: Go语言ListCommitParam.Asc
   * - 功能: 排序方向
   * - 类型: Go的bool对应Java的Boolean
   */
  private Boolean asc;
}
