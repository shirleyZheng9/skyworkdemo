package com.iwhalecloud.bote.service.skill;

/**
 * 由 {@link com.iwhalecloud.bote.service.skill.impl.SkillSquareBulkExportService} 实现，供异步 Runner 调用，避免与广场主服务循环依赖。
 */
public interface SkillSquareBulkExportJobHandler {

  /**
   * 执行大批量分包导出：写 zip、上传、更新 Redis 进度。
   */
  void executeLargeExport(long jobId, Integer top);
}
