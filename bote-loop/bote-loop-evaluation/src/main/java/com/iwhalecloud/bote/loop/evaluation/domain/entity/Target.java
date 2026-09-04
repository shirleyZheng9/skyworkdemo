package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评估对象实体
 * 迁移对应关系: Go语言Target
 * - 功能: 评估对象核心数据结构
 * - 字段: id, spaceId, sourceTargetId, targetType, createdBy, updatedBy, createdAt, updatedAt, deletedAt
 * <p>
 * Java实现说明:
 * - 对应Go的Target结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go int64 -> Java Long
 * - Go string -> Java String
 * - Go time.Time -> Java LocalDateTime
 * - Go soft_delete.DeletedAt -> Java Long
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Target {
  @JsonProperty("id")
  private Long id;

  @JsonProperty("space_id")
  private Long spaceId;

  @JsonProperty("source_target_id")
  private String sourceTargetId;

  @JsonProperty("target_type")
  private Integer targetType;

  @JsonProperty("created_by")
  private String createdBy;

  @JsonProperty("updated_by")
  private String updatedBy;

  @JsonProperty("created_at")
  private java.time.LocalDateTime createdAt;

  @JsonProperty("updated_at")
  private java.time.LocalDateTime updatedAt;

  @JsonProperty("deleted_at")
  private Long deletedAt;
}
