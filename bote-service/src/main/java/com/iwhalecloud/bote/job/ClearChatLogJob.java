package com.iwhalecloud.bote.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.mapper.chat.ChatTraceLogMapper;
import com.iwhalecloud.bss.litchi.job.AbstractSimpleJob;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.Date;
import org.apache.commons.lang3.time.DateUtils;

/**
 * 清理会话日志定时任务
 *
 * @author bianjp
 * @since 2025-01-17
 */
public class ClearChatLogJob extends AbstractSimpleJob {
  /** 每次清理的日志数量 */
  private static final int BATCH_SIZE = 10000;
  /** 最大处理批次数，避免定时任务长时间执行停不下来 */
  private static final int MAX_BATCH_COUNT = 200;

  private ChatTraceLogMapper chatTraceLogMapper;

  public ClearChatLogJob() {
    super("清理会话日志");
  }

  @Override
  protected void doExecute(ShardingContext shardingContext) {
    Integer keepDays = SystemParameter.CHAT_LOG_KEEP_DAYS.getIntegerValueFromDb();
    if (keepDays == null || keepDays <= 0) {
      return;
    }
    if (chatTraceLogMapper == null) {
      chatTraceLogMapper = SpringUtil.getBean(ChatTraceLogMapper.class);
    }

    Date maxDate = DateUtils.addDays(new Date(), -keepDays);
    int total = 0;
    // 分批删除，限制每次删除的数量以避免 SQL 执行超时（包含多个大字段，删除比较慢）
    for (int i = 0; i < MAX_BATCH_COUNT; i++) {
      int affectedRows = chatTraceLogMapper.deleteByMaxDate(maxDate, BATCH_SIZE);
      total += affectedRows;
      if (affectedRows < BATCH_SIZE) {
        break;
      }
    }
    logger.debug("Deleted chat logs: rows={}", total);
  }
}
