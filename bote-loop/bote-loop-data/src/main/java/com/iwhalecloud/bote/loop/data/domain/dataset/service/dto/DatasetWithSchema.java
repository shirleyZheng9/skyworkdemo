package com.iwhalecloud.bote.loop.data.domain.dataset.service.dto;

import com.iwhalecloud.bote.loop.data.domain.dataset.entity.Dataset;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetSchema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 带模式的数据集
 * 迁移对应关系: Go语言service.DatasetWithSchema
 * - 功能: 包含数据集和其模式信息的组合对象
 * - 字段定义: 数据集对象和模式对象
 * <p>
 * Java实现说明:
 * - 对应Go的service.DatasetWithSchema结构体
 * - 使用Lombok注解简化代码
 * - 提供数据集和模式的组合访问
 * - 支持Builder模式构建对象
 * - 支持有参和无参构造器
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go指针 -> Java对象引用
 * - Go嵌入结构体 -> Java组合
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatasetWithSchema {

  /**
   * 数据集对象
   * 迁移对应关系: Go语言DatasetWithSchema.*entity.Dataset
   * - 功能: 数据集的基本信息
   * - 类型: *entity.Dataset -> Dataset
   * - 用途: 数据集核心数据
   */
  private Dataset dataset;

  /**
   * 数据集模式
   * 迁移对应关系: Go语言DatasetWithSchema.Schema
   * - 功能: 数据集的模式定义
   * - 类型: *entity.DatasetSchema -> DatasetSchema
   * - 用途: 数据集结构定义
   */
  private DatasetSchema schema;
}
