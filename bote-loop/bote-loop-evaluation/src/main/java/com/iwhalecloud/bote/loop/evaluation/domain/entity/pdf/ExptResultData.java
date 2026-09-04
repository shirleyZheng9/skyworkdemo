package com.iwhalecloud.bote.loop.evaluation.domain.entity.pdf;

import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ExperimentDTO;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.BatchGetExperimentAggrResultResponse;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.BatchGetExperimentResultResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * 实验结果数据
 * */
@Getter
@Setter
public class ExptResultData {
  /** 实验ID */
  private Long exptId;
  /** 租户ID */
  private Long tenantId;
  /** 实验列表 */
  private List<Long> exptIds;
  /** 实验 */
  private ExperimentDTO experiment;
  /** 实验结果 */
  private BatchGetExperimentResultResponse experimentResultResponse;
  /** 实验结果指标统计 */
  private BatchGetExperimentAggrResultResponse experimentAggrResultResponse;
  /** 评估器集合 */
  private Map<Long, String> evaluatorInfos;
  /** 评估器版本ID列表 */
  private List<Long> evaluatorInfoList;
  /** 评估器通过得分集合 */
  private Map<Long, Double> evaluatorPassScoreMap;
}
