package com.iwhalecloud.bote.loop.evaluation.infra.repo.evaltarget.mysql.impl;

import com.iwhalecloud.bote.entity.loop.evaluation.TargetRecordEntity;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.evaltarget.mysql.EvalTargetRecordDAO;
import com.iwhalecloud.bote.mapper.loop.evaluation.EvalTargetRecordMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 评估目标记录DAO实现类
 * 对应Go: EvalTargetRecordDAOImpl
 */
@Repository
@RequiredArgsConstructor
public class EvalTargetRecordDAOImpl implements EvalTargetRecordDAO {
  private final EvalTargetRecordMapper evalTargetRecordMapper;

  /**
   * 创建评估目标记录
   * 对应Go: Create(ctx context.Context, record *model.TargetRecord) (id int64, err error)
   */
  @Override
  public Long create(TargetRecordEntity record) {
    evalTargetRecordMapper.create(record);
    return record.getId();
  }

  /**
   * 根据ID和空间ID获取评估目标记录
   * 对应Go: GetByIDAndSpaceID(ctx context.Context, recordID int64, spaceID int64) (*model.TargetRecord, error)
   */
  @Override
  public TargetRecordEntity getByIdAndSpaceId(Long recordId, Long spaceId) {
    return evalTargetRecordMapper.getByIdAndSpaceId(recordId, spaceId);
  }

  /**
   * 根据ID列表和空间ID列出评估目标记录
   * 对应Go: ListByIDsAndSpaceID(ctx context.Context, recordIDs []int64, spaceID int64) ([]*model.TargetRecord, error)
   */
  @Override
  public List<TargetRecordEntity> listByIdsAndSpaceId(List<Long> recordIds, Long spaceId) {
    return evalTargetRecordMapper.listByIdsAndSpaceId(recordIds, spaceId);
  }
}
