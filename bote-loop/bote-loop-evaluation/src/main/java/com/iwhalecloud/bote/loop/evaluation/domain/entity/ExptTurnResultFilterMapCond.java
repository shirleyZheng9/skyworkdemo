package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验轮次结果过滤器Map条件实体
 * 对应Go: entity.ExptTurnResultFilterMapCond
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExptTurnResultFilterMapCond {

  /**
   * 评估目标数据过滤器列表
   * 对应Go: EvalTargetDataFilters []*FieldFilter
   */
  private List<FieldFilterDO> evalTargetDataFilters;

  /**
   * 评估器分数过滤器列表
   * 对应Go: EvaluatorScoreFilters []*FieldFilter
   */
  private List<FieldFilterDO> evaluatorScoreFilters;

  /**
   * 标注浮点数过滤器列表
   * 对应Go: AnnotationFloatFilters []*FieldFilter
   */
  private List<FieldFilterDO> annotationFloatFilters;

  /**
   * 标注布尔值过滤器列表
   * 对应Go: AnnotationBoolFilters []*FieldFilter
   */
  private List<FieldFilterDO> annotationBoolFilters;

  /**
   * 标注字符串过滤器列表
   * 对应Go: AnnotationStringFilters []*FieldFilter
   */
  private List<FieldFilterDO> annotationStringFilters;
}
