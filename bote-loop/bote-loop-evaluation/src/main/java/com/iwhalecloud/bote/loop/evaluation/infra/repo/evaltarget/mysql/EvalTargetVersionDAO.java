package com.iwhalecloud.bote.loop.evaluation.infra.repo.evaltarget.mysql;

import com.iwhalecloud.bote.entity.loop.evaluation.TargetVersionEntity;
import java.util.List;

/**
 * 评估目标版本DAO接口
 * 对应Go: EvalTargetVersionDAO
 */
public interface EvalTargetVersionDAO {

  /**
   * 创建评估目标版本
   * 对应Go: CreateEvalTargetVersion(ctx context.Context, target *model.TargetVersion, opts ...db.Option) (err error)
   */
  void createEvalTargetVersion(TargetVersionEntity target);

  /**
   * 获取评估目标版本
   * 对应Go: GetEvalTargetVersion(ctx context.Context, spaceID int64, versionID int64, opts ...db.Option) (version *model.TargetVersion, err error)
   */
  TargetVersionEntity getEvalTargetVersion(Long spaceId, Long versionId);

  /**
   * 根据目标获取评估目标版本
   * 对应Go: GetEvalTargetVersionByTarget(ctx context.Context, spaceID int64, targetID int64, sourceTargetVersion string, opts ...db.Option) (version *model.TargetVersion, err error)
   */
  TargetVersionEntity getEvalTargetVersionByTarget(Long spaceId, Long targetId, String sourceTargetVersion);

  /**
   * 批量获取评估目标版本
   * 对应Go: BatchGetEvalTargetVersion(ctx context.Context, spaceID int64, versionIDs []int64, opts ...db.Option) (versions []*model.TargetVersion, err error)
   */
  List<TargetVersionEntity> batchGetEvalTargetVersion(Long spaceId, List<Long> versionIds);
}
