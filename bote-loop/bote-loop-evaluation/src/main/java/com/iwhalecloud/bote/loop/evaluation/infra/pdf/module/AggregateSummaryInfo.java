package com.iwhalecloud.bote.loop.evaluation.infra.pdf.module;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AggregateSummaryInfo {
  /** 评测集大小 */
  private Long evalSetSize;
  /** 实验数据通过得分 */
  private double passScore;
  /** 实验数据通过率 */
  private double passRate;
}
