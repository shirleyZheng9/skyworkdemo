package com.iwhalecloud.bote.loop.prompt.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 函数实体
 * 迁移对应关系: Go语言entity.Function
 * - 功能: 定义工具函数的信息
 * - 字段定义:
 * * Name: string - 函数名称
 * * Description: string - 函数描述
 * * Parameters: string - 函数参数
 * <p>
 * Java实现说明:
 * - 对应Go的entity.Function结构体
 * - 使用Java类定义，包含函数相关字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go string -> Java String
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Function {

  /**
   * 函数名称
   * 迁移对应关系: Go语言entity.Function.Name (string)
   * - 功能: 函数的名称
   * - 类型: Go的string对应Java的String
   * - 用途: 函数标识和调用
   */
  @JsonProperty("name")
  private String name;

  /**
   * 函数描述
   * 迁移对应关系: Go语言entity.Function.Description (string)
   * - 功能: 函数的描述信息
   * - 类型: Go的string对应Java的String
   * - 用途: 函数说明和文档
   */
  @JsonProperty("description")
  private String description;

  /**
   * 函数参数
   * 迁移对应关系: Go语言entity.Function.Parameters (string)
   * - 功能: 函数的参数定义
   * - 类型: Go的string对应Java的String
   * - 用途: 参数验证和调用
   */
  @JsonProperty("parameters")
  private String parameters;
}
