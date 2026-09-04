package com.iwhalecloud.bote.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.iwhalecloud.bote.service.model.IModelUsageLogService;
import com.iwhalecloud.bss.litchi.job.AbstractSimpleJob;
import com.iwhalecloud.bss.litchi.util.SpringUtil;

/**
 * 归档模型使用日志定时任务
 *
 * @author chen.linfa
 * @since 2026-04-15
 */
public class ArchiveModelUsageLogJob extends AbstractSimpleJob {
  private IModelUsageLogService modelUsageLogService;

  public ArchiveModelUsageLogJob() {
    super("归档模型使用日志");
  }

  @Override
  protected void doExecute(ShardingContext shardingContext) {
    if (modelUsageLogService == null) {
      modelUsageLogService = SpringUtil.getBean(IModelUsageLogService.class);
    }
    modelUsageLogService.archiveLogs();
  }
}
