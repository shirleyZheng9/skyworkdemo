package com.iwhalecloud.bote.service.model.impl;

import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.entity.model.ModelUsageLogEntity;
import com.iwhalecloud.bote.mapper.model.ModelUsageLogMapper;
import com.iwhalecloud.bote.service.model.IModelUsageLogService;
import com.iwhalecloud.bss.litchi.database.consts.DatabaseType;
import com.iwhalecloud.bss.litchi.database.util.DbUtil;
import com.iwhalecloud.bss.litchi.disruptor.DisruptorUtil;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

/**
 * 大模型使用量日志服务
 *
 * @author chen.linfa
 * @since 2026-04-07
 */
@Service
@RequiredArgsConstructor
@SuppressFBWarnings("SQL_INJECTION_SPRING_JDBC")
public class ModelUsageLogServiceImpl implements IModelUsageLogService, InitializingBean {
  private static final Logger logger = LoggerFactory.getLogger(ModelUsageLogServiceImpl.class);
  /** 每次归档日志数量 */
  private static final int BATCH_SIZE = 5000;
  /** 最大处理批次数，避免定时任务长时间执行停不下来 */
  private static final int MAX_BATCH_COUNT = 400;

  private final ModelUsageLogMapper modelUsageLogMapper;
  /** 是否支持快速拷贝(insert into select 语法) */
  private boolean supportsFastCopy;

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public void afterPropertiesSet() {
    DatabaseType databaseType = DbUtil.getDatabaseType();
    // PostgreSQL, MySQL, Oracle 都支持 insert into select，但部分 MySQL 衍生品不支持
    if (databaseType == DatabaseType.UDAL || databaseType == DatabaseType.ZDAAS) {
      this.supportsFastCopy = false;
    }
    else {
      // 使用一个不存在的日志 ID 检查数据库是否支持 insert into select
      try {
        modelUsageLogMapper.archiveByLogIds(Collections.singletonList(Long.MIN_VALUE));
        this.supportsFastCopy = true;
      }
      catch (Exception e) {
        // 有异常表示不支持
        logger.warn("Database not support insert into select: type={}, error={}", databaseType, e.getMessage());
        this.supportsFastCopy = false;
      }
    }
  }

  @Override
  public void addLog(ModelUsageLogEntity log) {
    log.setLogId(IDUtils.nextId());
    DisruptorUtil.getInstance().produce(log);
  }

  @Override
  public void archiveLogs() {
    Integer keepDays = SystemParameter.MODEL_USAGE_LOG_KEEP_DAYS.getIntegerValueFromDb();
    if (keepDays == null || keepDays <= 0) {
      return;
    }

    Date maxDate = DateUtils.addDays(new Date(), -keepDays);
    for (int i = 0; i < MAX_BATCH_COUNT; i++) {
      List<Long> logIds = modelUsageLogMapper.selectLogIdsForArchive(maxDate, BATCH_SIZE);
      if (logIds.isEmpty()) {
        break;
      }
      archiveByLogIds(logIds);
      modelUsageLogMapper.deleteByLogIds(logIds);
      if (logIds.size() < BATCH_SIZE) {
        break;
      }
    }
  }

  /**
   * 归档日志
   */
  private void archiveByLogIds(List<Long> logIds) {
    // 快速拷贝方式: 使用 insert into select
    if (supportsFastCopy) {
      modelUsageLogMapper.archiveByLogIds(logIds);
    }
    // 慢速拷贝方式，先查出数据，再批量插入
    else {
      List<ModelUsageLogEntity> logList = modelUsageLogMapper.selectLogListByLogIds(logIds);
      if (!logList.isEmpty()) {
        modelUsageLogMapper.batchInsertHistory(logList);
      }
    }
  }
}
