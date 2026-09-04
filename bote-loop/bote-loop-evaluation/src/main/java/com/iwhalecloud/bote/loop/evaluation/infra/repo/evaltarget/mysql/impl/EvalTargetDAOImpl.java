package com.iwhalecloud.bote.loop.evaluation.infra.repo.evaltarget.mysql.impl;

import com.iwhalecloud.bote.entity.loop.evaluation.TargetEntity;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.evaltarget.mysql.EvalTargetDAO;
import com.iwhalecloud.bote.mapper.loop.evaluation.EvalTargetMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 评估目标DAO实现类
 * 对应Go: EvalTargetDAOImpl
 */
@Repository
@RequiredArgsConstructor
public class EvalTargetDAOImpl implements EvalTargetDAO {
  private final EvalTargetMapper evalTargetMapper;

  /**
   * 创建评估目标
   * 对应Go: CreateEvalTarget(ctx context.Context, target *model.Target, opts ...db.Option) (err error)
   */
  @Override
  public void createEvalTarget(TargetEntity target) {
    evalTargetMapper.createEvalTarget(target);
  }

  /**
   * 获取评估目标
   * 对应Go: GetEvalTarget(ctx context.Context, targetID int64, opts ...db.Option) (*model.Target, error)
   */
  @Override
  public TargetEntity getEvalTarget(Long targetId) {
    return evalTargetMapper.getEvalTarget(targetId);
  }

  /**
   * 根据来源ID获取评估目标
   * 对应Go: GetEvalTargetBySourceID(ctx context.Context, spaceID int64, sourceTargetID string, targetType int32, opts ...db.Option) (*model.Target, error)
   */
  @Override
  public TargetEntity getEvalTargetBySourceId(Long spaceId, String sourceTargetId, Integer targetType) {
    return evalTargetMapper.getEvalTargetBySourceId(spaceId, sourceTargetId, targetType);
  }

  /**
   * 根据来源ID批量获取评估目标
   * 对应Go: BatchGetEvalTargetBySource(ctx context.Context, spaceID int64, sourceTargetIDs []string, targetType int32, opts ...db.Option) ([]*model.Target, error)
   */
  @Override
  public List<TargetEntity> batchGetEvalTargetBySource(Long spaceId, List<String> sourceTargetIds, Integer targetType) {
    return evalTargetMapper.batchGetEvalTargetBySource(spaceId, sourceTargetIds, targetType);
  }

  /**
   * 批量获取评估目标
   * 对应Go: BatchGetEvalTarget(ctx context.Context, spaceID int64, targetIDs []int64, opts ...db.Option) ([]*model.Target, error)
   */
  @Override
  public List<TargetEntity> batchGetEvalTarget(Long spaceId, List<Long> targetIds) {
    return evalTargetMapper.batchGetEvalTarget(spaceId, targetIds);
  }
}
