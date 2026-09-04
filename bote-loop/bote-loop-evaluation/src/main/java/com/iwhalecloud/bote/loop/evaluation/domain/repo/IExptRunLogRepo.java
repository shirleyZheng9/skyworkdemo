package com.iwhalecloud.bote.loop.evaluation.domain.repo;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptRunLog;
import java.util.Map;

/**
 * 实验运行日志仓库接口
 * 对应Go: IExptRunLogRepo
 */
public interface IExptRunLogRepo {

  /**
   * 创建实验运行日志
   * 对应Go: Create(ctx context.Context, exptRunLog *entity.ExptRunLog) error
   */
  void create(ExptRunLog exptRunLog);

  /**
   * 保存实验运行日志
   * 对应Go: Save(ctx context.Context, exptRunLog *entity.ExptRunLog) error
   */
  void save(ExptRunLog exptRunLog);

  /**
   * 更新实验运行日志
   * 对应Go: Update(ctx context.Context, exptID, exptRunID int64, ufields map[string]any) error
   */
  void update(Long exptId, Long exptRunId, Map<String, Object> ufields);

  /**
   * 获取实验运行日志
   * 对应Go: Get(ctx context.Context, exptID, exptRunID int64) (*entity.ExptRunLog, error)
   */
  ExptRunLog get(Long exptId, Long exptRunId);
}
