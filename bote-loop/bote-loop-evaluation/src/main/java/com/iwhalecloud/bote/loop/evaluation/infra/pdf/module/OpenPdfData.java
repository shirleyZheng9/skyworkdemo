package com.iwhalecloud.bote.loop.evaluation.infra.pdf.module;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class OpenPdfData {
  /** 报告信息 */
  private ReportInfo reportInfo;
  /** 基础信息 */
  private ExperimentBaseInfo baseInfo;
  /** 指标统计 */
  private AggregateSummaryInfo aggregateSummaryInfo;
  /** 评估器聚合得分 */
  private List<AggregateEvaluatorInfo> aggregateEvaluatorInfos;
  /** 得分明细-数据项分布 */
  private Map<String, List<ScoreDistributionInfo>> evaluatorScoreDistributionInfos;
  /** 数据明细 */
  private List<ItemInfo> itemInfos;
  /** 详情 */
  private List<ItemDetailInfo> itemDetailInfos;
}
