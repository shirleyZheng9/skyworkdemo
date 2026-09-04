package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.FieldTransformationConfigDTO;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.FieldDisplayFormat;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.FieldStatus;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.MultiModalSpec;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字段模式实体
 * 迁移对应关系: Go语言FieldSchema
 * - 功能: 字段模式数据结构
 * - 字段: key, name, description, contentType, defaultDisplayFormat, status, textSchema, multiModelSpec, hidden, isRequired, defaultTransformations
 * <p>
 * Java实现说明:
 * - 对应Go的FieldSchema结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go string -> Java String
 * - Go ContentType -> Java ContentType
 * - Go FieldDisplayFormat -> Java FieldDisplayFormat
 * - Go FieldStatus -> Java FieldStatus
 * - Go *MultiModalSpec -> Java MultiModalSpec
 * - Go bool -> Java Boolean
 * - Go []*FieldTransformationConfig -> Java List<FieldTransformationConfig>
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldSchema {
  @JsonProperty("key")
  private String key;

  @JsonProperty("name")
  private String name;

  @JsonProperty("description")
  private String description;

  @JsonProperty("content_type")
  private ContentType contentType;

  @JsonProperty("default_display_format")
  private FieldDisplayFormat defaultDisplayFormat;

  @JsonProperty("status")
  private FieldStatus status;

  @JsonProperty("text_schema")
  private String textSchema;

  @JsonProperty("multi_model_spec")
  private MultiModalSpec multiModelSpec;

  @JsonProperty("hidden")
  private Boolean hidden;

  @JsonProperty("is_required")
  private Boolean isRequired;

  @JsonProperty("default_transformations")
  private List<FieldTransformationConfigDTO> defaultTransformations;
}
