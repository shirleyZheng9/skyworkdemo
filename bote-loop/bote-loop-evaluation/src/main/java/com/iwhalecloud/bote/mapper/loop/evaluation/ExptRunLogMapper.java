package com.iwhalecloud.bote.mapper.loop.evaluation;

import com.iwhalecloud.bote.entity.loop.evaluation.ExptRunLogEntity;

import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * 实验运行日志Mapper接口
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/mysql/expt_run_log.go
 *
 * @author Generated
 * @since 2025-01-27
 */

public interface ExptRunLogMapper {

  /**
   * 创建实验运行日志
   * 迁移对应关系: Go语言IExptRunLogDAO.Create
   */
  int create(ExptRunLogEntity exptRunLog);

  /**
   * 更新实验运行日志
   * 迁移对应关系: Go语言IExptRunLogDAO.Save
   */
  int save(ExptRunLogEntity exptRunLog);

  /**
   * 更新实验运行日志
   * 迁移对应关系: Go语言IExptRunLogDAO.Update
   */
  int update(@Param("exptId") Long exptId,
             @Param("exptRunId") Long exptRunId,
             @Param("ufields") Map<String, Object> ufields);

  /**
   * 获取实验运行日志
   * 迁移对应关系: Go语言IExptRunLogDAO.Get
   */
  ExptRunLogEntity get(@Param("exptId") Long exptId, @Param("exptRunId") Long exptRunId);
}
