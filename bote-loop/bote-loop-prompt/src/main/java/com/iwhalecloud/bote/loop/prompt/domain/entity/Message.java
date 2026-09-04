package com.iwhalecloud.bote.loop.prompt.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息实体
 * 迁移对应关系: Go语言entity.Message
 * - 功能: 存储对话消息信息
 * - 字段定义:
 * * Role: Role - 角色类型
 * * ReasoningContent: *string - 推理内容
 * * Content: *string - 内容
 * * Parts: []*ContentPart - 内容部分
 * * ToolCallID: *string - 工具调用ID
 * * ToolCalls: []*ToolCall - 工具调用列表
 * <p>
 * Java实现说明:
 * - 对应Go的entity.Message结构体
 * - 使用Java类定义，包含消息相关字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go指针类型 -> Java对象引用
 * - Go切片类型 -> Java List
 * - Go枚举类型 -> Java枚举
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

  /**
   * 角色类型
   * 迁移对应关系: Go语言entity.Message.Role (Role)
   * - 功能: 消息的角色标识
   * - 类型: Go的枚举类型对应Java的枚举
   * - 用途: 区分消息来源角色
   */
  @JsonProperty("role")
  private Role role;

  /**
   * 推理内容
   * 迁移对应关系: Go语言entity.Message.ReasoningContent (*string)
   * - 功能: 推理过程的内容
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 存储AI推理过程
   */
  @JsonProperty("reasoning_content")
  private String reasoningContent;

  /**
   * 内容
   * 迁移对应关系: Go语言entity.Message.Content (*string)
   * - 功能: 消息的主要内容
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 存储消息文本内容
   */
  @JsonProperty("content")
  private String content;

  /**
   * 内容部分
   * 迁移对应关系: Go语言entity.Message.Parts ([]*ContentPart)
   * - 功能: 消息的内容部分列表
   * - 类型: Go的切片类型对应Java的List
   * - 用途: 支持多模态内容
   */
  @JsonProperty("parts")
  private List<ContentPart> parts;

  /**
   * 工具调用ID
   * 迁移对应关系: Go语言entity.Message.ToolCallID (*string)
   * - 功能: 关联的工具调用ID
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 关联工具调用结果
   */
  @JsonProperty("tool_call_id")
  private String toolCallId;

  /**
   * 工具调用列表
   * 迁移对应关系: Go语言entity.Message.ToolCalls ([]*ToolCall)
   * - 功能: 消息中的工具调用列表
   * - 类型: Go的切片类型对应Java的List
   * - 用途: 存储工具调用请求
   */
  @JsonProperty("tool_calls")
  private List<ToolCall> toolCalls;
}
