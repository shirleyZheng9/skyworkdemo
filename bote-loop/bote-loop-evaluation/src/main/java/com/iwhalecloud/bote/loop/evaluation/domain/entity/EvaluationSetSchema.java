package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评估集模式实体
 * 迁移对应关系: Go语言EvaluationSetSchema
 * - 功能: 评估集模式数据结构
 * - 字段: id, appId, spaceId, evaluationSetId, fieldSchemas, baseInfo
 * <p>
 * Java实现说明:
 * - 对应Go的EvaluationSetSchema结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go int64 -> Java Long
 * - Go int32 -> Java Integer
 * - Go []*FieldSchema -> Java List<FieldSchema>
 * - Go *BaseInfo -> Java BaseInfo
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationSetSchema {
  @JsonProperty("id")
  private Long id;

  @JsonProperty("app_id")
  private Integer appId;

  @JsonProperty("space_id")
  private Long spaceId;

  @JsonProperty("evaluation_set_id")
  private Long evaluationSetId;

  @JsonProperty("field_schemas")
  private List<FieldSchema> fieldSchemas;

  @JsonProperty("base_info")
  private BaseInfo baseInfo;
}
