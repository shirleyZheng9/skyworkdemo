package com.iwhalecloud.bote.loop.prompt.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 变量值实体
 * 迁移对应关系: Go语言entity.VariableVal
 * - 功能: 存储变量的值信息
 * - 字段定义:
 * * Key: string - 变量键
 * * Value: *string - 变量值
 * * PlaceholderMessages: []*Message - 占位符消息
 * <p>
 * Java实现说明:
 * - 对应Go的entity.VariableVal结构体
 * - 使用Java类定义，包含变量值字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go string -> Java String
 * - Go指针类型 -> Java对象引用
 * - Go切片类型 -> Java List
 */
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariableVal {

  /**
   * 变量键
   * 迁移对应关系: Go语言entity.VariableVal.Key (string)
   * - 功能: 变量的唯一标识
   * - 类型: Go的string对应Java的String
   * - 用途: 变量引用和查找
   */
  private String key;

  /**
   * 变量值
   * 迁移对应关系: Go语言entity.VariableVal.Value (*string)
   * - 功能: 变量的值
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 存储变量的实际值
   */
  private String value;

  /**
   * 占位符消息
   * 迁移对应关系: Go语言entity.VariableVal.PlaceholderMessages ([]*Message)
   * - 功能: 占位符类型的消息列表
   * - 类型: Go的切片类型对应Java的List
   * - 用途: 存储占位符消息内容
   */
  @JsonProperty("placeholder_messages")
  private List<Message> placeholderMessages;
}
