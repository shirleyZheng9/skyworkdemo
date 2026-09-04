package com.iwhalecloud.bote.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.iwhalecloud.bote.service.base.IArchiveMessageService;
import com.iwhalecloud.bss.litchi.job.AbstractSimpleJob;
import com.iwhalecloud.bss.litchi.util.SpringUtil;

/**
 * 定时任务：会话消息迁移历史表
 *
 * @author qian.sisheng
 * @since 2024-12-5
 */
public class ArchiveMessageJob extends AbstractSimpleJob {
  private IArchiveMessageService archiveMessageService;

  public ArchiveMessageJob() {
    super("会话消息迁移会话消息历史表");
  }

  @Override
  protected void doExecute(ShardingContext shardingContext) {
    if (archiveMessageService == null) {
      archiveMessageService = SpringUtil.getBean(IArchiveMessageService.class);
    }
    archiveMessageService.archiveSessions();
  }
}
