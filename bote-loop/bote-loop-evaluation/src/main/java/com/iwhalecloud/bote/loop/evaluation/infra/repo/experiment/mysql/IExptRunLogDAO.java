package com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql;

import com.iwhalecloud.bote.entity.loop.evaluation.ExptRunLogEntity;
import java.util.Map;

/**
 * 实验运行日志DAO接口
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/mysql/expt_run_log.go
 *
 * @author Generated
 * @since 2025-01-27
 */
public interface IExptRunLogDAO {

  /**
   * 创建实验运行日志
   * 迁移对应关系: Go语言IExptRunLogDAO.Create
   *
   * @param exptRunLog 实验运行日志
   */
  void create(ExptRunLogEntity exptRunLog);

  /**
   * 保存实验运行日志
   * 迁移对应关系: Go语言IExptRunLogDAO.Save
   *
   * @param exptRunLog 实验运行日志
   */
  void save(ExptRunLogEntity exptRunLog);

  /**
   * 更新实验运行日志
   * 迁移对应关系: Go语言IExptRunLogDAO.Update
   *
   * @param exptId 实验ID
   * @param exptRunId 实验运行ID
   * @param ufields 更新字段
   */
  void update(Long exptId, Long exptRunId, Map<String, Object> ufields);

  /**
   * 获取实验运行日志
   * 迁移对应关系: Go语言IExptRunLogDAO.Get
   *
   * @param exptId 实验ID
   * @param exptRunId 实验运行ID
   * @return 实验运行日志
   */
  ExptRunLogEntity get(Long exptId, Long exptRunId);
}
