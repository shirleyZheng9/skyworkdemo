package com.iwhalecloud.bote.loop.prompt.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 调试上下文实体
 * 迁移对应关系: Go语言entity.DebugContext
 * - 功能: 调试上下文的数据结构
 * - 字段定义:
 * * PromptID: int64 - Prompt ID
 * * UserID: string - 用户ID
 * * DebugCore: *DebugCore - 调试核心
 * * DebugConfig: *DebugConfig - 调试配置
 * * CompareConfig: *CompareConfig - 比较配置
 * <p>
 * Java实现说明:
 * - 对应Go的entity.DebugContext结构体
 * - 使用Java类定义，包含所有调试上下文字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * - 包含嵌套的复杂对象
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go int64 -> Java Long
 * - Go string -> Java String
 * - Go指针类型 -> Java对象引用
 * - Go切片类型 -> Java List
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DebugContext {

  /**
   * Prompt ID
   * 迁移对应关系: Go语言entity.DebugContext.PromptID (int64)
   * - 功能: 关联的Prompt标识
   * - 类型: Go的int64对应Java的Long
   * - 用途: 关联Prompt和调试上下文
   */
  @JsonProperty("prompt_id")
  private Long promptId;

  /**
   * 用户ID
   * 迁移对应关系: Go语言entity.DebugContext.UserID (string)
   * - 功能: 用户标识
   * - 类型: Go的string对应Java的String
   * - 用途: 用户隔离和权限控制
   */
  @JsonProperty("user_id")
  private String userId;

  /**
   * 调试核心
   * 迁移对应关系: Go语言entity.DebugContext.DebugCore (*DebugCore)
   * - 功能: 调试的核心配置和数据
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 存储调试的核心信息
   */
  @JsonProperty("debug_core")
  private DebugCore debugCore;

  /**
   * 调试配置
   * 迁移对应关系: Go语言entity.DebugContext.DebugConfig (*DebugConfig)
   * - 功能: 调试的配置信息
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 存储调试配置参数
   */
  @JsonProperty("debug_config")
  private DebugConfig debugConfig;

  /**
   * 比较配置
   * 迁移对应关系: Go语言entity.DebugContext.CompareConfig (*CompareConfig)
   * - 功能: 比较调试的配置信息
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 存储比较调试配置
   */
  @JsonProperty("compare_config")
  private CompareConfig compareConfig;
}
