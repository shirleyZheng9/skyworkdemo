package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据集规格实体
 * 迁移对应关系: Go语言entity.DatasetSpec
 * - 功能: 存储数据集的规格信息
 * - 字段定义:
 * * MaxItemCount: int64 - 条数上限
 * * MaxFieldCount: int32 - 字段上限
 * * MaxItemSize: int64 - 单条数据字数上限
 * <p>
 * Java实现说明:
 * - 对应Go的entity.DatasetSpec结构体
 * - 使用Java类定义，包含规格字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go int64 -> Java Long
 * - Go int32 -> Java Integer
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
}
