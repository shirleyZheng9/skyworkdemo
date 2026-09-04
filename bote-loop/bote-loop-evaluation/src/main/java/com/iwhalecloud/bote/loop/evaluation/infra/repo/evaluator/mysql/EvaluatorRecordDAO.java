package com.iwhalecloud.bote.loop.evaluation.infra.repo.evaluator.mysql;

import com.iwhalecloud.bote.entity.loop.evaluation.EvaluatorRecordEntity;
import java.util.List;

/**
 * 评估器执行结果数据访问对象接口
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/evaluator/mysql/evaluator_record.go
 *
 * @author Generated
 * @since 2025-01-27
 */
public interface EvaluatorRecordDAO {

  /**
   * 创建评估器执行结果
   * 迁移对应关系: Go语言EvaluatorRecordDAO.CreateEvaluatorRecord
   *
   * @param evaluatorRecord 评估器执行结果实体
   * @return 影响行数
   */
  int createEvaluatorRecord(EvaluatorRecordEntity evaluatorRecord);

  /**
   * 更新评估器执行结果
   * 迁移对应关系: Go语言EvaluatorRecordDAO.UpdateEvaluatorRecord
   *
   * @param evaluatorRecord 评估器执行结果实体
   * @return 影响行数
   */
  int updateEvaluatorRecord(EvaluatorRecordEntity evaluatorRecord);

  /**
   * 根据ID获取评估器执行结果
   * 迁移对应关系: Go语言EvaluatorRecordDAO.GetEvaluatorRecord
   *
   * @param evaluatorRecordId 评估器执行结果ID
   * @param includeDeleted 是否包含已删除记录
   * @return 评估器执行结果实体
   */
  EvaluatorRecordEntity getEvaluatorRecord(Long evaluatorRecordId, boolean includeDeleted);

  /**
   * 批量根据ID获取评估器执行结果
   * 迁移对应关系: Go语言EvaluatorRecordDAO.BatchGetEvaluatorRecord
   *
   * @param evaluatorRecordIds 评估器执行结果ID列表
   * @param includeDeleted 是否包含已删除记录
   * @return 评估器执行结果实体列表
   */
  List<EvaluatorRecordEntity> batchGetEvaluatorRecord(List<Long> evaluatorRecordIds, boolean includeDeleted);

  /**
   * 根据实验ID、数据项ID和轮次ID批量获取评估器执行结果
   *
   * @param spaceId 空间ID
   * @param experimentId 实验ID
   * @param itemIds 数据项ID列表
   * @param turnIds 轮次ID列表
   * @return 评估器执行结果实体列表
   */
  List<EvaluatorRecordEntity> batchGetByExperimentItemTurn(Long spaceId, Long experimentId, List<Long> itemIds, List<Long> turnIds);
}
