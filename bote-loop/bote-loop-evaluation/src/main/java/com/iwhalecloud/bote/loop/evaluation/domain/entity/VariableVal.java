package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 变量值结构体
 * 迁移对应关系: Go语言VariableVal
 * - 功能: 变量值数据结构
 * - 字段: key, value, placeholderMessages
 * <p>
 * Java实现说明:
 * - 对应Go的VariableVal结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go *string -> Java String
 * - Go []*Message -> Java List<Message>
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariableVal {
  @JsonProperty("key")
  private String key;

  @JsonProperty("value")
  private String value;

  @JsonProperty("placeholderMessages")
  private List<Message> placeholderMessages;
}
