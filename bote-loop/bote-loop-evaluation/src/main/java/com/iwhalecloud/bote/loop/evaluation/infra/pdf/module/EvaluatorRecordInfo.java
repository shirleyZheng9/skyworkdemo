package com.iwhalecloud.bote.loop.evaluation.infra.pdf.module;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EvaluatorRecordInfo {
  /** 评估器 */
  private String evaluator;
  /** 状态 */
  private String status;
  /** 是否通过 */
  private boolean passFlag;
  /** 得分 */
  private Double score;
  /** 得分原因 */
  private String reason;
}
