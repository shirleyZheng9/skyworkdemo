package com.iwhalecloud.bote.mapper.loop.evaluation;

import com.iwhalecloud.bote.entity.loop.evaluation.ExptEvaluatorRefEntity;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 实验评估器引用Mapper接口
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/mysql/expt_evaluator_ref.go
 *
 * @author Generated
 * @since 2025-01-27
 */

public interface ExptEvaluatorRefMapper {

  /**
   * 创建实验评估器引用
   * 迁移对应关系: Go语言IExptEvaluatorRefDAO.Create
   */
  int create(@Param("exptEvaluatorRefs") List<ExptEvaluatorRefEntity> exptEvaluatorRefs);

  /**
   * 批量根据实验ID获取评估器引用
   * 迁移对应关系: Go语言IExptEvaluatorRefDAO.MGetByExptID
   */
  List<ExptEvaluatorRefEntity> mGetByExptId(@Param("exptIds") List<Long> exptIds, @Param("spaceId") Long spaceId);
}
