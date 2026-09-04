package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验聚合结果实体
 * 迁移对应关系: Go语言ExptAggrResult
 * - 功能: 实验聚合结果数据结构
 * - 字段: id, spaceId, experimentId, fieldType, fieldKey, score, aggrResult, version, status
 * <p>
 * Java实现说明:
 * - 对应Go的ExptAggrResult结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go int64 -> Java Long
 * - Go int32 -> Java Integer
 * - Go string -> Java String
 * - Go float64 -> Java Double
 * - Go []byte -> Java byte[]
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExptAggrResult {
  @JsonProperty("id")
  private Long id;

  @JsonProperty("space_id")
  private Long spaceId;

  @JsonProperty("experiment_id")
  private Long experimentId;

  @JsonProperty("field_type")
  private Integer fieldType;

  @JsonProperty("field_key")
  private String fieldKey;

  @JsonProperty("score")
  private Double score;

  @JsonProperty("aggr_result")
  private String aggrResult;

  @JsonProperty("version")
  private Long version;

  @JsonProperty("status")
  private Integer status;
}
