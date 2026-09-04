package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字段数据实体
 * 迁移对应关系: Go语言FieldData
 * - 功能: 字段数据结构
 * - 字段: key, name, content
 * <p>
 * Java实现说明:
 * - 对应Go的FieldData结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go string -> Java String
 * - Go *Content -> Java Content
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldData {
  @JsonProperty("key")
  private String key;

  @JsonProperty("name")
  private String name;

  @JsonProperty("content")
  private Content content;
}
