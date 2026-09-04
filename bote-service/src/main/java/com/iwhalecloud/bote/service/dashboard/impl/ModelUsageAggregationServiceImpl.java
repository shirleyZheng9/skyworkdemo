package com.iwhalecloud.bote.service.dashboard.impl;

import com.iwhalecloud.bote.dto.dashboard.ModelUsageAggregationCheckpointDTO;
import com.iwhalecloud.bote.dto.dashboard.ModelUsageLogIncrementDTO;
import com.iwhalecloud.bote.dto.dashboard.ModelUsageStatusCandidateDTO;
import com.iwhalecloud.bote.mapper.dashboard.ModelUsageAggregationMapper;
import com.iwhalecloud.bote.service.dashboard.IModelUsageAggregationService;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 按日志入库时间增量汇总模型调用量。
 *
 * <p>汇总数据和检查点在同一事务提交：失败时二者一起回滚，任务重试不会漏计。
 * 使用 created_time 而非模型请求 start_time，避免异步日志延迟入库后落到已推进的游标之前。</p>
 */
@Service
public class ModelUsageAggregationServiceImpl implements IModelUsageAggregationService {
  /** 检查点业务编码；修改编码会产生新的统计起点，因此上线后不能随意变更。 */
  static final String TASK_CODE = "dashboard_model_usage_total_v1";

  /** 每批只读取轻量日志字段，控制事务时长和应用内存占用。 */
  private static final int BATCH_SIZE = 2000;

  /** 每次任务最多校准 200 个模型，避免周期性扫描全部汇总记录。 */
  private static final int STATUS_BATCH_SIZE = 200;

  /**
   * 给异步日志写入预留一分钟安全窗口。
   * 日志可能晚于模型请求落库；如果直接统计到当前时刻，迟到日志可能插入已经推进的
   * 游标之前，从而被永久遗漏。
   */
  private static final long CUTOFF_DELAY_MILLIS = 60_000L;

  private final ModelUsageAggregationMapper mapper;

  public ModelUsageAggregationServiceImpl(ModelUsageAggregationMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public int aggregateNextBatch() {
    // 对检查点记录加行锁，同一时刻只允许一个节点消费该任务的下一批日志。
    ModelUsageAggregationCheckpointDTO checkpoint = mapper.selectCheckpointForUpdate(TASK_CODE);
    if (checkpoint == null) {
      // DML 未预置检查点时的安全兜底：从首次执行时开始，不回扫口径不完整的旧数据。
      mapper.insertCheckpoint(TASK_CODE, new Date());
      return 0;
    }

    // 只读取一分钟以前已经稳定入库的数据，降低异步日志迟到导致漏数的风险。
    Date cutoffTime = new Date(System.currentTimeMillis() - CUTOFF_DELAY_MILLIS);
    List<ModelUsageLogIncrementDTO> logs = mapper.selectPendingLogs(
      checkpoint.getStatisticsStartTime(),
      checkpoint.getLastCreatedTime(),
      checkpoint.getLastLogId(),
      cutoffTime,
      BATCH_SIZE
    );
    if (logs == null || logs.isEmpty()) {
      return 0;
    }

    /*
     * SQL 已显式排除空入库时间。这里再次校验 Mapper 返回值，避免测试桩、兼容数据库或后续
     * SQL 改动意外返回空游标，并最终把 NULL 写入检查点。没有完整复合游标的日志无法确定
     * 增量顺序，只能按异常数据忽略；正常日志写入路径会同时保证 log_id 和 created_time 非空。
     */
    List<ModelUsageLogIncrementDTO> consumableLogs = new ArrayList<>(logs.size());
    for (ModelUsageLogIncrementDTO log : logs) {
      if (log != null && log.getCreatedTime() != null && log.getLogId() != null) {
        consumableLogs.add(log);
      }
    }
    if (consumableLogs.isEmpty()) {
      return 0;
    }

    /*
     * 在应用内按“租户 + 模型”合并本批日志，使数据库每个模型最多执行一次累加，
     * 避免逐条 UPDATE 汇总表。LinkedHashMap 保持稳定处理顺序，方便问题排查。
     */
    Map<ModelKey, Increment> grouped = new LinkedHashMap<>();
    for (ModelUsageLogIncrementDTO log : consumableLogs) {
      // 历史异常数据缺少租户或模型时无法确定归属，因此忽略该条并继续推进检查点。
      if (log.getTenantId() == null || log.getModelId() == null) {
        continue;
      }
      grouped.computeIfAbsent(new ModelKey(log.getTenantId(), log.getModelId()), ignored -> new Increment())
        .add(log);
    }
    for (Map.Entry<ModelKey, Increment> entry : grouped.entrySet()) {
      ModelKey key = entry.getKey();
      Increment value = entry.getValue();

      // 优先更新已有汇总行；只有首次出现的“租户 + 模型”组合才执行插入。
      int updated = mapper.incrementTotal(
        key.tenantId(), key.modelId(), value.modelName, value.productType, value.count
      );
      if (updated == 0) {
        mapper.insertTotal(
          key.tenantId(), key.modelId(), value.modelName, value.productType, value.count
        );
      }
    }

    /*
     * 查询结果按 created_time、log_id 稳定排序，最后一条即下一批的游标起点。
     * 汇总写入与检查点推进在同一事务中：任一步失败都会整体回滚，重试不会漏计。
     */
    ModelUsageLogIncrementDTO last = consumableLogs.get(consumableLogs.size() - 1);
    mapper.updateCheckpoint(TASK_CODE, last.getCreatedTime(), last.getLogId());
    return consumableLogs.size();
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public int refreshNextModelStatusBatch() {
    // 按最久未检查时间获取固定数量记录，形成循环校准，避免每次扫描整张汇总表。
    List<ModelUsageStatusCandidateDTO> candidates = mapper.selectStatusCandidates(STATUS_BATCH_SIZE);
    if (candidates == null || candidates.isEmpty()) {
      return 0;
    }
    for (ModelUsageStatusCandidateDTO item : candidates) {
      /*
       * 状态区分有效、停用、等待、删除和未知；来源独立区分 CUSTOM/PLATFORM。
       * 状态变化后，后续轮询会覆盖旧快照，例如模型恢复启用时重新校准为 ACTIVE。
       */
      mapper.updateModelStatus(
        item.getTenantId(), item.getModelId(), item.getModelStatus(), item.getModelSource(),
        item.getProductType());
    }
    return candidates.size();
  }

  private record ModelKey(Long tenantId, Long modelId) {
  }

  private static final class Increment {
    private long count;
    private String modelName;
    private String productType;

    private void add(ModelUsageLogIncrementDTO log) {
      count++;

      // 只用非空名称更新快照，防止异常日志把已有模型名称覆盖为空。
      if (StringUtils.isNotBlank(log.getModelName())) {
        modelName = log.getModelName();
      }
      if (StringUtils.isNotBlank(log.getProductType())) {
        productType = log.getProductType();
      }
    }
  }
}
