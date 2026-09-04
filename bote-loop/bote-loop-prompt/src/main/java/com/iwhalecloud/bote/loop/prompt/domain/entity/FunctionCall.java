package com.iwhalecloud.bote.loop.prompt.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 函数调用实体
 * 迁移对应关系: Go语言entity.FunctionCall
 * - 功能: 存储函数调用的详细信息
 * - 字段定义:
 * * Name: string - 函数名称
 * * Arguments: *string - 函数参数
 * <p>
 * Java实现说明:
 * - 对应Go的entity.FunctionCall结构体
 * - 使用Java类定义，包含函数调用字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go string -> Java String
 * - Go指针类型 -> Java对象引用
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FunctionCall {

  /**
   * 函数名称
   * 迁移对应关系: Go语言entity.FunctionCall.Name (string)
   * - 功能: 被调用的函数名称
   * - 类型: Go的string对应Java的String
   * - 用途: 函数标识和调用
   */
  @JsonProperty("name")
  private String name;

  /**
   * 函数参数
   * 迁移对应关系: Go语言entity.FunctionCall.Arguments (*string)
   * - 功能: 函数的调用参数
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 存储函数调用的参数信息
   */
  @JsonProperty("arguments")
  private String arguments;

}
