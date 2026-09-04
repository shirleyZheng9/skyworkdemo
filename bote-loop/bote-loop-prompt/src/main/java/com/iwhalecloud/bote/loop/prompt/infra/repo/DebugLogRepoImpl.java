package com.iwhalecloud.bote.loop.prompt.infra.repo;

import com.iwhalecloud.bote.entity.loop.prompt.PromptDebugLogEntity;
import com.iwhalecloud.bote.loop.infra.idgen.IIDGenerator;
import com.iwhalecloud.bote.loop.prompt.domain.entity.DebugLog;
import com.iwhalecloud.bote.loop.prompt.domain.repo.IDebugLogRepo;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.ListDebugHistoryParam;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.ListDebugHistoryResult;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.ListParam;
import com.iwhalecloud.bote.loop.prompt.infra.repo.mysql.IDebugLogDAO;
import com.iwhalecloud.bote.loop.prompt.infra.repo.mysql.convertor.DebugLogConvertor;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 调试日志仓库实现
 * 迁移对应关系: Go语言repo.DebugLogRepoImpl
 * - 功能: 调试日志的数据访问实现
 * - 主要方法:
 * * saveDebugLog - 保存调试日志
 * * listDebugHistory - 列表查询调试历史
 * <p>
 * Java实现说明:
 * - 对应Go的repo.DebugLogRepoImpl结构体
 * - 使用Spring Data JPA实现数据访问
 * - 使用Spring事务管理
 * - 集成ID生成器
 * <p>
 * 技术栈迁移:
 * - Go GORM -> Java Spring Data JPA
 * - Go context.Context -> Java方法参数
 * - Go error返回 -> Java异常处理
 * - Go切片类型 -> Java List
 * - Go map类型 -> Java Map
 * - Go time.Time -> Java LocalDateTime
 */
@Component
@RequiredArgsConstructor
public class DebugLogRepoImpl implements IDebugLogRepo {
  private final IIDGenerator idGenerator;
  private final IDebugLogDAO debugLogDAO;

  @Override
  public void saveDebugLog(DebugLog debugLog) {
    if (debugLog == null) {
      return;
    }

    Long id = idGenerator.genId();
    PromptDebugLogEntity debugLogPO = DebugLogConvertor.debugLogDO2PO(debugLog);
    debugLogPO.setId(id);
    debugLogPO.setCreatedAt(new Date());
    debugLogPO.setUpdatedAt(new Date());

    // 默认值
    if (debugLogPO.getDeletedAt() == null) {
      debugLogPO.setDeletedAt(0L);
    }

    debugLogDAO.save(debugLogPO);
  }

  @Override
  public ListDebugHistoryResult listDebugHistory(ListDebugHistoryParam param) {
    int pageSize = param.getPageSize();

    // 计算时间范围
    LocalDateTime startAfter = LocalDateTime.now().minusDays(param.getDaysLimit());
    long startAfterMillis = startAfter.toInstant(ZoneOffset.UTC).toEpochMilli();

    ListParam listParam = ListParam.builder()
      .promptId(param.getPromptId())
      .userId(param.getUserId())
      .startBefore(param.getPageToken())
      .startAfter(startAfterMillis)
      // 支持function call多步调试记录串联后，一次完整的bug可能对应多条记录，为了准确分页，每次调试历史只取第一步记录
      .debugStep(1)
      .limit(pageSize + 1)
      .build();

    List<PromptDebugLogEntity> debugLogs = debugLogDAO.list(listParam);
    if (debugLogs.isEmpty()) {
      return ListDebugHistoryResult.builder().build();
    }

    Long nextPageToken = null;
    boolean hasMore = false;
    if (debugLogs.size() > pageSize) {
      hasMore = true;
      nextPageToken = debugLogs.get(pageSize).getStartedAt();
      debugLogs = debugLogs.subList(0, pageSize);
    }

    // 构建调试日志映射
    Map<Long, PromptDebugLogEntity> debugLogMap = debugLogs.stream()
      .filter(Objects::nonNull)
      .collect(Collectors.toMap(
        PromptDebugLogEntity::getDebugId,
        log -> log,
        (existing, replacement) -> existing
      ));

    // 获取所有调试ID
    List<Long> debugIDs = debugLogs.stream()
      .filter(Objects::nonNull)
      .map(PromptDebugLogEntity::getDebugId)
      .collect(Collectors.toList());

    // 查询可能存在的多步调试记录
    ListParam allLogsParam = ListParam.builder()
      .debugIds(debugIDs)
      .build();
    List<PromptDebugLogEntity> allLogs = debugLogDAO.list(allLogsParam);

    // 用最后一步的结束时间覆盖第一步的结束时间
    for (PromptDebugLogEntity stepLog : allLogs) {
      if (stepLog == null) {
        continue;
      }
      PromptDebugLogEntity log = debugLogMap.get(stepLog.getDebugId());
      if (log != null && stepLog.getEndedAt() > log.getEndedAt()) {
        log.setEndedAt(stepLog.getEndedAt());
        log.setCostMs(stepLog.getEndedAt() - log.getStartedAt());
        log.setInputTokens(log.getInputTokens() + stepLog.getInputTokens());
        log.setOutputTokens(log.getOutputTokens() + stepLog.getOutputTokens());
      }
    }

    // 按开始时间降序排序
    List<PromptDebugLogEntity> sortedLogs = new ArrayList<>(debugLogMap.values());
    sortedLogs.sort((a, b) -> Long.compare(b.getStartedAt(), a.getStartedAt()));

    return ListDebugHistoryResult.builder()
      .debugHistory(DebugLogConvertor.debugLogsPO2DO(sortedLogs))
      .nextPageToken(nextPageToken)
      .hasMore(hasMore)
      .build();
  }
}
