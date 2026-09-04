package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据集规格实体
 * 迁移对应关系: Go语言DatasetSpec
 * - 功能: 数据集规格数据结构
 * - 字段: maxItemCount, maxFieldCount, maxItemSize, maxItemDataNestedDepth
 * <p>
 * Java实现说明:
 * - 对应Go的DatasetSpec结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go int64 -> Java Long
 * - Go int32 -> Java Integer
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatasetSpec {
  @JsonProperty("max_item_count")
  private Long maxItemCount;

  @JsonProperty("max_field_count")
  private Integer maxFieldCount;

  @JsonProperty("max_item_size")
  private Long maxItemSize;

  @JsonProperty("max_item_data_nested_depth")
  private Integer maxItemDataNestedDepth;
}
