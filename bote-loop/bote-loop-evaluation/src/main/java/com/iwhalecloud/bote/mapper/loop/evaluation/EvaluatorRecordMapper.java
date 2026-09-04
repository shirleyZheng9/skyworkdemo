package com.iwhalecloud.bote.mapper.loop.evaluation;

import com.iwhalecloud.bote.entity.loop.evaluation.EvaluatorRecordEntity;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 评估器执行结果Mapper接口
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/evaluator/mysql/evaluator_record.go
 *
 * @author Generated
 * @since 2025-01-27
 */

public interface EvaluatorRecordMapper {

  /**
   * 创建评估器执行结果
   * 迁移对应关系: Go语言EvaluatorRecordDAOImpl.CreateEvaluatorRecord
   *
   * @param evaluatorRecord 评估器执行结果实体
   * @return 影响行数
   */
  int createEvaluatorRecord(@Param("evaluatorRecord") EvaluatorRecordEntity evaluatorRecord);

  /**
   * 更新评估器执行结果
   * 迁移对应关系: Go语言EvaluatorRecordDAOImpl.UpdateEvaluatorRecord
   *
   * @param evaluatorRecord 评估器执行结果实体
   * @return 影响行数
   */
  int updateEvaluatorRecord(@Param("evaluatorRecord") EvaluatorRecordEntity evaluatorRecord);

  /**
   * 根据ID获取评估器执行结果
   * 迁移对应关系: Go语言EvaluatorRecordDAOImpl.GetEvaluatorRecord
   *
   * @param evaluatorRecordId 评估器执行结果ID
   * @param includeDeleted 是否包含已删除记录
   * @return 评估器执行结果实体
   */
  EvaluatorRecordEntity getEvaluatorRecord(@Param("evaluatorRecordId") Long evaluatorRecordId,
                                           @Param("includeDeleted") boolean includeDeleted);

  /**
   * 批量根据ID获取评估器执行结果
   * 迁移对应关系: Go语言EvaluatorRecordDAOImpl.BatchGetEvaluatorRecord
   *
   * @param evaluatorRecordIds 评估器执行结果ID列表
   * @param includeDeleted 是否包含已删除记录
   * @return 评估器执行结果实体列表
   */
  List<EvaluatorRecordEntity> batchGetEvaluatorRecord(@Param("evaluatorRecordIds") List<Long> evaluatorRecordIds,
                                                      @Param("includeDeleted") boolean includeDeleted);

  /**
   * 根据实验ID、数据项ID和轮次ID批量获取评估器执行结果
   *
   * @param spaceId 空间ID
   * @param experimentId 实验ID
   * @param itemIds 数据项ID列表
   * @param turnIds 轮次ID列表
   * @return 评估器执行结果实体列表
   */
  List<EvaluatorRecordEntity> batchGetByExperimentItemTurn(@Param("spaceId") Long spaceId,
                                                           @Param("experimentId") Long experimentId,
                                                           @Param("itemIds") List<Long> itemIds,
                                                           @Param("turnIds") List<Long> turnIds);
}
