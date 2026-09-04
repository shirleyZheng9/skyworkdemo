package com.iwhalecloud.bote.loop.evaluation.infra.repo.evaluator.mysql.impl;

import com.iwhalecloud.bote.entity.loop.evaluation.EvaluatorRecordEntity;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.evaluator.mysql.EvaluatorRecordDAO;
import com.iwhalecloud.bote.mapper.loop.evaluation.EvaluatorRecordMapper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 评估器执行结果数据访问对象实现类
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/evaluator/mysql/evaluator_record.go
 *
 * @author Generated
 * @since 2025-01-27
 */
@Repository
@RequiredArgsConstructor
public class EvaluatorRecordDAOImpl implements EvaluatorRecordDAO {
  private final EvaluatorRecordMapper evaluatorRecordMapper;

  @Override
  public int createEvaluatorRecord(EvaluatorRecordEntity evaluatorRecord) {
    try {
      return evaluatorRecordMapper.createEvaluatorRecord(evaluatorRecord);
    }
    catch (Exception e) {
      throw new BssException("创建评估器执行结果失败: " + e.getMessage(), e);
    }
  }

  @Override
  public int updateEvaluatorRecord(EvaluatorRecordEntity evaluatorRecord) {
    try {
      if (evaluatorRecord == null) {
        throw new BssException("评估器执行结果不能为空");
      }
      return evaluatorRecordMapper.updateEvaluatorRecord(evaluatorRecord);
    }
    catch (Exception e) {
      throw new BssException("更新评估器执行结果失败: " + e.getMessage(), e);
    }
  }

  @Override
  public EvaluatorRecordEntity getEvaluatorRecord(Long evaluatorRecordId, boolean includeDeleted) {
    try {
      return evaluatorRecordMapper.getEvaluatorRecord(evaluatorRecordId, includeDeleted);
    }
    catch (Exception e) {
      throw new BssException("根据ID获取评估器执行结果失败: " + e.getMessage(), e);
    }
  }

  @Override
  public List<EvaluatorRecordEntity> batchGetEvaluatorRecord(List<Long> evaluatorRecordIds, boolean includeDeleted) {
    try {
      return evaluatorRecordMapper.batchGetEvaluatorRecord(evaluatorRecordIds, includeDeleted);
    }
    catch (Exception e) {
      throw new BssException("批量根据ID获取评估器执行结果失败: " + e.getMessage(), e);
    }
  }

  @Override
  public List<EvaluatorRecordEntity> batchGetByExperimentItemTurn(Long spaceId, Long experimentId, List<Long> itemIds, List<Long> turnIds) {
    try {
      return evaluatorRecordMapper.batchGetByExperimentItemTurn(spaceId, experimentId, itemIds, turnIds);
    }
    catch (Exception e) {
      throw new BssException("根据实验ID、数据项ID和轮次ID批量获取评估器执行结果失败: " + e.getMessage(), e);
    }
  }
}
