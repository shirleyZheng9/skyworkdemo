package com.iwhalecloud.bote.loop.evaluation.infra.pdf.module;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ItemDetailInfo {
  /** 数据项ID */
  private Long itemId;
  /** 评测集数据 */
  private Map<String, String> evalSetMap;
  /** 评测对象输出数据 */
  private String evalTargetOutput;
  /** 评估器得分 */
  private List<EvaluatorRecordInfo> evaluatorRecordInfos;
}
