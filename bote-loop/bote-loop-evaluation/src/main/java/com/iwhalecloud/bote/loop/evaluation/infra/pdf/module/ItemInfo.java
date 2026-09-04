package com.iwhalecloud.bote.loop.evaluation.infra.pdf.module;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ItemInfo {
  /** 数据项ID */
  private Long itemId;
  /** 评估器得分 */
  private Map<String, Double> evaluatorScores;
  /** 评测对象执行耗时 */
  private String totalCost;
}
