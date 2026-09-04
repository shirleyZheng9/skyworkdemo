package com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.impl;

import com.iwhalecloud.bote.entity.loop.evaluation.ExptAggrResultEntity;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.ExptAggrResultDAO;
import com.iwhalecloud.bote.mapper.loop.evaluation.ExptAggrResultMapper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 实验聚合结果DAO实现类
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/mysql/expt_aggr_result.go
 *
 * @author Generated
 * @since 2025-01-27
 */
@Repository
@RequiredArgsConstructor
public class ExptAggrResultDAOImpl implements ExptAggrResultDAO {
  private final ExptAggrResultMapper exptAggrResultMapper;

  /**
   * 获取实验聚合结果
   * 迁移对应关系: Go语言ExptAggrResultDAOImpl.GetExptAggrResult
   */
  @Override
  public ExptAggrResultEntity getExptAggrResult(Long experimentId, Integer fieldType, String fieldKey) {
    try {
      return exptAggrResultMapper.getExptAggrResult(experimentId, fieldType, fieldKey);
    }
    catch (Exception e) {
      throw new BssException("获取实验聚合结果失败: " + e.getMessage(), e);
    }
  }

  /**
   * 根据实验ID获取聚合结果列表
   * 迁移对应关系: Go语言ExptAggrResultDAOImpl.GetExptAggrResultByExperimentID
   */
  @Override
  public List<ExptAggrResultEntity> getExptAggrResultByExperimentId(Long experimentId) {
    try {
      return exptAggrResultMapper.getExptAggrResultByExperimentId(experimentId);
    }
    catch (Exception e) {
      throw new BssException("根据实验ID获取聚合结果列表失败: " + e.getMessage(), e);
    }
  }

  /**
   * 批量根据实验ID获取聚合结果列表
   * 迁移对应关系: Go语言ExptAggrResultDAOImpl.BatchGetExptAggrResultByExperimentIDs
   */
  @Override
  public List<ExptAggrResultEntity> batchGetExptAggrResultByExperimentIds(List<Long> experimentIds) {
    try {
      return exptAggrResultMapper.batchGetExptAggrResultByExperimentIds(experimentIds);
    }
    catch (Exception e) {
      throw new BssException("批量根据实验ID获取聚合结果列表失败: " + e.getMessage(), e);
    }
  }

  /**
   * 创建实验聚合结果
   * 迁移对应关系: Go语言ExptAggrResultDAOImpl.CreateExptAggrResult
   */
  @Override
  public void createExptAggrResult(ExptAggrResultEntity exptAggrResult) {
    try {
      exptAggrResultMapper.createExptAggrResult(exptAggrResult);
    }
    catch (Exception e) {
      throw new BssException("创建实验聚合结果失败: " + e.getMessage(), e);
    }
  }

  /**
   * 批量创建实验聚合结果
   * 迁移对应关系: Go语言ExptAggrResultDAOImpl.BatchCreateExptAggrResult
   */
  @Override
  public void batchCreateExptAggrResult(List<ExptAggrResultEntity> exptAggrResults) {
    try {
      exptAggrResultMapper.batchCreateExptAggrResult(exptAggrResults);
    }
    catch (Exception e) {
      throw new BssException("批量创建实验聚合结果失败: " + e.getMessage(), e);
    }
  }

  /**
   * 根据版本更新实验聚合结果
   * 迁移对应关系: Go语言ExptAggrResultDAOImpl.UpdateExptAggrResultByVersion
   */
  @Override
  public void updateExptAggrResultByVersion(ExptAggrResultEntity exptAggrResult, Long taskVersion) {
    try {
      exptAggrResultMapper.updateExptAggrResultByVersion(exptAggrResult, taskVersion);
    }
    catch (Exception e) {
      throw new BssException("根据版本更新实验聚合结果失败: " + e.getMessage(), e);
    }
  }

  /**
   * 更新并获取最新版本
   * 迁移对应关系: Go语言ExptAggrResultDAOImpl.UpdateAndGetLatestVersion
   */
  @Override
  public Long updateAndGetLatestVersion(Long experimentId, Integer fieldType, String fieldKey) {
    try {
      return exptAggrResultMapper.updateAndGetLatestVersion(experimentId, fieldType, fieldKey);
    }
    catch (Exception e) {
      throw new BssException("更新并获取最新版本失败: " + e.getMessage(), e);
    }
  }
}
