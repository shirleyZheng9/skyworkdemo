package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字段配置实体
 * 迁移对应关系: Go语言FieldConf
 * - 功能: 字段配置数据结构
 * - 字段: fieldName, fromField, value
 * <p>
 * Java实现说明:
 * - 对应Go的FieldConf结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go string -> Java String
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldConf {
  @JsonProperty("field_name")
  private String fieldName;

  @JsonProperty("from_field")
  private String fromField;

  @JsonProperty("value")
  private String value;
}
