package com.iwhalecloud.bote.loop.evaluation.infra.pdf.module;

import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScoreDistributionInfo implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  /** 得分 */
  private String score;
  /** 百分比 */
  private Double percentage;
  /** 数量 */
  private Long count;
}
