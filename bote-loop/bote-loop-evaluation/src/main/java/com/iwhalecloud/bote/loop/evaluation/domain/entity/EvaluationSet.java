package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评估集实体
 * 迁移对应关系: Go语言EvaluationSet
 * - 功能: 评估集数据结构
 * - 字段: id, appId, spaceId, name, description, status, spec, features, itemCount, changeUncommitted, evaluationSetVersion, latestVersion, nextVersionNum, baseInfo, bizCategory
 * <p>
 * Java实现说明:
 * - 对应Go的EvaluationSet结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go int64 -> Java Long
 * - Go int32 -> Java Integer
 * - Go string -> Java String
 * - Go DatasetStatus -> Java DatasetStatus
 * - Go *DatasetSpec -> Java DatasetSpec
 * - Go *DatasetFeatures -> Java DatasetFeatures
 * - Go bool -> Java Boolean
 * - Go *EvaluationSetVersion -> Java EvaluationSetVersion
 * - Go *BaseInfo -> Java BaseInfo
 * - Go BizCategory -> Java String
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationSet {
  @JsonProperty("id")
  private Long id;

  @JsonProperty("app_id")
  private Integer appId;

  @JsonProperty("space_id")
  private Long spaceId;

  @JsonProperty("name")
  private String name;

  @JsonProperty("description")
  private String description;

  @JsonProperty("status")
  private DatasetStatus status;

  @JsonProperty("spec")
  private DatasetSpec spec;

  @JsonProperty("features")
  private DatasetFeatures features;

  @JsonProperty("item_count")
  private Long itemCount;

  @JsonProperty("change_uncommitted")
  private Boolean changeUncommitted;

  @JsonProperty("evaluation_set_version")
  private EvaluationSetVersion evaluationSetVersion;

  @JsonProperty("latest_version")
  private String latestVersion;

  @JsonProperty("next_version_num")
  private Long nextVersionNum;

  @JsonProperty("base_info")
  private BaseInfo baseInfo;

  @JsonProperty("biz_category")
  private String bizCategory;
  @JsonProperty("catalog_item_id")
  private Long catalogItemId;
}
