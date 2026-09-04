package com.iwhalecloud.bote.loop.evaluation.domain.service;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptAggregateResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.UpdateExptAggrResultParam;

import java.util.List;

/**
 * 实验聚合结果服务接口
 * 对应Go: ExptAggrResultService
 */
public interface ExptAggrResultService {

  /**
   * 批量获取实验聚合结果
   * 对应Go: BatchGetExptAggrResultByExperimentIDs
   */
  List<ExptAggregateResult> batchGetExptAggrResultByExperimentIds(Long spaceId, List<Long> experimentIds);

  /**
   * 实验完成时接收事件计算并持久化聚合结果，注意此时有更新评分场景的时序问题
   * 对应Go: CreateExptAggrResult
   */
  void createExptAggrResult(Long spaceId, Long experimentId);

  /**
   * 修正评分时接收事件计算并更新聚合结果
   * 对应Go: UpdateExptAggrResult
   */
  void updateExptAggrResult(UpdateExptAggrResultParam param);
}
