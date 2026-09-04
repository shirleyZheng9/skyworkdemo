package com.iwhalecloud.bote.loop.evaluation.domain.repo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验轮次结果过滤器映射条件实体
 * 迁移对应关系: Go语言ExptTurnResultFilterMapCond
 * - 功能: 实验轮次结果过滤器映射条件
 * - 字段: evalTargetDataFilters, evaluatorScoreFilters, annotationFloatFilters, annotationBoolFilters, annotationStringFilters
 * <p>
 * Java实现说明:
 * - 对应Go的ExptTurnResultFilterMapCond结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go []*FieldFilter -> Java List<FieldFilter>
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExptTurnResultFilterMapCond {
  @JsonProperty("eval_target_data_filters")
  private List<FieldFilter> evalTargetDataFilters;

  @JsonProperty("evaluator_score_filters")
  private List<FieldFilter> evaluatorScoreFilters;

  @JsonProperty("annotation_float_filters")
  private List<FieldFilter> annotationFloatFilters;

  @JsonProperty("annotation_bool_filters")
  private List<FieldFilter> annotationBoolFilters;

  @JsonProperty("annotation_string_filters")
  private List<FieldFilter> annotationStringFilters;
}
