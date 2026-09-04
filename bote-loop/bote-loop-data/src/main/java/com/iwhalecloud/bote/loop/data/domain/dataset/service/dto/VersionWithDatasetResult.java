package com.iwhalecloud.bote.loop.data.domain.dataset.service.dto;

import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetVersion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 版本化数据集和模式DTO
 * 迁移对应关系: Go语言service.VersionedDatasetWithSchema
 * - 功能: 存储版本化数据集和模式信息
 * - 字段定义: 版本和数据集模式
 * <p>
 * Java实现说明:
 * - 对应Go的service.VersionedDatasetWithSchema结构体
 * - 使用Java类定义，包含版本化数据集字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go指针类型 -> Java对象引用
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VersionWithDatasetResult {

  private DatasetVersion version;
  private DatasetWithSchema dataset;
}
