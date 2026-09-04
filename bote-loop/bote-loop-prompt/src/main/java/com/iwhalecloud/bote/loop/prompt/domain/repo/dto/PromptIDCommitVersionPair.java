package com.iwhalecloud.bote.loop.prompt.domain.repo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PromptID和提交版本对
 * 迁移对应关系: Go语言PromptIDCommitVersionPair
 * - 功能: 用于批量查询提交版本的参数类
 * - 字段定义:
 * * promptId: Long - Prompt ID
 * * commitVersion: String - 提交版本
 * <p>
 * Java实现说明:
 * - 对应Go的PromptIDCommitVersionPair结构体
 * - 使用Lombok注解简化代码
 * - 用于批量查询提交版本时的参数传递
 * <p>
 * 技术栈迁移:
 * - Go int64 -> Java Long
 * - Go string -> Java String
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromptIDCommitVersionPair {

  /**
   * Prompt ID
   * 迁移对应关系: Go语言PromptIDCommitVersionPair.PromptID
   * - 功能: Prompt标识
   * - 类型: Go的int64对应Java的Long
   */
  private Long promptId;

  /**
   * 提交版本
   * 迁移对应关系: Go语言PromptIDCommitVersionPair.CommitVersion
   * - 功能: 提交版本号
   * - 类型: Go的string对应Java的String
   */
  private String commitVersion;
}
