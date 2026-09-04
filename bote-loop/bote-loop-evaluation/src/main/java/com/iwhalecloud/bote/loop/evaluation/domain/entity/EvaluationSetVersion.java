package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评估集版本实体
 * 迁移对应关系: Go语言EvaluationSetVersion
 * - 功能: 评估集版本数据结构
 * - 字段: id, appId, spaceId, evaluationSetId, version, versionNum, description, evaluationSetSchema, itemCount, baseInfo
 * <p>
 * Java实现说明:
 * - 对应Go的EvaluationSetVersion结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go int64 -> Java Long
 * - Go int32 -> Java Integer
 * - Go string -> Java String
 * - Go *EvaluationSetSchema -> Java EvaluationSetSchema
 * - Go *BaseInfo -> Java BaseInfo
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationSetVersion {
  @JsonProperty("id")
  private Long id;

  @JsonProperty("app_id")
  private Integer appId;

  @JsonProperty("space_id")
  private Long spaceId;

  @JsonProperty("evaluation_set_id")
  private Long evaluationSetId;

  @JsonProperty("version")
  private String version;

  @JsonProperty("version_num")
  private Long versionNum;

  @JsonProperty("description")
  private String description;

  @JsonProperty("evaluation_set_schema")
  private EvaluationSetSchema evaluationSetSchema;

  @JsonProperty("item_count")
  private Long itemCount;

  @JsonProperty("base_info")
  private BaseInfo baseInfo;
}
