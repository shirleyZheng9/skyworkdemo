package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 排序实体
 * 迁移对应关系: Go语言OrderBy
 * - 功能: 排序数据结构
 * - 字段: field, isAsc
 * <p>
 * Java实现说明:
 * - 对应Go的OrderBy结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go *string -> Java String
 * - Go *bool -> Java Boolean
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderBy {
  @JsonProperty("field")
  private String field;

  @JsonProperty("is_asc")
  private Boolean isAsc;
}
