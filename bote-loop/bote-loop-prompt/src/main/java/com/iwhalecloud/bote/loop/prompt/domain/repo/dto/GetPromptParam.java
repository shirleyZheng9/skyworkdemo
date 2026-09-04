package com.iwhalecloud.bote.loop.prompt.domain.repo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取Prompt参数
 * 迁移对应关系: Go语言repo.GetPromptParam
 * - 功能: 定义获取Prompt的查询参数
 * - 字段定义:
 * * PromptID: int64 - Prompt ID
 * * WithCommit: bool - 是否包含提交信息
 * * CommitVersion: string - 提交版本
 * * WithDraft: bool - 是否包含草稿
 * * UserID: string - 用户ID
 * <p>
 * Java实现说明:
 * - 对应Go的repo.GetPromptParam结构体
 * - 使用Java类定义，包含查询参数字段
 * - 使用Lombok注解简化代码
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go int64 -> Java Long
 * - Go bool -> Java Boolean
 * - Go string -> Java String
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetPromptParam {

  /**
   * Prompt ID
   * 迁移对应关系: Go语言repo.GetPromptParam.PromptID (int64)
   * - 功能: Prompt的唯一标识
   * - 类型: Go的int64对应Java的Long
   * - 用途: 精确查询指定Prompt
   */
  private Long promptId;

  /**
   * 是否包含提交信息
   * 迁移对应关系: Go语言repo.GetPromptParam.WithCommit (bool)
   * - 功能: 是否包含提交信息
   * - 类型: Go的bool对应Java的Boolean
   * - 用途: 控制是否加载提交信息
   */
  private Boolean withCommit;

  /**
   * 提交版本
   * 迁移对应关系: Go语言repo.GetPromptParam.CommitVersion (string)
   * - 功能: 指定提交版本
   * - 类型: Go的string对应Java的String
   * - 用途: 查询特定版本的提交信息
   */
  private String commitVersion;

  /**
   * 是否包含草稿
   * 迁移对应关系: Go语言repo.GetPromptParam.WithDraft (bool)
   * - 功能: 是否包含草稿信息
   * - 类型: Go的bool对应Java的Boolean
   * - 用途: 控制是否加载草稿信息
   */
  private Boolean withDraft;

  /**
   * 用户ID
   * 迁移对应关系: Go语言repo.GetPromptParam.UserID (string)
   * - 功能: 用户标识
   * - 类型: Go的string对应Java的String
   * - 用途: 权限控制和草稿查询
   */
  private String userId;

  /**
   * 空间ID
   * - 功能: 空间标识
   * - 类型: Long
   * - 用途: 双主键查询，用于锁定唯一数据
   */
  private Long spaceId;
}
