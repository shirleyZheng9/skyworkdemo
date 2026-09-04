package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工具调用实体
 * 迁移对应关系: Go语言ToolCall
 * - 功能: 工具调用数据结构
 * - 字段: index, id, type, functionCall
 * <p>
 * Java实现说明:
 * - 对应Go的ToolCall结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go int64 -> Java Long
 * - Go string -> Java String
 * - Go ToolType -> Java ToolType
 * - Go *FunctionCall -> Java FunctionCall
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolCall {
  @JsonProperty("index")
  private Long index;

  @JsonProperty("id")
  private String id;

  @JsonProperty("type")
  private ToolType type;

  @JsonProperty("function_call")
  private FunctionCall functionCall;
}
