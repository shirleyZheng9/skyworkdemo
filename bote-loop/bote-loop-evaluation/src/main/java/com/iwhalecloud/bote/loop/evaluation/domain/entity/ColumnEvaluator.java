package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 列评估器
 * 对应Go: ColumnEvaluator
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ColumnEvaluator {

  /**
   * 评估器ID
   * 对应Go: EvaluatorID int64
   */
  private Long evaluatorId;

  /**
   * 评估器版本ID
   * 对应Go: EvaluatorVersionID int64
   */
  private Long evaluatorVersionId;

  /**
   * 评估器名称
   * 对应Go: Name string
   */
  private String name;

  /**
   * 评估器类型
   * 对应Go: EvaluatorType entity.EvaluatorType
   */
  private EvaluatorType evaluatorType;

  /**
   * 评估器版本
   * 对应Go: Version string
   */
  private String version;

  /**
   * 描述
   * 对应Go: Description string
   */
  private String description;

  /**
   * 创建者
   * 对应Go: CreatedBy string
   */
  private String createdBy;

  /**
   * 创建时间
   * 对应Go: CreatedAt int64
   */
  private Long createdAt;

  /**
   * 更新时间
   * 对应Go: UpdatedAt int64
   */
  private Long updatedAt;
}
