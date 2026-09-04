package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 多模态规格实体
 * 迁移对应关系: Go语言entity.MultiModalSpec
 * - 功能: 存储多模态规格限制信息
 * - 字段定义:
 * * MaxFileCount: int64 - 文件数量上限
 * * MaxFileSize: int64 - 文件大小上限
 * * SupportedFormats: []string - 文件格式
 * <p>
 * Java实现说明:
 * - 对应Go的entity.MultiModalSpec结构体
 * - 使用Java类定义，包含多模态规格字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go切片 -> Java List
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MultiModalSpec {

  @JsonProperty("max_file_count")
  private Long maxFileCount;

  @JsonProperty("max_file_size")
  private Long maxFileSize;

  @JsonProperty("supported_formats")
  private List<String> supportedFormats;
}
