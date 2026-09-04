package com.iwhalecloud.bote.loop.prompt.domain.repo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 列表查询提交信息参数
 * 迁移对应关系: Go语言repo.ListCommitInfoParam
 * - 功能: 定义列表查询提交信息的参数
 * - 字段定义:
 * * PromptID: int64 - Prompt ID
 * * PageSize: int - 页大小
 * * PageToken: *int64 - 分页令牌
 * * Asc: bool - 是否升序
 * <p>
 * Java实现说明:
 * - 对应Go的repo.ListCommitInfoParam结构体
 * - 使用Java类定义，包含查询参数字段
 * - 使用Lombok注解简化代码
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go int64 -> Java Long
 * - Go *int64 -> Java Long (可空)
 * - Go int -> Java Integer
 * - Go bool -> Java Boolean
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListCommitInfoParam {

  /**
   * Prompt ID
   * 迁移对应关系: Go语言repo.ListCommitInfoParam.PromptID (int64)
   * - 功能: 要查询的Prompt标识
   * - 类型: Go的int64对应Java的Long
   * - 用途: 定位要查询的Prompt
   */
  private Long promptId;

  /**
   * 页大小
   * 迁移对应关系: Go语言repo.ListCommitInfoParam.PageSize (int)
   * - 功能: 每页记录数
   * - 类型: Go的int对应Java的Integer
   * - 用途: 分页查询
   */
  private Integer pageSize;

  /**
   * 分页令牌
   * 迁移对应关系: Go语言repo.ListCommitInfoParam.PageToken (*int64)
   * - 功能: 分页查询的令牌
   * - 类型: Go的*int64对应Java的Long (可空)
   * - 用途: 游标分页
   */
  private Long pageToken;

  /**
   * 是否升序
   * 迁移对应关系: Go语言repo.ListCommitInfoParam.Asc (bool)
   * - 功能: 排序方向
   * - 类型: Go的bool对应Java的Boolean
   * - 用途: 排序控制
   */
  private Boolean asc;

}
