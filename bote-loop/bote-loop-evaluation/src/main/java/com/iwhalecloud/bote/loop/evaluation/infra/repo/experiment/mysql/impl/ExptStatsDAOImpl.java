package com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.impl;

import com.iwhalecloud.bote.entity.loop.evaluation.ExptStatsEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.StatsCntArithOp;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.IExptStatsDAO;
import com.iwhalecloud.bote.mapper.loop.evaluation.ExptStatsMapper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 实验统计DAO实现类
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/mysql/expt_stats.go
 *
 * @author Generated
 * @since 2025-01-27
 */
@Repository
@RequiredArgsConstructor
public class ExptStatsDAOImpl implements IExptStatsDAO {
  private final ExptStatsMapper exptStatsMapper;

  /**
   * 创建实验统计
   * 迁移对应关系: Go语言exptStatsDAOImpl.Create
   */
  @Override
  public void create(ExptStatsEntity stats) {
    try {
      exptStatsMapper.create(stats);
    }
    catch (Exception e) {
      throw new BssException("创建实验统计失败: " + e.getMessage(), e);
    }
  }

  /**
   * 获取实验统计
   * 迁移对应关系: Go语言exptStatsDAOImpl.Get
   */
  @Override
  public ExptStatsEntity get(Long exptId, Long spaceId) {
    try {
      return exptStatsMapper.get(exptId, spaceId);
    }
    catch (Exception e) {
      throw new BssException("获取实验统计失败: " + e.getMessage(), e);
    }
  }

  /**
   * 批量获取实验统计
   * 迁移对应关系: Go语言exptStatsDAOImpl.MGet
   */
  @Override
  public List<ExptStatsEntity> mGet(List<Long> exptIds, Long spaceId) {
    try {
      if (exptIds == null || exptIds.isEmpty()) {
        return List.of();
      }
      return exptStatsMapper.mGet(exptIds, spaceId);
    }
    catch (Exception e) {
      throw new BssException("批量获取实验统计失败: " + e.getMessage(), e);
    }
  }

  /**
   * 根据实验ID更新统计
   * 迁移对应关系: Go语言exptStatsDAOImpl.UpdateByExptID
   */
  @Override
  public void updateByExptId(Long exptId, Long spaceId, ExptStatsEntity stats) {
    try {
      exptStatsMapper.updateByExptId(exptId, spaceId, stats);
    }
    catch (Exception e) {
      throw new BssException("根据实验ID更新统计失败: " + e.getMessage(), e);
    }
  }

  /**
   * 算术操作计数
   * 迁移对应关系: Go语言exptStatsDAOImpl.ArithOperateCount
   */
  @Override
  public void arithOperateCount(Long exptId, Long spaceId, StatsCntArithOp cntArithOp) {
    try {
      if (cntArithOp == null) {
        return;
      }
      if (cntArithOp.getOpStatusCnt() == null || cntArithOp.getOpStatusCnt().isEmpty()) {
        return;
      }
      exptStatsMapper.arithOperateCount(exptId, spaceId, cntArithOp);
    }
    catch (Exception e) {
      throw new BssException("算术操作计数失败: " + e.getMessage(), e);
    }
  }

  /**
   * 保存实验统计
   * 迁移对应关系: Go语言exptStatsDAOImpl.Save
   * 使用先查询再决定插入或更新的方式，兼容所有数据库
   */
  @Override
  public void save(ExptStatsEntity stats) {
    try {
      // 先检查记录是否存在
      boolean exists = exptStatsMapper.existsByExptIdAndSpaceId(stats.getExptId(), stats.getSpaceId());

      if (exists) {
        // 记录存在，执行更新
        exptStatsMapper.updateStats(stats);
      }
      else {
        // 记录不存在，执行插入
        exptStatsMapper.insertStats(stats);
      }
    }
    catch (Exception e) {
      throw new BssException("保存实验统计失败: " + e.getMessage(), e);
    }
  }
}
