package com.iwhalecloud.bote.loop.prompt.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工具调用实体
 * 迁移对应关系: Go语言entity.ToolCall
 * - 功能: 存储工具调用的信息
 * - 字段定义:
 * * Index: int64 - 调用索引
 * * ID: string - 调用ID
 * * Type: ToolType - 工具类型
 * * FunctionCall: *FunctionCall - 函数调用
 * <p>
 * Java实现说明:
 * - 对应Go的entity.ToolCall结构体
 * - 使用Java类定义，包含工具调用字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go int64 -> Java Long
 * - Go string -> Java String
 * - Go枚举类型 -> Java枚举
 * - Go指针类型 -> Java对象引用
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolCall {

  /**
   * 调用索引
   * 迁移对应关系: Go语言entity.ToolCall.Index (int64)
   * - 功能: 工具调用的索引
   * - 类型: Go的int64对应Java的Long
   * - 用途: 调用顺序和标识
   */
  @JsonProperty("index")
  private Long index;

  /**
   * 调用ID
   * 迁移对应关系: Go语言entity.ToolCall.ID (string)
   * - 功能: 工具调用的唯一标识
   * - 类型: Go的string对应Java的String
   * - 用途: 调用追踪和关联
   */
  @JsonProperty("id")
  private String id;

  /**
   * 工具类型
   * 迁移对应关系: Go语言entity.ToolCall.Type (ToolType)
   * - 功能: 工具的类型标识
   * - 类型: Go的枚举类型对应Java的枚举
   * - 用途: 区分不同类型的工具调用
   */
  @JsonProperty("type")
  private ToolType type;

  /**
   * 函数调用
   * 迁移对应关系: Go语言entity.ToolCall.FunctionCall (*FunctionCall)
   * - 功能: 具体的函数调用信息
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 存储函数调用的详细信息
   */
  @JsonProperty("function_call")
  private FunctionCall functionCall;

}
