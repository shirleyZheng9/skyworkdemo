package com.iwhalecloud.bote.mapper.loop.evaluation;

import com.iwhalecloud.bote.entity.loop.evaluation.ExptStatsEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.StatsCntArithOp;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 实验统计Mapper接口
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/mysql/expt_stats.go
 *
 * @author Generated
 * @since 2025-01-27
 */

public interface ExptStatsMapper {

  /**
   * 创建实验统计
   * 迁移对应关系: Go语言IExptStatsDAO.Create
   */
  int create(ExptStatsEntity stats);

  /**
   * 获取实验统计
   * 迁移对应关系: Go语言IExptStatsDAO.Get
   */
  ExptStatsEntity get(@Param("exptId") Long exptId, @Param("spaceId") Long spaceId);

  /**
   * 批量获取实验统计
   * 迁移对应关系: Go语言IExptStatsDAO.MGet
   */
  List<ExptStatsEntity> mGet(@Param("exptIds") List<Long> exptIds, @Param("spaceId") Long spaceId);

  /**
   * 根据实验ID更新统计
   * 迁移对应关系: Go语言IExptStatsDAO.UpdateByExptID
   */
  int updateByExptId(@Param("exptId") Long exptId,
                     @Param("spaceId") Long spaceId,
                     @Param("stats") ExptStatsEntity stats);

  /**
   * 算术操作计数
   * 迁移对应关系: Go语言IExptStatsDAO.ArithOperateCount
   */
  int arithOperateCount(@Param("exptId") Long exptId,
                        @Param("spaceId") Long spaceId,
                        @Param("cntArithOp") StatsCntArithOp cntArithOp);

  /**
   * 检查记录是否存在
   */
  boolean existsByExptIdAndSpaceId(@Param("exptId") Long exptId, @Param("spaceId") Long spaceId);

  /**
   * 插入实验统计
   */
  int insertStats(ExptStatsEntity stats);

  /**
   * 更新实验统计
   */
  int updateStats(ExptStatsEntity stats);
}
