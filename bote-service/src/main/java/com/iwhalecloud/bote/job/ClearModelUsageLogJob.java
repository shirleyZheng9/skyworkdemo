package com.iwhalecloud.bote.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.mapper.model.ModelUsageLogMapper;
import com.iwhalecloud.bss.litchi.job.AbstractSimpleJob;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.Date;
import org.apache.commons.lang3.time.DateUtils;

/**
 * 清理模型使用日志定时任务
 *
 * @author chen.linfa
 * @since 2026-04-07
 */
public class ClearModelUsageLogJob extends AbstractSimpleJob {
  /** 每次清理的日志数量 */
  private static final int BATCH_SIZE = 5000;
  /** 最大处理批次数，避免定时任务长时间执行停不下来 */
  private static final int MAX_BATCH_COUNT = 400;

  private ModelUsageLogMapper modelUsageLogMapper;

  public ClearModelUsageLogJob() {
    super("清理模型使用日志");
  }

  @Override
  protected void doExecute(ShardingContext shardingContext) {
    Integer keepDays = SystemParameter.MODEL_USAGE_LOG_HIS_TIME.getIntegerValueFromDb();
    if (keepDays == null || keepDays <= 0) {
      return;
    }
    if (modelUsageLogMapper == null) {
      modelUsageLogMapper = SpringUtil.getBean(ModelUsageLogMapper.class);
    }

    Date maxDate = DateUtils.addDays(new Date(), -keepDays);
    int total = 0;
    for (int i = 0; i < MAX_BATCH_COUNT; i++) {
      int affectedRows = modelUsageLogMapper.clearHistoryByMaxDate(maxDate, BATCH_SIZE);
      total += affectedRows;
      if (affectedRows < BATCH_SIZE) {
        break;
      }
    }
    logger.debug("Deleted model usage logs: rows={}", total);
  }
}
