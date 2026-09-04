package com.iwhalecloud.bote.loop.evaluation.domain.service;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExperimentResultListResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptCalculateStats;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptStats;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnEvaluatorResultRef;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResultFilterKeyMapping;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.MGetExperimentResultParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Session;

import java.util.List;

/**
 * 实验结果服务接口
 * 对应Go: ExptResultService
 */
public interface ExptResultService {

  /**
   * 批量获取实验结果
   * 对应Go: MGetExperimentResult
   */
  ExperimentResultListResult mGetExperimentResult(MGetExperimentResultParam param);

  /**
   * 将 run_log 表结果同步到 result 表
   * 对应Go: RecordItemRunLogs
   */
  List<ExptTurnEvaluatorResultRef> recordItemRunLogs(Long exptId, Long exptRunId, Long itemId, Long spaceId);

  /**
   * 获取实验数据项轮次结果
   * 对应Go: GetExptItemTurnResults
   */
  List<ExptTurnResult> getExptItemTurnResults(Long exptId, Long itemId, Long spaceId, Session session);

  /**
   * 获取实验数据项轮次结果（带exptRunId过滤）
   * 对应Go: GetExptItemTurnResults
   */
  List<ExptTurnResult> getExptItemTurnResults(Long exptId, Long itemId, Long spaceId, Long exptRunId, Session session);

  /**
   * 创建统计
   * 对应Go: CreateStats
   */
  void createStats(ExptStats exptStats);

  /**
   * 获取统计
   * 对应Go: GetStats
   */
  ExptStats getStats(Long exptId, Long spaceId, Session session);

  /**
   * 批量获取统计
   * 对应Go: MGetStats
   */
  List<ExptStats> mGetStats(List<Long> exptIds, Long spaceId, Session session);

  /**
   * 计算统计
   * 对应Go: CalculateStats
   */
  ExptCalculateStats calculateStats(Long exptId, Long spaceId, Session session);

  /**
   * 手动更新实验轮次结果过滤器
   * 对应Go: ManualUpsertExptTurnResultFilter
   */
  void manualUpsertExptTurnResultFilter(Long spaceId, Long exptId, List<Long> itemIds);

  /**
   * 更新实验轮次结果过滤器
   * 对应Go: UpsertExptTurnResultFilter
   */
  void upsertExptTurnResultFilter(Long spaceId, Long exptId, List<Long> itemIds);

  /**
   * 插入实验轮次结果过滤器键映射
   * 对应Go: InsertExptTurnResultFilterKeyMappings
   */
  void insertExptTurnResultFilterKeyMappings(List<ExptTurnResultFilterKeyMapping> mappings);

  /**
   * 比较实验轮次结果过滤器
   * 对应Go: CompareExptTurnResultFilters
   */
  void compareExptTurnResultFilters(Long spaceId, Long exptId, List<Long> itemIds, Integer retryTimes);
}
