package com.iwhalecloud.bote.loop.evaluation.domain.repo;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTarget;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetRecord;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.dto.BatchGetEvalTargetBySourceParam;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.dto.CreateEvalTargetResult;
import java.util.List;

/**
 * 评估目标仓库接口
 * 对应Go: IEvalTargetRepo
 */
public interface IEvalTargetRepo {

  /**
   * 创建评估目标
   * 对应Go: CreateEvalTarget(ctx context.Context, do *entity.EvalTarget) (id, versionID int64, err error)
   */
  CreateEvalTargetResult createEvalTarget(EvalTarget evalTarget);

  /**
   * 获取评估目标
   * 对应Go: GetEvalTarget(ctx context.Context, targetID int64) (do *entity.EvalTarget, err error)
   */
  EvalTarget getEvalTarget(Long targetId);

  /**
   * 获取评估目标版本
   * 对应Go: GetEvalTargetVersion(ctx context.Context, spaceID, versionID int64) (do *entity.EvalTarget, err error)
   */
  EvalTarget getEvalTargetVersion(Long spaceId, Long versionId);

  /**
   * 根据来源批量获取评估目标
   * 对应Go: BatchGetEvalTargetBySource(ctx context.Context, param *BatchGetEvalTargetBySourceParam) (dos []*entity.EvalTarget, err error)
   */
  List<EvalTarget> batchGetEvalTargetBySource(BatchGetEvalTargetBySourceParam param);

  /**
   * 批量获取评估目标版本
   * 对应Go: BatchGetEvalTargetVersion(ctx context.Context, spaceID int64, versionIDs []int64) (dos []*entity.EvalTarget, err error)
   */
  List<EvalTarget> batchGetEvalTargetVersion(Long spaceId, List<Long> versionIds);

  /**
   * 创建评估目标记录
   * 对应Go: CreateEvalTargetRecord(ctx context.Context, record *entity.EvalTargetRecord) (int64, error)
   */
  Long createEvalTargetRecord(EvalTargetRecord record);

  /**
   * 根据ID和空间ID获取评估目标记录
   * 对应Go: GetEvalTargetRecordByIDAndSpaceID(ctx context.Context, spaceID int64, recordID int64) (*entity.EvalTargetRecord, error)
   */
  EvalTargetRecord getEvalTargetRecordByIdAndSpaceId(Long spaceId, Long recordId);

  /**
   * 根据ID列表和空间ID列出评估目标记录
   * 对应Go: ListEvalTargetRecordByIDsAndSpaceID(ctx context.Context, spaceID int64, recordIDs []int64) ([]*entity.EvalTargetRecord, error)
   */
  List<EvalTargetRecord> listEvalTargetRecordByIdsAndSpaceId(Long spaceId, List<Long> recordIds);
}
