package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新实验聚合结果参数实体
 * 迁移对应关系: Go语言UpdateExptAggrResultParam
 * - 功能: 更新实验聚合结果参数数据结构
 * - 字段: spaceId, experimentId, fieldType, fieldKey
 * <p>
 * Java实现说明:
 * - 对应Go的UpdateExptAggrResultParam结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go int64 -> Java Long
 * - Go FieldType -> Java FieldType
 * - Go string -> Java String
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateExptAggrResultParam {
  @JsonProperty("space_id")
  private Long spaceId;

  @JsonProperty("experiment_id")
  private Long experimentId;

  @JsonProperty("field_type")
  private FieldType fieldType;

  @JsonProperty("field_key")
  private String fieldKey;
}
