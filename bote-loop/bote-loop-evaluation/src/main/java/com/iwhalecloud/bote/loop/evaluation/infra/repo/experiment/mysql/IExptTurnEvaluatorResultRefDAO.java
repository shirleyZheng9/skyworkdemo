package com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql;

import com.iwhalecloud.bote.entity.loop.evaluation.ExptTurnEvaluatorResultRefEntity;
import java.util.List;

/**
 * 实验轮次评估器结果引用DAO接口
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/mysql/expt_turn_evaluator_result_ref.go
 *
 * @author Generated
 * @since 2025-01-27
 */
public interface IExptTurnEvaluatorResultRefDAO {

  /**
   * 批量获取实验轮次评估器结果引用
   * 迁移对应关系: Go语言IExptTurnEvaluatorResultRefDAO.BatchGet
   *
   * @param spaceId 空间ID
   * @param exptTurnResultIds 实验轮次结果ID列表
   * @return 实验轮次评估器结果引用列表
   */
  List<ExptTurnEvaluatorResultRefEntity> batchGet(Long spaceId, List<Long> exptTurnResultIds);

  /**
   * 根据实验ID获取实验轮次评估器结果引用
   * 迁移对应关系: Go语言IExptTurnEvaluatorResultRefDAO.GetByExptID
   *
   * @param spaceId 空间ID
   * @param exptId 实验ID
   * @return 实验轮次评估器结果引用列表
   */
  List<ExptTurnEvaluatorResultRefEntity> getByExptId(Long spaceId, Long exptId);

  /**
   * 根据实验ID和评估器版本ID获取实验轮次评估器结果引用
   * 迁移对应关系: Go语言IExptTurnEvaluatorResultRefDAO.GetByExptEvaluatorVersionID
   *
   * @param spaceId 空间ID
   * @param exptId 实验ID
   * @param evaluatorVersionId 评估器版本ID
   * @return 实验轮次评估器结果引用列表
   */
  List<ExptTurnEvaluatorResultRefEntity> getByExptEvaluatorVersionId(Long spaceId, Long exptId, Long evaluatorVersionId);
}
