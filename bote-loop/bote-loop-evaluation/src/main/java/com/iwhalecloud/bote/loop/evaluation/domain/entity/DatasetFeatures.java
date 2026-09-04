package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据集特性实体
 * 迁移对应关系: Go语言DatasetFeatures
 * - 功能: 数据集特性数据结构
 * - 字段: editSchema, repeatedData, multiModal
 * <p>
 * Java实现说明:
 * - 对应Go的DatasetFeatures结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go bool -> Java Boolean
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatasetFeatures {
  @JsonProperty("editSchema")
  private Boolean editSchema;

  @JsonProperty("repeatedData")
  private Boolean repeatedData;

  @JsonProperty("multiModal")
  private Boolean multiModal;
}
