package com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql;

import com.iwhalecloud.bote.entity.loop.evaluation.ExptAggrResultEntity;
import java.util.List;

/**
 * 实验聚合结果DAO接口
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/mysql/expt_aggr_result.go
 *
 * @author Generated
 * @since 2025-01-27
 */
public interface ExptAggrResultDAO {

  /**
   * 获取实验聚合结果
   * 迁移对应关系: Go语言ExptAggrResultDAO.GetExptAggrResult
   *
   * @param experimentId 实验ID
   * @param fieldType 字段类型
   * @param fieldKey 字段键
   * @return 实验聚合结果
   */
  ExptAggrResultEntity getExptAggrResult(Long experimentId, Integer fieldType, String fieldKey);

  /**
   * 根据实验ID获取聚合结果列表
   * 迁移对应关系: Go语言ExptAggrResultDAO.GetExptAggrResultByExperimentID
   *
   * @param experimentId 实验ID
   * @return 聚合结果列表
   */
  List<ExptAggrResultEntity> getExptAggrResultByExperimentId(Long experimentId);

  /**
   * 批量根据实验ID获取聚合结果列表
   * 迁移对应关系: Go语言ExptAggrResultDAO.BatchGetExptAggrResultByExperimentIDs
   *
   * @param experimentIds 实验ID列表
   * @return 聚合结果列表
   */
  List<ExptAggrResultEntity> batchGetExptAggrResultByExperimentIds(List<Long> experimentIds);

  /**
   * 创建实验聚合结果
   * 迁移对应关系: Go语言ExptAggrResultDAO.CreateExptAggrResult
   *
   * @param exptAggrResult 实验聚合结果
   */
  void createExptAggrResult(ExptAggrResultEntity exptAggrResult);

  /**
   * 批量创建实验聚合结果
   * 迁移对应关系: Go语言ExptAggrResultDAO.BatchCreateExptAggrResult
   *
   * @param exptAggrResults 实验聚合结果列表
   */
  void batchCreateExptAggrResult(List<ExptAggrResultEntity> exptAggrResults);

  /**
   * 根据版本更新实验聚合结果
   * 迁移对应关系: Go语言ExptAggrResultDAO.UpdateExptAggrResultByVersion
   *
   * @param exptAggrResult 实验聚合结果
   * @param taskVersion 任务版本
   */
  void updateExptAggrResultByVersion(ExptAggrResultEntity exptAggrResult, Long taskVersion);

  /**
   * 更新并获取最新版本
   * 迁移对应关系: Go语言ExptAggrResultDAO.UpdateAndGetLatestVersion
   *
   * @param experimentId 实验ID
   * @param fieldType 字段类型
   * @param fieldKey 字段键
   * @return 最新版本号
   */
  Long updateAndGetLatestVersion(Long experimentId, Integer fieldType, String fieldKey);
}
