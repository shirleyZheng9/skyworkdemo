package com.iwhalecloud.bote.loop.evaluation.infra.repo.evaltarget.mysql;

import com.iwhalecloud.bote.entity.loop.evaluation.TargetRecordEntity;
import java.util.List;

/**
 * 评估目标记录DAO接口
 * 对应Go: EvalTargetRecordDAO
 */
public interface EvalTargetRecordDAO {

  /**
   * 创建评估目标记录
   * 对应Go: Create(ctx context.Context, record *model.TargetRecord) (id int64, err error)
   */
  Long create(TargetRecordEntity record);

  /**
   * 根据ID和空间ID获取评估目标记录
   * 对应Go: GetByIDAndSpaceID(ctx context.Context, recordID int64, spaceID int64) (*model.TargetRecord, error)
   */
  TargetRecordEntity getByIdAndSpaceId(Long recordId, Long spaceId);

  /**
   * 根据ID列表和空间ID列出评估目标记录
   * 对应Go: ListByIDsAndSpaceID(ctx context.Context, recordIDs []int64, spaceID int64) ([]*model.TargetRecord, error)
   */
  List<TargetRecordEntity> listByIdsAndSpaceId(List<Long> recordIds, Long spaceId);
}
