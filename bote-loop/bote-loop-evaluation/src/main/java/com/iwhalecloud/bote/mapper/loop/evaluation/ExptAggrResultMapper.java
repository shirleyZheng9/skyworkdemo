package com.iwhalecloud.bote.mapper.loop.evaluation;

import com.iwhalecloud.bote.entity.loop.evaluation.ExptAggrResultEntity;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 实验聚合结果Mapper接口
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/mysql/expt_aggr_result.go
 *
 * @author Generated
 * @since 2025-01-27
 */

public interface ExptAggrResultMapper {

  /**
   * 获取实验聚合结果
   * 迁移对应关系: Go语言ExptAggrResultDAO.GetExptAggrResult
   */
  ExptAggrResultEntity getExptAggrResult(@Param("experimentId") Long experimentId,
                                         @Param("fieldType") Integer fieldType,
                                         @Param("fieldKey") String fieldKey);

  /**
   * 根据实验ID获取聚合结果列表
   * 迁移对应关系: Go语言ExptAggrResultDAO.GetExptAggrResultByExperimentID
   */
  List<ExptAggrResultEntity> getExptAggrResultByExperimentId(@Param("experimentId") Long experimentId);

  /**
   * 批量根据实验ID获取聚合结果列表
   * 迁移对应关系: Go语言ExptAggrResultDAO.BatchGetExptAggrResultByExperimentIDs
   */
  List<ExptAggrResultEntity> batchGetExptAggrResultByExperimentIds(@Param("experimentIds") List<Long> experimentIds);

  /**
   * 创建实验聚合结果
   * 迁移对应关系: Go语言ExptAggrResultDAO.CreateExptAggrResult
   */
  int createExptAggrResult(ExptAggrResultEntity exptAggrResult);

  /**
   * 批量创建实验聚合结果
   * 迁移对应关系: Go语言ExptAggrResultDAO.BatchCreateExptAggrResult
   */
  int batchCreateExptAggrResult(@Param("exptAggrResults") List<ExptAggrResultEntity> exptAggrResults);

  /**
   * 根据版本更新实验聚合结果
   * 迁移对应关系: Go语言ExptAggrResultDAO.UpdateExptAggrResultByVersion
   */
  int updateExptAggrResultByVersion(@Param("exptAggrResult") ExptAggrResultEntity exptAggrResult,
                                    @Param("taskVersion") Long taskVersion);

  /**
   * 更新并获取最新版本
   * 迁移对应关系: Go语言ExptAggrResultDAO.UpdateAndGetLatestVersion
   */
  Long updateAndGetLatestVersion(@Param("experimentId") Long experimentId,
                                 @Param("fieldType") Integer fieldType,
                                 @Param("fieldKey") String fieldKey);
}
