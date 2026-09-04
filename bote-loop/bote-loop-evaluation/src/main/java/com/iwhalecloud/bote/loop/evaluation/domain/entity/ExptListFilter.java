package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验列表过滤器实体
 * 迁移对应关系: Go语言ExptListFilter
 * - 功能: 实验列表过滤条件
 * - 字段: fuzzyName, includes, excludes
 * <p>
 * Java实现说明:
 * - 对应Go的ExptListFilter结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go string -> Java String
 * - Go *ExptFilterFields -> Java ExptFilterFields
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExptListFilter {
  @JsonProperty("fuzzy_name")
  private String fuzzyName;

  @JsonProperty("includes")
  private ExptFilterFields includes;

  @JsonProperty("excludes")
  private ExptFilterFields excludes;
}
