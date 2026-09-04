package com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql;

import com.iwhalecloud.bote.entity.loop.evaluation.ExptEvaluatorRefEntity;
import java.util.List;

/**
 * 实验评估器引用DAO接口
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/mysql/expt_evaluator_ref.go
 *
 * @author Generated
 * @since 2025-01-27
 */
public interface IExptEvaluatorRefDAO {

  /**
   * 创建实验评估器引用
   * 迁移对应关系: Go语言IExptEvaluatorRefDAO.Create
   *
   * @param exptEvaluatorRefs 实验评估器引用列表
   */
  void create(List<ExptEvaluatorRefEntity> exptEvaluatorRefs);

  /**
   * 批量根据实验ID获取评估器引用
   * 迁移对应关系: Go语言IExptEvaluatorRefDAO.MGetByExptID
   *
   * @param exptIds 实验ID列表
   * @param spaceId 空间ID
   * @return 评估器引用列表
   */
  List<ExptEvaluatorRefEntity> mGetByExptId(List<Long> exptIds, Long spaceId);
}
