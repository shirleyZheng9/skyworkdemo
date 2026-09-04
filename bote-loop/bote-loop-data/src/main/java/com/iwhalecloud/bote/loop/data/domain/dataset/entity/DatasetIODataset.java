package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据集IO数据集实体
 * 迁移对应关系: Go语言entity.DatasetIODataset
 * - 功能: 存储数据集IO数据集信息
 * - 字段定义:
 * * SpaceID: *int64 - 空间ID
 * * DatasetID: int64 - 数据集ID
 * * VersionID: *int64 - 版本ID
 * <p>
 * Java实现说明:
 * - 对应Go的entity.DatasetIODataset结构体
 * - 使用Java类定义，包含IO数据集字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go指针类型 -> Java对象引用
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatasetIODataset {

  @JsonProperty("space_id")
  private Long spaceId;

  @JsonProperty("dataset_id")
  private Long datasetId;

  @JsonProperty("version_id")
  private Long versionId;
}
