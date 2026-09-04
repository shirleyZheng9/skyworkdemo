package com.iwhalecloud.bote.loop.evaluation.infra.repo.evaltarget.mysql.impl;

import com.iwhalecloud.bote.entity.loop.evaluation.TargetVersionEntity;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.evaltarget.mysql.EvalTargetVersionDAO;
import com.iwhalecloud.bote.mapper.loop.evaluation.EvalTargetVersionMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 评估目标版本DAO实现类
 * 对应Go: EvalTargetVersionDAOImpl
 */
@Repository
@RequiredArgsConstructor
public class EvalTargetVersionDAOImpl implements EvalTargetVersionDAO {
  private final EvalTargetVersionMapper evalTargetVersionMapper;

  /**
   * 创建评估目标版本
   * 对应Go: CreateEvalTargetVersion(ctx context.Context, target *model.TargetVersion, opts ...db.Option) (err error)
   */
  @Override
  public void createEvalTargetVersion(TargetVersionEntity target) {
    evalTargetVersionMapper.createEvalTargetVersion(target);
  }

  /**
   * 获取评估目标版本
   * 对应Go: GetEvalTargetVersion(ctx context.Context, spaceID int64, versionID int64, opts ...db.Option) (version *model.TargetVersion, err error)
   */
  @Override
  public TargetVersionEntity getEvalTargetVersion(Long spaceId, Long versionId) {
    return evalTargetVersionMapper.getEvalTargetVersion(spaceId, versionId);
  }

  /**
   * 根据目标获取评估目标版本
   * 对应Go: GetEvalTargetVersionByTarget(ctx context.Context, spaceID int64, targetID int64, sourceTargetVersion string, opts ...db.Option) (version *model.TargetVersion, err error)
   */
  @Override
  public TargetVersionEntity getEvalTargetVersionByTarget(Long spaceId, Long targetId, String sourceTargetVersion) {
    return evalTargetVersionMapper.getEvalTargetVersionByTarget(spaceId, targetId, sourceTargetVersion);
  }

  /**
   * 批量获取评估目标版本
   * 对应Go: BatchGetEvalTargetVersion(ctx context.Context, spaceID int64, versionIDs []int64, opts ...db.Option) (versions []*model.TargetVersion, err error)
   */
  @Override
  public List<TargetVersionEntity> batchGetEvalTargetVersion(Long spaceId, List<Long> versionIds) {
    return evalTargetVersionMapper.batchGetEvalTargetVersion(spaceId, versionIds);
  }
}
