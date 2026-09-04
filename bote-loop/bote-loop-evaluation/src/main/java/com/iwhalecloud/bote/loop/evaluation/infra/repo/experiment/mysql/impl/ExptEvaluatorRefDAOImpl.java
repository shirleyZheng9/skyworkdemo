package com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.impl;

import com.iwhalecloud.bote.entity.loop.evaluation.ExptEvaluatorRefEntity;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.IExptEvaluatorRefDAO;
import com.iwhalecloud.bote.mapper.loop.evaluation.ExptEvaluatorRefMapper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 实验评估器引用DAO实现类
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/mysql/expt_evaluator_ref.go
 *
 * @author Generated
 * @since 2025-01-27
 */
@Repository
@RequiredArgsConstructor
public class ExptEvaluatorRefDAOImpl implements IExptEvaluatorRefDAO {
  private final ExptEvaluatorRefMapper exptEvaluatorRefMapper;

  /**
   * 创建实验评估器引用
   * 迁移对应关系: Go语言exptEvaluatorRefDAOImpl.Create
   */
  @Override
  public void create(List<ExptEvaluatorRefEntity> exptEvaluatorRefs) {
    try {
      if (exptEvaluatorRefs == null || exptEvaluatorRefs.isEmpty()) {
        return;
      }
      exptEvaluatorRefMapper.create(exptEvaluatorRefs);
    }
    catch (Exception e) {
      throw new BssException("创建实验评估器引用失败: " + e.getMessage(), e);
    }
  }

  /**
   * 批量根据实验ID获取评估器引用
   * 迁移对应关系: Go语言exptEvaluatorRefDAOImpl.MGetByExptID
   */
  @Override
  public List<ExptEvaluatorRefEntity> mGetByExptId(List<Long> exptIds, Long spaceId) {
    try {
      if (exptIds == null || exptIds.isEmpty()) {
        return List.of();
      }
      return exptEvaluatorRefMapper.mGetByExptId(exptIds, spaceId);
    }
    catch (Exception e) {
      throw new BssException("批量根据实验ID获取评估器引用失败: " + e.getMessage(), e);
    }
  }
}
