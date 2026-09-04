package com.iwhalecloud.bote.loop.evaluation.infra.pdf.module;

import lombok.Getter;
import lombok.Setter;

/**
 * 评估器指标统计
 */
@Getter
@Setter
public final class AggregateEvaluatorInfo {
  /** 评估器版本ID */
  private Long evaluatorVersionId;
  /** 评估器名称 */
  private String evaluatorName;
  /** 评估器版本 */
  private String evaluatorVersion;
  /** 实验数据通过得分 */
  private Double passScore;
  /** 实验数据通过率 */
  private Double passRate;
  /** 平均分 */
  private Double averageScore;
  /** 最小分 */
  private Double minScore;
  /** 最大分 */
  private Double maxScore;
  /** 总分 */
  private Double sumScore;
}
