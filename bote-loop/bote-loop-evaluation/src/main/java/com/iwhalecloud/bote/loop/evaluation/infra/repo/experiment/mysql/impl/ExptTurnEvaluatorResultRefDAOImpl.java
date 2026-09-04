package com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.impl;

import com.iwhalecloud.bote.entity.loop.evaluation.ExptTurnEvaluatorResultRefEntity;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.IExptTurnEvaluatorResultRefDAO;
import com.iwhalecloud.bote.mapper.loop.evaluation.ExptTurnEvaluatorResultRefMapper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 实验轮次评估器结果引用DAO实现类
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/mysql/expt_turn_evaluator_result_ref.go
 *
 * @author Generated
 * @since 2025-01-27
 */
@Repository
@RequiredArgsConstructor
public class ExptTurnEvaluatorResultRefDAOImpl implements IExptTurnEvaluatorResultRefDAO {
  private final ExptTurnEvaluatorResultRefMapper exptTurnEvaluatorResultRefMapper;

  /**
   * 批量获取实验轮次评估器结果引用
   * 迁移对应关系: Go语言ExptTurnEvaluatorResultRefDAOImpl.BatchGet
   */
  @Override
  public List<ExptTurnEvaluatorResultRefEntity> batchGet(Long spaceId, List<Long> exptTurnResultIds) {
    try {
      return exptTurnEvaluatorResultRefMapper.batchGet(spaceId, exptTurnResultIds);
    }
    catch (Exception e) {
      throw new BssException("批量获取实验轮次评估器结果引用失败: " + e.getMessage(), e);
    }
  }

  /**
   * 根据实验ID获取实验轮次评估器结果引用
   * 迁移对应关系: Go语言ExptTurnEvaluatorResultRefDAOImpl.GetByExptID
   */
  @Override
  public List<ExptTurnEvaluatorResultRefEntity> getByExptId(Long spaceId, Long exptId) {
    try {
      return exptTurnEvaluatorResultRefMapper.getByExptId(spaceId, exptId);
    }
    catch (Exception e) {
      throw new BssException("根据实验ID获取实验轮次评估器结果引用失败: " + e.getMessage(), e);
    }
  }

  /**
   * 根据实验ID和评估器版本ID获取实验轮次评估器结果引用
   * 迁移对应关系: Go语言ExptTurnEvaluatorResultRefDAOImpl.GetByExptEvaluatorVersionID
   */
  @Override
  public List<ExptTurnEvaluatorResultRefEntity> getByExptEvaluatorVersionId(Long spaceId, Long exptId, Long evaluatorVersionId) {
    try {
      return exptTurnEvaluatorResultRefMapper.getByExptEvaluatorVersionId(spaceId, exptId, evaluatorVersionId);
    }
    catch (Exception e) {
      throw new BssException("根据实验ID和评估器版本ID获取实验轮次评估器结果引用失败: " + e.getMessage(), e);
    }
  }
}
