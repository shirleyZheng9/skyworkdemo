package com.iwhalecloud.bote.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.iwhalecloud.bote.dto.base.PublishRecordDTO;
import com.iwhalecloud.bote.service.publish.IPublishService;
import com.iwhalecloud.bss.litchi.job.AbstractSimpleJob;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据同步状态更新任务
 * <p>定时更新在线发布任务的状态，避免用户未及时刷新发布状态导致任务状态异常</p>
 * @author qian.sisheng
 * @since 2026-04-24
 */
public class DataSyncStatusUpdateJob extends AbstractSimpleJob {

  private static final Logger logger = LoggerFactory.getLogger(DataSyncStatusUpdateJob.class);


  private IPublishService publishService;

  public DataSyncStatusUpdateJob() {
    super("数据同步状态更新任务");
  }

  @Override
  protected void doExecute(ShardingContext shardingContext) {
    if (publishService == null) {
      publishService = SpringUtil.getBean(IPublishService.class);
    }
    List<PublishRecordDTO> records = publishService.queryOnlinePublishRecordWithRunning();
    if (CollectionUtils.isEmpty(records)) {
      return;
    }
    for (PublishRecordDTO record : records) {
      try {
        publishService.updateOnlinePublishStatus(record, null);
      }
      catch (Exception e) {
        if (logger.isErrorEnabled()) {
          logger.error("Failed to update publish status: publishId={}", record.getId(), e);
        }
      }
    }
  }
}
