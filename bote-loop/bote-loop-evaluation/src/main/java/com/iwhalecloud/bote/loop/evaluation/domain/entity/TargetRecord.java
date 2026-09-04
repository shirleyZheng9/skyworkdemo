package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评估对象记录实体
 * 迁移对应关系: Go语言TargetRecord
 * - 功能: 评估对象记录数据结构
 * - 字段: id, spaceId, targetId, targetVersionId, name, description, input, expectedOutput, createdBy, updatedBy, createdAt, updatedAt
 * <p>
 * Java实现说明:
 * - 对应Go的TargetRecord结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go int64 -> Java Long
 * - Go string -> Java String
 * - Go time.Time -> Java LocalDateTime
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TargetRecord {
  @JsonProperty("id")
  private Long id;

  @JsonProperty("space_id")
  private Long spaceId;

  @JsonProperty("target_id")
  private Long targetId;

  @JsonProperty("target_version_id")
  private Long targetVersionId;

  @JsonProperty("name")
  private String name;

  @JsonProperty("description")
  private String description;

  @JsonProperty("input")
  private String input;

  @JsonProperty("expected_output")
  private String expectedOutput;

  @JsonProperty("created_by")
  private String createdBy;

  @JsonProperty("updated_by")
  private String updatedBy;

  @JsonProperty("created_at")
  private java.time.LocalDateTime createdAt;

  @JsonProperty("updated_at")
  private java.time.LocalDateTime updatedAt;
}
