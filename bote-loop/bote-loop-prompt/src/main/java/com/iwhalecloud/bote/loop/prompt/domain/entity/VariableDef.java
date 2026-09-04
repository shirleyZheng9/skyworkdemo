package com.iwhalecloud.bote.loop.prompt.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 变量定义实体
 * 迁移对应关系: Go语言entity.VariableDef
 * - 功能: 定义模板变量的信息
 * - 字段定义:
 * * Key: string - 变量键
 * * Desc: string - 变量描述
 * * Type: VariableType - 变量类型
 * <p>
 * Java实现说明:
 * - 对应Go的entity.VariableDef结构体
 * - 使用Java类定义，包含变量定义字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go string -> Java String
 * - Go枚举类型 -> Java枚举
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariableDef {

  /**
   * 变量键
   * 迁移对应关系: Go语言entity.VariableDef.Key (string)
   * - 功能: 变量的唯一标识
   * - 类型: Go的string对应Java的String
   * - 用途: 变量引用和查找
   */
  @JsonProperty("key")
  private String key;

  /**
   * 变量描述
   * 迁移对应关系: Go语言entity.VariableDef.Desc (string)
   * - 功能: 变量的描述信息
   * - 类型: Go的string对应Java的String
   * - 用途: 变量说明和文档
   */
  @JsonProperty("desc")
  private String desc;

  /**
   * 变量类型
   * 迁移对应关系: Go语言entity.VariableDef.Type (VariableType)
   * - 功能: 变量的类型标识
   * - 类型: Go的枚举类型对应Java的枚举
   * - 用途: 类型检查和验证
   */
  @JsonProperty("type")
  private VariableType type;

}
