package com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.impl;

import com.iwhalecloud.bote.entity.loop.evaluation.ExptRunLogEntity;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.IExptRunLogDAO;
import com.iwhalecloud.bote.mapper.loop.evaluation.ExptRunLogMapper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 实验运行日志DAO实现类
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/mysql/expt_run_log.go
 *
 * @author Generated
 * @since 2025-01-27
 */
@Repository
@RequiredArgsConstructor
public class ExptRunLogDAOImpl implements IExptRunLogDAO {
  private final ExptRunLogMapper exptRunLogMapper;

  /**
   * 创建实验运行日志
   * 迁移对应关系: Go语言ExptRunLogDAOImpl.Create
   */
  @Override
  public void create(ExptRunLogEntity exptRunLog) {
    try {
      exptRunLogMapper.create(exptRunLog);
    }
    catch (Exception e) {
      throw new BssException("创建实验运行日志失败: " + e.getMessage(), e);
    }
  }

  /**
   * 保存实验运行日志
   * 迁移对应关系: Go语言ExptRunLogDAOImpl.Save
   */
  @Override
  public void save(ExptRunLogEntity exptRunLog) {
    try {
      exptRunLogMapper.save(exptRunLog);
    }
    catch (Exception e) {
      throw new BssException("保存实验运行日志失败: " + e.getMessage(), e);
    }
  }

  /**
   * 更新实验运行日志
   * 迁移对应关系: Go语言ExptRunLogDAOImpl.Update
   */
  @Override
  public void update(Long exptId, Long exptRunId, Map<String, Object> ufields) {
    try {
      exptRunLogMapper.update(exptId, exptRunId, ufields);
    }
    catch (Exception e) {
      throw new BssException("更新实验运行日志失败: " + e.getMessage(), e);
    }
  }

  /**
   * 获取实验运行日志
   * 迁移对应关系: Go语言ExptRunLogDAOImpl.Get
   */
  @Override
  public ExptRunLogEntity get(Long exptId, Long exptRunId) {
    try {
      return exptRunLogMapper.get(exptId, exptRunId);
    }
    catch (Exception e) {
      throw new BssException("获取实验运行日志失败: " + e.getMessage(), e);
    }
  }
}
