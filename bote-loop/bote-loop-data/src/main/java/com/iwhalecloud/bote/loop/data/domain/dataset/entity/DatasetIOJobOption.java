package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据集IO任务选项实体
 * 迁移对应关系: Go语言entity.DatasetIOJobOption
 * - 功能: 存储数据集IO任务选项信息
 * - 字段定义:
 * * OverwriteDataset: *bool - 覆盖数据集
 * <p>
 * Java实现说明:
 * - 对应Go的entity.DatasetIOJobOption结构体
 * - 使用Java类定义，包含任务选项字段
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
public class DatasetIOJobOption {

  @JsonProperty("overwrite_dataset")
  private Boolean overwriteDataset;
}
