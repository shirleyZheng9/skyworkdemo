package com.iwhalecloud.bote.loop.prompt.domain.repo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PromptID和UserID对
 * 迁移对应关系: Go语言PromptIDUserIDPair
 * - 功能: 用于批量查询的参数类
 * - 字段定义:
 * * promptId: Long - Prompt ID
 * * userId: String - 用户ID
 * <p>
 * Java实现说明:
 * - 对应Go的PromptIDUserIDPair结构体
 * - 使用Lombok注解简化代码
 * - 用于批量查询时的参数传递
 * <p>
 * 技术栈迁移:
 * - Go int64 -> Java Long
 * - Go string -> Java String
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromptIDUserIDPair {

  /**
   * Prompt ID
   * 迁移对应关系: Go语言PromptIDUserIDPair.PromptID
   * - 功能: Prompt标识
   * - 类型: Go的int64对应Java的Long
   */
  private Long promptId;

  /**
   * 用户ID
   * 迁移对应关系: Go语言PromptIDUserIDPair.UserID
   * - 功能: 用户标识
   * - 类型: Go的string对应Java的String
   */
  private String userId;
}
