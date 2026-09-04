package com.iwhalecloud.bote.loop.evaluation.domain.repo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项目快照过滤器实体
 * 迁移对应关系: Go语言ItemSnapshotFilter
 * - 功能: 项目快照过滤条件
 * - 字段: boolMapFilters, floatMapFilters, intMapFilters, stringMapFilters
 * <p>
 * Java实现说明:
 * - 对应Go的ItemSnapshotFilter结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go []*FieldFilter -> Java List<FieldFilter>
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemSnapshotFilter {
  @JsonProperty("bool_map_filters")
  private List<FieldFilter> boolMapFilters;

  @JsonProperty("float_map_filters")
  private List<FieldFilter> floatMapFilters;

  @JsonProperty("int_map_filters")
  private List<FieldFilter> intMapFilters;

  @JsonProperty("string_map_filters")
  private List<FieldFilter> stringMapFilters;
}
