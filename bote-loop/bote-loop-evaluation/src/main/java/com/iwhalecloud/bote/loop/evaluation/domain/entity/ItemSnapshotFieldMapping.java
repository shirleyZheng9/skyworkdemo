package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项快照字段映射实体
 * 迁移对应关系: Go语言ItemSnapshotFieldMapping
 * - 功能: 项快照字段映射数据结构
 * - 字段: fieldKey, mappingKey, mappingSubKey
 * <p>
 * Java实现说明:
 * - 对应Go的ItemSnapshotFieldMapping结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go string -> Java String
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemSnapshotFieldMapping {
  @JsonProperty("field_key")
  private String fieldKey;

  @JsonProperty("mapping_key")
  private String mappingKey;

  @JsonProperty("mapping_sub_key")
  private String mappingSubKey;
}
