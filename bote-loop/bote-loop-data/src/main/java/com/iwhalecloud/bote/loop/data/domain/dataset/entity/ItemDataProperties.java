package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项目数据属性实体
 * 迁移对应关系: Go语言entity.ItemDataProperties
 * - 功能: 存储项目数据属性信息
 * - 字段定义:
 * * Storage: entity.Provider - 外部存储，为空表示存在 RDS 中
 * * StorageKey: string - 外部存储 key
 * * CompressFormat: string - 压缩格式, 为空表示未压缩
 * * Bytes: int64 - 字节数
 * * Runes: int64 - 字符数
 * <p>
 * Java实现说明:
 * - 对应Go的entity.ItemDataProperties结构体
 * - 使用Java类定义，包含项目数据属性字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go枚举类型 -> Java枚举
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemDataProperties {

  @JsonProperty("storage")
  private String storage;

  @JsonProperty("storage_key")
  private String storageKey;

  @JsonProperty("compress_format")
  private String compressFormat;

  @JsonProperty("bytes")
  private Long bytes;

  @JsonProperty("runes")
  private Long runes;
}
