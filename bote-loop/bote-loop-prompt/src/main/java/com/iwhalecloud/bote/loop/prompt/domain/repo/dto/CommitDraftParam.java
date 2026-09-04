package com.iwhalecloud.bote.loop.prompt.domain.repo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 提交草稿参数
 * 迁移对应关系: Go语言repo.CommitDraftParam
 * - 功能: 定义提交草稿的参数
 * - 字段定义:
 * * PromptID: int64 - Prompt ID
 * * UserID: string - 用户ID
 * * CommitVersion: string - 提交版本
 * * CommitDescription: string - 提交描述
 * <p>
 * Java实现说明:
 * - 对应Go的repo.CommitDraftParam结构体
 * - 使用Java类定义，包含提交参数字段
 * - 使用Lombok注解简化代码
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go int64 -> Java Long
 * - Go string -> Java String
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommitDraftParam {

  /**
   * Prompt ID
   * 迁移对应关系: Go语言repo.CommitDraftParam.PromptID (int64)
   * - 功能: 要提交的Prompt标识
   * - 类型: Go的int64对应Java的Long
   * - 用途: 定位要提交的Prompt
   */
  private Long promptId;

  /**
   * 用户ID
   * 迁移对应关系: Go语言repo.CommitDraftParam.UserID (string)
   * - 功能: 执行提交的用户标识
   * - 类型: Go的string对应Java的String
   * - 用途: 权限控制和审计
   */
  private String userId;

  /**
   * 提交版本
   * 迁移对应关系: Go语言repo.CommitDraftParam.CommitVersion (string)
   * - 功能: 提交的版本号
   * - 类型: Go的string对应Java的String
   * - 用途: 版本管理
   */
  private String commitVersion;

  /**
   * 提交描述
   * 迁移对应关系: Go语言repo.CommitDraftParam.CommitDescription (string)
   * - 功能: 提交的描述信息
   * - 类型: Go的string对应Java的String
   * - 用途: 提交说明
   */
  private String commitDescription;

  /**
   * 空间ID
   * - 功能: 空间标识
   * - 类型: Long
   * - 用途: 双主键查询，用于锁定唯一数据
   */
  private Long spaceId;

}
