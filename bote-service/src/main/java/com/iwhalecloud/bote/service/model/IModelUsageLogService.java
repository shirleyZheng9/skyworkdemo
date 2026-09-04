package com.iwhalecloud.bote.service.model;

import com.iwhalecloud.bote.entity.model.ModelUsageLogEntity;

/**
 * 大模型使用量日志服务
 *
 * @author chen.linfa
 * @since 2026-04-07
 */
public interface IModelUsageLogService {
  /**
   * 记录大模型使用量
   */
  void addLog(ModelUsageLogEntity log);

  /**
   * 归档大模型使用量日志
   */
  void archiveLogs();
}
