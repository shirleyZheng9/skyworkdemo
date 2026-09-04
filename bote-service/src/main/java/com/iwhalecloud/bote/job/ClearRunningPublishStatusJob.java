package com.iwhalecloud.bote.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.iwhalecloud.bote.service.publish.IPublishService;
import com.iwhalecloud.bss.litchi.job.AbstractSimpleJob;
import com.iwhalecloud.bss.litchi.util.SpringUtil;

/**
 * 定时任务：清理长时间处于发布中的数据
 *
 * @author chen.linfa
 * @since 2025-07-28
 */
public class ClearRunningPublishStatusJob extends AbstractSimpleJob {
  private IPublishService publishService;

  public ClearRunningPublishStatusJob() {
    super("清理长时间处于发布中数据的定时任务");
  }

  @Override
  protected void doExecute(ShardingContext shardingContext) {
    if (publishService == null) {
      publishService = SpringUtil.getBean(IPublishService.class);
    }
    publishService.clearRunningPublishStatus();
  }
}
