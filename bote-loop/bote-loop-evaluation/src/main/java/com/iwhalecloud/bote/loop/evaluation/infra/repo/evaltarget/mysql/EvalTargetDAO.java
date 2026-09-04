package com.iwhalecloud.bote.loop.evaluation.infra.repo.evaltarget.mysql;

import com.iwhalecloud.bote.entity.loop.evaluation.TargetEntity;
import java.util.List;

/**
 * 评估目标DAO接口
 * 对应Go: EvalTargetDAO
 */
public interface EvalTargetDAO {

  /**
   * 创建评估目标
   * 对应Go: CreateEvalTarget(ctx context.Context, target *model.Target, opts ...db.Option) (err error)
   */
  void createEvalTarget(TargetEntity target);

  /**
   * 获取评估目标
   * 对应Go: GetEvalTarget(ctx context.Context, targetID int64, opts ...db.Option) (*model.Target, error)
   */
  TargetEntity getEvalTarget(Long targetId);

  /**
   * 根据来源ID获取评估目标
   * 对应Go: GetEvalTargetBySourceID(ctx context.Context, spaceID int64, sourceTargetID string, targetType int32, opts ...db.Option) (*model.Target, error)
   */
  TargetEntity getEvalTargetBySourceId(Long spaceId, String sourceTargetId, Integer targetType);

  /**
   * 根据来源ID批量获取评估目标
   * 对应Go: BatchGetEvalTargetBySource(ctx context.Context, spaceID int64, sourceTargetIDs []string, targetType int32, opts ...db.Option) ([]*model.Target, error)
   */
  List<TargetEntity> batchGetEvalTargetBySource(Long spaceId, List<String> sourceTargetIds, Integer targetType);

  /**
   * 批量获取评估目标
   * 对应Go: BatchGetEvalTarget(ctx context.Context, spaceID int64, targetIDs []int64, opts ...db.Option) ([]*model.Target, error)
   */
  List<TargetEntity> batchGetEvalTarget(Long spaceId, List<Long> targetIds);
}
