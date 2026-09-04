package com.iwhalecloud.bote.mapper.loop.evaluation;

import com.iwhalecloud.bote.entity.loop.evaluation.ExptTurnEvaluatorResultRefEntity;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 实验轮次评估器结果引用Mapper接口
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/mysql/expt_turn_evaluator_result_ref.go
 *
 * @author Generated
 * @since 2025-01-27
 */

public interface ExptTurnEvaluatorResultRefMapper {

  /**
   * 批量获取实验轮次评估器结果引用
   * 迁移对应关系: Go语言ExptTurnEvaluatorResultRefDAOImpl.BatchGet
   */
  List<ExptTurnEvaluatorResultRefEntity> batchGet(@Param("spaceId") Long spaceId,
                                                  @Param("exptTurnResultIds") List<Long> exptTurnResultIds);

  /**
   * 根据实验ID获取实验轮次评估器结果引用
   * 迁移对应关系: Go语言ExptTurnEvaluatorResultRefDAOImpl.GetByExptID
   */
  List<ExptTurnEvaluatorResultRefEntity> getByExptId(@Param("spaceId") Long spaceId,
                                                     @Param("exptId") Long exptId);

  /**
   * 根据实验ID和评估器版本ID获取实验轮次评估器结果引用
   * 迁移对应关系: Go语言ExptTurnEvaluatorResultRefDAOImpl.GetByExptEvaluatorVersionID
   */
  List<ExptTurnEvaluatorResultRefEntity> getByExptEvaluatorVersionId(@Param("spaceId") Long spaceId,
                                                                     @Param("exptId") Long exptId,
                                                                     @Param("evaluatorVersionId") Long evaluatorVersionId);
}
