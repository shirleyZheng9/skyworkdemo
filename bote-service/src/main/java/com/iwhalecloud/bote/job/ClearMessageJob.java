package com.iwhalecloud.bote.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.iwhalecloud.bote.service.base.IArchiveMessageService;
import com.iwhalecloud.bss.litchi.job.AbstractSimpleJob;
import com.iwhalecloud.bss.litchi.util.SpringUtil;

/**
 * 清除会话消息历史表数据
 *
 * @author qian.sisheng
 * @since 2024-12-6
 */

public class ClearMessageJob extends AbstractSimpleJob {
  private IArchiveMessageService archiveMessageService;

  public ClearMessageJob() {
    super("清除会话消息历史表数据");
  }

  @Override
  protected void doExecute(ShardingContext shardingContext) {
    if (archiveMessageService == null) {
      archiveMessageService = SpringUtil.getBean(IArchiveMessageService.class);
    }
    archiveMessageService.clearHistorySessions();
  }
}
