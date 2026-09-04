package com.iwhalecloud.bote.loop.prompt.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 调试消息实体
 * 迁移对应关系: Go语言entity.DebugMessage
 * - 功能: 调试消息的数据结构
 * - 字段定义:
 * * Role: Role - 角色
 * * ReasoningContent: *string - 推理内容
 * * Content: *string - 内容
 * * Parts: []*ContentPart - 内容部分
 * * ToolCallID: *string - 工具调用ID
 * * ToolCalls: []*DebugToolCall - 工具调用
 * * DebugID: *string - 调试ID
 * * InputTokens: *int64 - 输入Token数
 * * OutputTokens: *int64 - 输出Token数
 * * CostMS: *int64 - 耗时(毫秒)
 * <p>
 * Java实现说明:
 * - 对应Go的entity.DebugMessage结构体
 * - 使用Java类定义，包含调试消息字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go *string -> Java String (可空)
 * - Go *int64 -> Java Long (可空)
 * - Go切片类型 -> Java List
 * - Go指针类型 -> Java对象引用
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DebugMessage {

  /**
   * 角色
   * 迁移对应关系: Go语言entity.DebugMessage.Role (Role)
   * - 功能: 消息的角色类型
   * - 类型: Go的Role枚举对应Java的Role枚举
   * - 用途: 标识消息的角色
   */
  @JsonProperty("role")
  private Role role;

  /**
   * 推理内容
   * 迁移对应关系: Go语言entity.DebugMessage.ReasoningContent (*string)
   * - 功能: 推理过程的内容
   * - 类型: Go的*string对应Java的String (可空)
   * - 用途: 存储推理过程
   */
  @JsonProperty("reasoning_content")
  private String reasoningContent;

  /**
   * 内容
   * 迁移对应关系: Go语言entity.DebugMessage.Content (*string)
   * - 功能: 消息的主要内容
   * - 类型: Go的*string对应Java的String (可空)
   * - 用途: 存储消息内容
   */
  @JsonProperty("content")
  private String content;

  /**
   * 内容部分
   * 迁移对应关系: Go语言entity.DebugMessage.Parts ([]*ContentPart)
   * - 功能: 消息的内容部分列表
   * - 类型: Go的切片类型对应Java的List
   * - 用途: 存储多模态内容
   */
  @JsonProperty("parts")
  private List<ContentPart> parts;

  /**
   * 工具调用ID
   * 迁移对应关系: Go语言entity.DebugMessage.ToolCallID (*string)
   * - 功能: 工具调用的标识
   * - 类型: Go的*string对应Java的String (可空)
   * - 用途: 关联工具调用
   */
  @JsonProperty("tool_call_id")
  private String toolCallId;

  /**
   * 工具调用
   * 迁移对应关系: Go语言entity.DebugMessage.ToolCalls ([]*DebugToolCall)
   * - 功能: 调试工具调用列表
   * - 类型: Go的切片类型对应Java的List
   * - 用途: 存储工具调用信息
   */
  @JsonProperty("tool_calls")
  private List<DebugToolCall> toolCalls;

  /**
   * 调试ID
   * 迁移对应关系: Go语言entity.DebugMessage.DebugID (*string)
   * - 功能: 调试会话的标识
   * - 类型: Go的*string对应Java的String (可空)
   * - 用途: 关联调试会话
   */
  @JsonProperty("debug_id")
  private String debugId;

  /**
   * 输入Token数
   * 迁移对应关系: Go语言entity.DebugMessage.InputTokens (*int64)
   * - 功能: 输入内容的Token数量
   * - 类型: Go的*int64对应Java的Long (可空)
   * - 用途: 成本计算
   */
  @JsonProperty("input_tokens")
  private Long inputTokens;

  /**
   * 输出Token数
   * 迁移对应关系: Go语言entity.DebugMessage.OutputTokens (*int64)
   * - 功能: 输出内容的Token数量
   * - 类型: Go的*int64对应Java的Long (可空)
   * - 用途: 成本计算
   */
  @JsonProperty("output_tokens")
  private Long outputTokens;

  /**
   * 耗时(毫秒)
   * 迁移对应关系: Go语言entity.DebugMessage.CostMS (*int64)
   * - 功能: 处理耗时
   * - 类型: Go的*int64对应Java的Long (可空)
   * - 用途: 性能分析
   */
  @JsonProperty("cost_ms")
  private Long costMS;
}
