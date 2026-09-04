package com.iwhalecloud.bote.service.base.impl;

import com.iwhalecloud.bote.common.consts.SceneConsts;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.mapper.agent.SessionStateMapper;
import com.iwhalecloud.bote.mapper.agent.SessionTaskMapper;
import com.iwhalecloud.bote.mapper.chat.SessionClearMapper;
import com.iwhalecloud.bote.service.base.IArchiveMessageService;
import com.iwhalecloud.bote.service.mcp.IMcpToolFileService;
import com.iwhalecloud.bss.litchi.database.consts.DatabaseType;
import com.iwhalecloud.bss.litchi.database.util.DbUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/**
 * 会话消息归档服务实现
 *
 * @author qian.sisheng
 * @since 2024-12-6
 */
@Service
@RequiredArgsConstructor
@SuppressFBWarnings("SQL_INJECTION_SPRING_JDBC")
public class ArchiveMessageServiceImpl implements IArchiveMessageService, InitializingBean {
  private static final Logger logger = LoggerFactory.getLogger(ArchiveMessageServiceImpl.class);
  /** 归档、清理时每次处理的会话数 */
  private static final int BATCH_SIZE = 50;
  /** 最大处理批次数，避免定时任务长时间执行停不下来 */
  private static final int MAX_BATCH_COUNT = 2000;

  private final SessionClearMapper sessionClearMapper;
  private final IMcpToolFileService mcpToolFileService;
  private final SessionStateMapper sessionStateMapper;
  private final SessionTaskMapper sessionTaskMapper;
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
      // 使用一个不存在的会话 ID 检查数据库是否支持 insert into select
      try {
        sessionClearMapper.archiveSession(Collections.singletonList(Long.MIN_VALUE));
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
  public void archiveSessions() {
    Integer keepDays = SystemParameter.ARCHIVE_MESSAGE_TIME.getIntegerValueFromDb();
    // 未配置，或配置为非正数时不清理
    if (keepDays == null || keepDays <= 0) {
      return;
    }

    // 要处理的数据量可能比较大，不使用事务，以避免事务过长、过大

    // 先删除在途表、历史表都存在的数据（未使用事务，归档中途失败会产生异常数据），避免异常数据导致定时任务持续失败
    sessionClearMapper.deleteArchiveFailingSession();
    sessionClearMapper.deleteArchiveFailingMsg();
    sessionClearMapper.deleteArchiveFailingMsgText();
    sessionClearMapper.deleteArchiveFailingMsgFile();

    // TODO 删除会话不存的消息、消息文本、消息文件

    // 分批清理，避免一次处理太多数据（可能会导致 SQL 执行超时）
    // TODO 优化批次大小: 难以预知每个会话的消息数量，会话批次太小会导致执行太慢，批次太大可能导致归档消息时 SQL 执行超时
    // 待归档会话的最大更新时间
    Date maxUpdatedTime = DateUtils.addDays(new Date(), -keepDays);
    int count = 0;
    for (int i = 0; i < MAX_BATCH_COUNT; i++) {
      List<Long> sessionIds = sessionClearMapper.selectSessionIdsForArchive(maxUpdatedTime, BATCH_SIZE);
      if (sessionIds.isEmpty()) {
        break;
      }
      count += sessionIds.size();
      // 归档会话消息
      archiveSessionsBySessionIds(sessionIds);
      if (sessionIds.size() < BATCH_SIZE) {
        break;
      }
    }

    // 删除工作流调试产生的 MCP 工具文件
    mcpToolFileService.deleteTestFiles();
    logger.debug("Archive message finished: sessionCount={}", count);
  }

  /**
   * 归档会话相关表
   */
  private void archiveSessionsBySessionIds(List<Long> sessionIds) {
    // 快速拷贝方式: 使用 insert into select
    if (supportsFastCopy) {
      sessionClearMapper.archiveMsgFile(sessionIds);
      sessionClearMapper.archiveMsgText(sessionIds);
      sessionClearMapper.archiveMsg(sessionIds);
      sessionClearMapper.archiveSession(sessionIds);
    }
    // 慢速拷贝方式，先查出数据，再批量插入
    else {
      archiveTable(sessionIds, sessionClearMapper::selectMsgFileList, sessionClearMapper::batchInsertMsgFileHistory);
      archiveTable(sessionIds, sessionClearMapper::selectMsgTextList, sessionClearMapper::batchInsertMsgTextHistory);
      archiveTable(sessionIds, sessionClearMapper::selectMsgList, sessionClearMapper::batchInsertMsgHistory);
      archiveTable(sessionIds, sessionClearMapper::selectSessionList, sessionClearMapper::batchInsertSessionHistory);
    }

    // 删除数据
    // 注意删除顺序，被依赖的表应放在后面删除
    sessionStateMapper.deleteBySessionIds(sessionIds);
    sessionTaskMapper.deleteTasksBySessionIds(sessionIds);
    sessionClearMapper.deleteMsgFileBySessionIds(sessionIds);
    sessionClearMapper.deleteMsgTextBySessionIds(sessionIds);
    sessionClearMapper.deleteMsgBySessionIds(sessionIds);
    sessionClearMapper.deleteSessionBySessionIds(sessionIds);
  }

  /**
   * 归档单个表
   */
  private <T> void archiveTable(List<Long> sessionIds, Function<List<Long>, List<T>> selectFunc, Consumer<List<T>> insertFunc) {
    List<T> list = selectFunc.apply(sessionIds);
    if (!list.isEmpty()) {
      // 分批插入，避免超出 SQL 参数数量、请求包大小限制
      for (List<T> partitionedList : ListUtils.partition(list, 300)) {
        insertFunc.accept(partitionedList);
      }
    }
  }

  @Override
  public void archiveSession(Long sessionId) {
    archiveSessionsBySessionIds(Collections.singletonList(sessionId));
    mcpToolFileService.deleteBySessionId(sessionId);
  }

  @Override
  public void archiveSessionsByTenantIdAndBotId(@Nullable Long spaceId, Long tenantId, @Nullable Long botId, @Nullable Long botTenantId, Long creatorId) {
    List<Long> sessionIds = sessionClearMapper.selectSessionIdsByBotId(spaceId, tenantId, botId, botTenantId, creatorId);
    if (sessionIds.isEmpty()) {
      return;
    }
    // 分批归档
    for (List<Long> partitionedSessionIds : ListUtils.partition(sessionIds, BATCH_SIZE)) {
      archiveSessionsBySessionIds(partitionedSessionIds);
    }
    mcpToolFileService.deleteBySessionIds(sessionIds);
  }

  @Override
  public void clearHistorySessions() {
    // 借用这个定时任务清理下调试自主规划模式、知识问答模式智能体产生的会话记录，只保留 7 天
    sessionClearMapper.deleteChatMessagesBySessionId(SceneConsts.TEST_CONVERSATION_ID, DateUtils.addDays(new Date(), -7));

    Integer keepDays = SystemParameter.CLEAR_MESSAGE_HIS_TIME.getIntegerValueFromDb();
    // 未配置，或配置为非正数时不清理
    if (keepDays == null || keepDays <= 0) {
      return;
    }

    // 要处理的数据量可能比较大，不使用事务，以避免事务过长、过大

    // 分批清理，避免一次处理太多数据（可能会导致 SQL 执行超时）
    // 待清理会话的最大更新时间
    Date maxUpdatedTime = DateUtils.addDays(new Date(), -keepDays);
    int count = 0;
    for (int i = 0; i < MAX_BATCH_COUNT; i++) {
      List<Long> sessionIds = sessionClearMapper.selectHistorySessionIdsForClear(maxUpdatedTime, BATCH_SIZE);
      if (sessionIds.isEmpty()) {
        break;
      }
      count += sessionIds.size();
      // 注意删除顺序，被依赖的表应放在后面删除
      sessionClearMapper.deleteChatMessagesBySessionIds(sessionIds);
      // 失效消息关联的文件。先标记后删除，删除文件需要连接文件服务器，不放在这里处理以避免影响稳定性
      sessionClearMapper.disableFilesByHistorySessionIds(sessionIds);
      sessionClearMapper.deleteHistoryMsgFileBySessionIds(sessionIds);
      sessionClearMapper.deleteHistoryMsgTextBySessionIds(sessionIds);
      sessionClearMapper.deleteHistoryMsgBySessionIds(sessionIds);
      sessionClearMapper.deleteHistorySessionBySessionIds(sessionIds);

      // 删除消息关联的 MCP 工具文件
      mcpToolFileService.deleteBySessionIds(sessionIds);
      if (sessionIds.size() < BATCH_SIZE) {
        break;
      }
    }

    // 删除自主规划模式、知识问答模式智能体的会话记录。虽然前面根据 sessionId 清理了，但仍会漏掉一些（特殊会话 ID、历史数据）
    sessionClearMapper.deleteChatMessages(maxUpdatedTime);

    // TODO 清理异常数据(历史会话不存在的消息，历史消息不存在的消息文本、消息文件)。注意归档、清理两个定时任务同时运行时会有问题
    logger.debug("Clear message finished: sessionCount={}", count);
  }
}
