package com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql;

import com.iwhalecloud.bote.entity.loop.evaluation.ExptStatsEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.StatsCntArithOp;
import java.util.List;

/**
 * 实验统计DAO接口
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/mysql/expt_stats.go
 *
 * @author Generated
 * @since 2025-01-27
 */
public interface IExptStatsDAO {

  /**
   * 创建实验统计
   * 迁移对应关系: Go语言IExptStatsDAO.Create
   *
   * @param stats 实验统计
   */
  void create(ExptStatsEntity stats);

  /**
   * 获取实验统计
   * 迁移对应关系: Go语言IExptStatsDAO.Get
   *
   * @param exptId 实验ID
   * @param spaceId 空间ID
   * @return 实验统计
   */
  ExptStatsEntity get(Long exptId, Long spaceId);

  /**
   * 批量获取实验统计
   * 迁移对应关系: Go语言IExptStatsDAO.MGet
   *
   * @param exptIds 实验ID列表
   * @param spaceId 空间ID
   * @return 实验统计列表
   */
  List<ExptStatsEntity> mGet(List<Long> exptIds, Long spaceId);

  /**
   * 根据实验ID更新统计
   * 迁移对应关系: Go语言IExptStatsDAO.UpdateByExptID
   *
   * @param exptId 实验ID
   * @param spaceId 空间ID
   * @param stats 实验统计
   */
  void updateByExptId(Long exptId, Long spaceId, ExptStatsEntity stats);

  /**
   * 算术操作计数
   * 迁移对应关系: Go语言IExptStatsDAO.ArithOperateCount
   *
   * @param exptId 实验ID
   * @param spaceId 空间ID
   * @param cntArithOp 计数算术操作
   */
  void arithOperateCount(Long exptId, Long spaceId, StatsCntArithOp cntArithOp);

  /**
   * 保存实验统计
   * 迁移对应关系: Go语言IExptStatsDAO.Save
   *
   * @param stats 实验统计
   */
  void save(ExptStatsEntity stats);
}
