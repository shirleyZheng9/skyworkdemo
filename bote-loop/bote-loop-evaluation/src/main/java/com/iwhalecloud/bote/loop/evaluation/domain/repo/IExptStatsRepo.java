package com.iwhalecloud.bote.loop.evaluation.domain.repo;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptStats;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.StatsCntArithOp;
import java.util.List;

/**
 * 实验统计仓库接口
 * 对应Go: IExptStatsRepo
 */
public interface IExptStatsRepo {

  /**
   * 创建实验统计
   * 对应Go: Create(ctx context.Context, stats *entity.ExptStats) error
   */
  void create(ExptStats stats);

  /**
   * 获取实验统计
   * 对应Go: Get(ctx context.Context, exptID, spaceID int64) (*entity.ExptStats, error)
   */
  ExptStats get(Long exptId, Long spaceId);

  /**
   * 批量获取实验统计
   * 对应Go: MGet(ctx context.Context, exptIDs []int64, spaceID int64) ([]*entity.ExptStats, error)
   */
  List<ExptStats> mGet(List<Long> exptIds, Long spaceId);

  /**
   * 根据实验ID更新统计
   * 对应Go: UpdateByExptID(ctx context.Context, exptID, spaceID int64, stats *entity.ExptStats) error
   */
  void updateByExptId(Long exptId, Long spaceId, ExptStats stats);

  /**
   * 算术操作计数
   * 对应Go: ArithOperateCount(ctx context.Context, exptID, spaceID int64, cntArithOp *entity.StatsCntArithOp) error
   */
  void arithOperateCount(Long exptId, Long spaceId, StatsCntArithOp cntArithOp);

  /**
   * 保存实验统计
   * 对应Go: Save(ctx context.Context, stats *entity.ExptStats) error
   */
  void save(ExptStats stats);
}
