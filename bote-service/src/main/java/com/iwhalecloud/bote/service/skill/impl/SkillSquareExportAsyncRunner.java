package com.iwhalecloud.bote.service.skill.impl;

import com.iwhalecloud.bote.cache.SkillSquareBulkExportCache;
import com.iwhalecloud.bote.service.skill.SkillSquareBulkExportJobHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 技能广场大批量导出异步执行
 */
@Component
@RequiredArgsConstructor
public class SkillSquareExportAsyncRunner {
  private static final Logger logger = LoggerFactory.getLogger(SkillSquareExportAsyncRunner.class);

  private final SkillSquareBulkExportJobHandler jobHandler;
  private final SkillSquareBulkExportCache bulkExportCache;

  public void run(long jobId, Integer top) {
    try {
      bulkExportCache.markRunning(jobId);
      jobHandler.executeLargeExport(jobId, top);
      bulkExportCache.markSuccess(jobId);
    }
    catch (Exception e) {
      logger.error("skill square bulk export job failed, jobId={}", jobId, e);
      bulkExportCache.markFailed(jobId, e.getMessage());
    }
  }
}
