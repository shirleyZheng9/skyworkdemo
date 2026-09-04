package com.iwhalecloud.bote.loop.prompt.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工具实体
 * 迁移对应关系: Go语言entity.Tool
 * - 功能: 定义可调用的工具
 * - 字段定义:
 * * Type: ToolType - 工具类型
 * * Function: *Function - 函数定义
 * <p>
 * Java实现说明:
 * - 对应Go的entity.Tool结构体
 * - 使用Java类定义，包含工具相关字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go枚举类型 -> Java枚举
 * - Go指针类型 -> Java对象引用
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tool {

  /**
   * 工具类型
   * 迁移对应关系: Go语言entity.Tool.Type (ToolType)
   * - 功能: 工具的类型标识
   * - 类型: Go的枚举类型对应Java的枚举
   * - 用途: 区分不同类型的工具
   */
  @JsonProperty("type")
  private ToolType type;

  /**
   * 函数定义
   * 迁移对应关系: Go语言entity.Tool.Function (*Function)
   * - 功能: 工具的函数定义
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 定义工具的具体功能
   */
  @JsonProperty("function")
  private Function function;
}
