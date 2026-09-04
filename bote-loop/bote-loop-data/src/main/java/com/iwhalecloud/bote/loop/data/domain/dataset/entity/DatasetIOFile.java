package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据集IO文件实体
 * 迁移对应关系: Go语言entity.DatasetIOFile
 * - 功能: 存储数据集IO文件信息
 * - 字段定义:
 * * Provider: entity.Provider - 存储提供商
 * * Path: string - 文件路径
 * * Format: *FileFormat - 数据文件的格式
 * * CompressFormat: *FileFormat - 压缩包格式
 * * Files: []string - 数据文件列表
 * <p>
 * Java实现说明:
 * - 对应Go的entity.DatasetIOFile结构体
 * - 使用Java类定义，包含IO文件字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go指针类型 -> Java对象引用
 * - Go切片 -> Java List
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatasetIOFile {

  @JsonProperty("provider")
  private String provider;

  @JsonProperty("path")
  private String path;

  @JsonProperty("format")
  private FileFormat format;

  @JsonProperty("compress_format")
  private FileFormat compressFormat;

  @JsonProperty("files")
  private List<String> files;
}
