package com.iwhalecloud.bote.loop.evaluation.domain.repo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字段过滤器实体
 * 迁移对应关系: Go语言FieldFilter
 * - 功能: 用于描述map字段的筛选条件
 * - 字段: key, op, values
 * - 操作符: =, >, >=, <, <=, BETWEEN, LIKE
 * <p>
 * Java实现说明:
 * - 对应Go的FieldFilter结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go string -> Java String
 * - Go []any -> Java List<Object>
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldFilter {
  @JsonProperty("key")
  private String key;

  @JsonProperty("op")
  private String op;

  @JsonProperty("values")
  private List<Object> values;
}
