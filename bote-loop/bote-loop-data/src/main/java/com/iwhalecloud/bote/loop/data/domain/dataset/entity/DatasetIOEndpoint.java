package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据集IO端点实体
 * 迁移对应关系: Go语言entity.DatasetIOEndpoint
 * - 功能: 存储数据集IO端点信息
 * - 字段定义:
 * * File: *DatasetIOFile - 文件端点
 * * Dataset: *DatasetIODataset - 数据集端点
 * <p>
 * Java实现说明:
 * - 对应Go的entity.DatasetIOEndpoint结构体
 * - 使用Java类定义，包含IO端点字段
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
public class DatasetIOEndpoint {

  @JsonProperty("file")
  private DatasetIOFile file;

  @JsonProperty("dataset")
  private DatasetIODataset dataset;
}
