package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工具实体
 * 迁移对应关系: Go语言Tool
 * - 功能: 工具数据结构
 * - 字段: type, function
 * <p>
 * Java实现说明:
 * - 对应Go的Tool结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go ToolType -> Java ToolType
 * - Go *Function -> Java Function
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tool {
  @JsonProperty("type")
  private ToolType type;

  @JsonProperty("function")
  private Function function;
}
