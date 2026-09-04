package com.iwhalecloud.bote.loop.evaluation.infra.repo.evaluator;

import com.iwhalecloud.bote.entity.loop.evaluation.EvaluatorRecordEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorRecord;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IEvaluatorRecordRepo;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.evaluator.mysql.EvaluatorRecordDAO;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.evaluator.mysql.convertor.EvaluatorRecordConvertor;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 评估器执行结果仓储实现类
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/evaluator/evaluator_record_impl.go
 *
 * @author Generated
 * @since 2025-01-27
 */
@Repository
@RequiredArgsConstructor
public class EvaluatorRecordRepoImpl implements IEvaluatorRecordRepo {
  private final EvaluatorRecordDAO evaluatorRecordDAO;

  @Override
  public void createEvaluatorRecord(EvaluatorRecord evaluatorRecord) {
    try {
      EvaluatorRecordEntity po = EvaluatorRecordConvertor.convertToPO(evaluatorRecord);
      evaluatorRecordDAO.createEvaluatorRecord(po);
    }
    catch (Exception e) {
      throw new BssException("创建评估器执行结果失败: " + e.getMessage(), e);
    }
  }

  @Override
  public void correctEvaluatorRecord(EvaluatorRecord evaluatorRecord) {
    try {
      EvaluatorRecordEntity po = EvaluatorRecordConvertor.convertToPO(evaluatorRecord);
      evaluatorRecordDAO.updateEvaluatorRecord(po);
    }
    catch (Exception e) {
      throw new BssException("修正评估器执行结果失败: " + e.getMessage(), e);
    }
  }

  @Override
  public EvaluatorRecord getEvaluatorRecord(Long evaluatorRecordId, Boolean includeDeleted) {
    try {
      EvaluatorRecordEntity po = evaluatorRecordDAO.getEvaluatorRecord(evaluatorRecordId, includeDeleted);
      if (po == null) {
        return null;
      }
      return EvaluatorRecordConvertor.convertToDO(po);
    }
    catch (Exception e) {
      throw new BssException("根据ID获取评估器执行结果失败: " + e.getMessage(), e);
    }
  }

  @Override
  public List<EvaluatorRecord> batchGetEvaluatorRecord(List<Long> evaluatorRecordIds, Boolean includeDeleted) {
    try {
      if (evaluatorRecordIds == null || evaluatorRecordIds.isEmpty()) {
        return new ArrayList<>();
      }
      List<EvaluatorRecordEntity> pos = evaluatorRecordDAO.batchGetEvaluatorRecord(evaluatorRecordIds, includeDeleted);
      return pos.stream()
        .map(EvaluatorRecordConvertor::convertToDO)
        .collect(Collectors.toList());
    }
    catch (Exception e) {
      throw new BssException("批量根据ID获取评估器执行结果失败: " + e.getMessage(), e);
    }
  }

  @Override
  public List<EvaluatorRecord> batchGetByExperimentItemTurn(Long spaceId, Long experimentId, List<Long> itemIds, List<Long> turnIds) {
    try {
      List<EvaluatorRecordEntity> pos = evaluatorRecordDAO.batchGetByExperimentItemTurn(spaceId, experimentId, itemIds, turnIds);
      return pos.stream()
        .map(EvaluatorRecordConvertor::convertToDO)
        .collect(Collectors.toList());
    }
    catch (Exception e) {
      throw new BssException("根据实验ID、数据项ID和轮次ID批量获取评估器执行结果失败: " + e.getMessage(), e);
    }
  }
}
