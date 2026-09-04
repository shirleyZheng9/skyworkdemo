package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 参数实体
 * 迁移对应关系: Go语言Param
 * - 功能: 参数数据结构
 * - 字段: name, type, description, required, defaultValue, enumValues, properties, items, additionalProperties
 * <p>
 * Java实现说明:
 * - 对应Go的Param结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go string -> Java String
 * - Go bool -> Java Boolean
 * - Go []string -> Java List<String>
 * - Go map[string]*Param -> Java Map<String, Param>
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Param {
  @JsonProperty("name")
  private String name;

  @JsonProperty("type")
  private String type;

  @JsonProperty("description")
  private String description;

  @JsonProperty("required")
  private Boolean required;

  @JsonProperty("default_value")
  private String defaultValue;

  @JsonProperty("enum_values")
  private List<String> enumValues;

  @JsonProperty("properties")
  private Map<String, Param> properties;

  @JsonProperty("items")
  private Param items;

  @JsonProperty("additional_properties")
  private Boolean additionalProperties;
}
