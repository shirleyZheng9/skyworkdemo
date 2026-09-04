package com.iwhalecloud.bote.loop.prompt.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 回复实体
 * 迁移对应关系: Go语言entity.Reply
 * - 功能: 存储Prompt执行的回复信息
 * - 字段定义:
 * * Item: *ReplyItem - 回复项
 * * DebugID: int64 - 调试ID
 * * DebugStep: int32 - 调试步骤
 * * DebugTraceKey: string - 调试追踪键
 * <p>
 * Java实现说明:
 * - 对应Go的entity.Reply结构体
 * - 使用Java类定义，包含回复相关字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go int64 -> Java Long
 * - Go int32 -> Java Integer
 * - Go string -> Java String
 * - Go指针类型 -> Java对象引用
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reply {

  /**
   * 回复项
   * 迁移对应关系: Go语言entity.Reply.Item (*ReplyItem)
   * - 功能: 回复的具体内容项
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 存储回复的详细信息
   */
  @JsonProperty("item")
  private ReplyItem item;

  /**
   * 调试ID
   * 迁移对应关系: Go语言entity.Reply.DebugID (int64)
   * - 功能: 调试会话的唯一标识
   * - 类型: Go的int64对应Java的Long
   * - 用途: 调试追踪和关联
   */
  @JsonProperty("debug_id")
  private Long debugId;

  /**
   * 调试步骤
   * 迁移对应关系: Go语言entity.Reply.DebugStep (int32)
   * - 功能: 调试执行的步骤编号
   * - 类型: Go的int32对应Java的Integer
   * - 用途: 调试步骤追踪
   */
  @JsonProperty("debug_step")
  private Integer debugStep;

  /**
   * 调试追踪键
   * 迁移对应关系: Go语言entity.Reply.DebugTraceKey (string)
   * - 功能: 调试追踪的键值
   * - 类型: Go的string对应Java的String
   * - 用途: 调试链路追踪
   */
  @JsonProperty("debug_trace_key")
  private String debugTraceKey;
}
