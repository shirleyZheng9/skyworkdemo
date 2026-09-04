package com.iwhalecloud.bote.loop.evaluation.domain.repo;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorRecord;
import java.util.List;

/**
 * 评估器记录仓库接口
 * 对应Go: IEvaluatorRecordRepo
 */
public interface IEvaluatorRecordRepo {

  /**
   * 创建评估器记录
   * 对应Go: CreateEvaluatorRecord(ctx context.Context, evaluatorRecord *entity.EvaluatorRecord) error
   */
  void createEvaluatorRecord(EvaluatorRecord evaluatorRecord);

  /**
   * 修正评估器记录
   * 对应Go: CorrectEvaluatorRecord(ctx context.Context, evaluatorRecordDO *entity.EvaluatorRecord) error
   */
  void correctEvaluatorRecord(EvaluatorRecord evaluatorRecordDO);

  /**
   * 获取评估器记录
   * 对应Go: GetEvaluatorRecord(ctx context.Context, evaluatorRecordID int64, includeDeleted bool) (*entity.EvaluatorRecord, error)
   */
  EvaluatorRecord getEvaluatorRecord(Long evaluatorRecordId, Boolean includeDeleted);

  /**
   * 批量获取评估器记录
   * 对应Go: BatchGetEvaluatorRecord(ctx context.Context, evaluatorRecordIDs []int64, includeDeleted bool) ([]*entity.EvaluatorRecord, error)
   */
  List<EvaluatorRecord> batchGetEvaluatorRecord(List<Long> evaluatorRecordIds, Boolean includeDeleted);

  /**
   * 根据实验ID、数据项ID和轮次ID批量获取评估器记录
   *
   * @param spaceId 空间ID
   * @param experimentId 实验ID
   * @param itemIds 数据项ID列表
   * @param turnIds 轮次ID列表
   * @return 评估器记录列表
   */
  List<EvaluatorRecord> batchGetByExperimentItemTurn(Long spaceId, Long experimentId, List<Long> itemIds, List<Long> turnIds);
}
