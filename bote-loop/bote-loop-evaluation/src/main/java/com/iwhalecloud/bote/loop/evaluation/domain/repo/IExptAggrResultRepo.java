package com.iwhalecloud.bote.loop.evaluation.domain.repo;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptAggrResult;
import java.util.List;

/**
 * 实验聚合结果仓库接口
 * 对应Go: IExptAggrResultRepo
 */
public interface IExptAggrResultRepo {

  /**
   * 获取实验聚合结果
   * 对应Go: GetExptAggrResult(ctx context.Context, experimentID int64, fieldType int32, fieldKey string) (*entity.ExptAggrResult, error)
   */
  ExptAggrResult getExptAggrResult(Long experimentId, Integer fieldType, String fieldKey);

  /**
   * 根据实验ID获取聚合结果
   * 对应Go: GetExptAggrResultByExperimentID(ctx context.Context, experimentID int64) ([]*entity.ExptAggrResult, error)
   */
  List<ExptAggrResult> getExptAggrResultByExperimentId(Long experimentId);

  /**
   * 批量根据实验ID获取聚合结果
   * 对应Go: BatchGetExptAggrResultByExperimentIDs(ctx context.Context, experimentIDs []int64) ([]*entity.ExptAggrResult, error)
   */
  List<ExptAggrResult> batchGetExptAggrResultByExperimentIds(List<Long> experimentIds);

  /**
   * 创建实验聚合结果
   * 对应Go: CreateExptAggrResult(ctx context.Context, exptAggrResult *entity.ExptAggrResult) error
   */
  void createExptAggrResult(ExptAggrResult exptAggrResult);

  /**
   * 批量创建实验聚合结果
   * 对应Go: BatchCreateExptAggrResult(ctx context.Context, exptAggrResults []*entity.ExptAggrResult) error
   */
  void batchCreateExptAggrResult(List<ExptAggrResult> exptAggrResults);

  /**
   * 根据版本更新实验聚合结果
   * 对应Go: UpdateExptAggrResultByVersion(ctx context.Context, exptAggrResult *entity.ExptAggrResult, taskVersion int64) error
   */
  void updateExptAggrResultByVersion(ExptAggrResult exptAggrResult, Long taskVersion);

  /**
   * 更新并获取最新版本
   * 对应Go: UpdateAndGetLatestVersion(ctx context.Context, experimentID int64, fieldType int32, fieldKey string) (int64, error)
   */
  Long updateAndGetLatestVersion(Long experimentId, Integer fieldType, String fieldKey);
}
