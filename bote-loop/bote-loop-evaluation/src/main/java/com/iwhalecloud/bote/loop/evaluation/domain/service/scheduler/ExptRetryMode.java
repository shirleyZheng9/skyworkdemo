package com.iwhalecloud.bote.loop.evaluation.domain.service.scheduler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.component.IConfiger;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.BatchGetEvaluationSetItemsParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationSetItem;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Experiment;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptExecConf;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptItemResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptItemResultRunLog;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptRunMode;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptStats;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptStatus;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnEvaluatorResultRef;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ItemRunState;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Turn;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.TurnRunState;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.event.ExptScheduleEvent;
import com.iwhalecloud.bote.loop.evaluation.domain.event.ExptEventPublisher;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IExperimentRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IExptItemResultRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IExptStatsRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IExptTurnResultRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.service.EvaluationSetItemService;
import com.iwhalecloud.bote.loop.infra.idgen.IIDGenerator;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 实验失败重试模式实现
 * 迁移对应关系: Go语言ExptFailRetryExec
 * - 功能: 处理实验失败重试模式的调度逻辑
 * - 主要方法:
 * * exptStart - 实验开始处理
 * * scanEvalItems - 扫描评估项目
 * * exptEnd - 实验结束处理
 * * publishResult - 发布结果
 * <p>
 * Java实现说明:
 * - 对应Go的ExptFailRetryExec结构体
 * - 使用Spring Component注解
 * - 实现ExptSchedulerMode接口
 */
@Component
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class ExptRetryMode implements ExptSchedulerMode {
  private static final Logger logger = LoggerFactory.getLogger(ExptRetryMode.class);

  private final IExptTurnResultRepo exptTurnResultRepo;
  private final IExptItemResultRepo exptItemResultRepo;
  private final IExptStatsRepo exptStatsRepo;
  private final IIDGenerator idgenerator;
  private final IExperimentRepo exptRepo;
  private final IConfiger configer;
  private final ExptEventPublisher publisher;
  private final ExptBaseExec exptBaseExec;
  private final EvaluationSetItemService evaluationSetItemService;

  @Override
  public ExptRunMode mode() {
    return ExptRunMode.FAIL_RETRY;
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public void exptStart(ExptScheduleEvent event, Experiment expt) {
    try {
      if (ExptStatus.PROCESSING == expt.getStatus()) {
        return;
      }

      List<Long> itemIds = resolveItemIds(event);
      if (itemIds == null || itemIds.isEmpty()) {
        logger.warn("ExptRetryMode.exptStart: no item results found, expt_id: {}", event.getExptId());
        return;
      }

      Map<Long, EvaluationSetItem> evalSetItemMap = loadEvaluationSetItems(event, expt, itemIds);
      List<ExptItemResult> existingItemResults = exptItemResultRepo.batchGet(event.getSpaceId(), event.getExptId(), itemIds);

      List<ExptTurnResult> newTurnResults = createTurnResults(event, existingItemResults, evalSetItemMap);
      if (!newTurnResults.isEmpty()) {
        exptTurnResultRepo.batchCreateNx(newTurnResults);
      }

      updateItemResults(event, itemIds);
      createItemRunLogs(event, itemIds);
      updateStatistics(event);
      updateExperimentStatus(event);
    }
    catch (Exception e) {
      throw new BssException("实验失败重试开始处理失败: " + e.getMessage(), e);
    }
  }

  /**
   * 解析itemIds
   */
  private List<Long> resolveItemIds(ExptScheduleEvent event) {
    List<Long> specifiedItemIds = parseItemIdsFromExt(event);
    if (specifiedItemIds != null && !specifiedItemIds.isEmpty()) {
      return specifiedItemIds;
    }
    return exptItemResultRepo.getItemIdListByExptId(event.getExptId(), event.getSpaceId());
  }

  /**
   * 从ext中解析itemIds
   */
  private List<Long> parseItemIdsFromExt(ExptScheduleEvent event) {
    if (event.getExt() == null) {
      return null;
    }
    String itemIdsStr = event.getExt().get("itemIds");
    if (itemIdsStr == null || itemIdsStr.isEmpty()) {
      return null;
    }
    try {
      return JsonUtil.parseJson(itemIdsStr, new TypeReference<List<Long>>() {
      });
    }
    catch (Exception e) {
      logger.warn("Failed to parse itemIds from ext: {}", itemIdsStr, e);
      return null;
    }
  }

  /**
   * 加载评估集数据项
   */
  private Map<Long, EvaluationSetItem> loadEvaluationSetItems(ExptScheduleEvent event, Experiment expt, List<Long> itemIds) {
    Long evalSetID = expt.getEvalSet().getId();
    Long evalSetVersionID = expt.getEvalSet().getEvaluationSetVersion().getId();

    BatchGetEvaluationSetItemsParam param = BatchGetEvaluationSetItemsParam.builder()
      .spaceId(event.getSpaceId())
      .evaluationSetId(evalSetID)
      .itemIds(itemIds)
      .build();

    if (!evalSetVersionID.equals(evalSetID)) {
      param.setVersionId(evalSetVersionID);
    }

    PageInfo<EvaluationSetItem> evalSetItems = evaluationSetItemService.batchGetEvaluationSetItems(param);
    Map<Long, EvaluationSetItem> itemIdToEvalSetItem = new HashMap<>();
    for (EvaluationSetItem item : evalSetItems.getList()) {
      itemIdToEvalSetItem.put(item.getItemId(), item);
    }
    return itemIdToEvalSetItem;
  }

  /**
   * 创建TurnResult列表
   */
  private List<ExptTurnResult> createTurnResults(ExptScheduleEvent event, List<ExptItemResult> existingItemResults,
                                                 Map<Long, EvaluationSetItem> evalSetItemMap) {
    int turnCnt = calculateTurnCount(existingItemResults, evalSetItemMap);
    List<Long> turnResultIds = idgenerator.genMultiIds(turnCnt);

    List<ExptTurnResult> newTurnResults = new ArrayList<>();
    int idIdx = 0;
    for (ExptItemResult itemResult : existingItemResults) {
      Long itemId = itemResult.getItemId();
      EvaluationSetItem evalSetItem = evalSetItemMap.get(itemId);
      if (evalSetItem == null || evalSetItem.getTurns() == null) {
        continue;
      }

      for (int turnIdx = 0; turnIdx < evalSetItem.getTurns().size(); turnIdx++) {
        Turn turn = evalSetItem.getTurns().get(turnIdx);
        ExptTurnResult etr = ExptTurnResult.builder()
          .id(turnResultIds.get(idIdx))
          .spaceId(event.getSpaceId())
          .exptId(event.getExptId())
          .exptRunId(event.getExptRunId())
          .itemId(itemId)
          .turnId(turn.getId())
          .turnIdx(turnIdx)
          .status(TurnRunState.QUEUEING.getValue())
          .build();
        newTurnResults.add(etr);
        idIdx++;
      }
    }
    return newTurnResults;
  }

  /**
   * 计算Turn总数
   */
  private int calculateTurnCount(List<ExptItemResult> existingItemResults, Map<Long, EvaluationSetItem> evalSetItemMap) {
    int turnCnt = 0;
    for (ExptItemResult itemResult : existingItemResults) {
      EvaluationSetItem evalSetItem = evalSetItemMap.get(itemResult.getItemId());
      if (evalSetItem != null && evalSetItem.getTurns() != null) {
        turnCnt += evalSetItem.getTurns().size();
      }
    }
    return turnCnt;
  }

  /**
   * 更新ItemResult状态
   */
  private void updateItemResults(ExptScheduleEvent event, List<Long> itemIds) {
    Set<Long> itemIDSet = new HashSet<>(itemIds);
    Map<String, Object> updateFields = Map.of(
      "status", ItemRunState.QUEUEING.getValue(),
      "expt_run_id", event.getExptRunId()
    );
    exptItemResultRepo.updateItemsResult(event.getSpaceId(), event.getExptId(),
      new ArrayList<>(itemIDSet), updateFields);
  }

  /**
   * 创建Item运行日志
   */
  private void createItemRunLogs(ExptScheduleEvent event, List<Long> itemIds) {
    Set<Long> itemIDSet = new HashSet<>(itemIds);
    List<Long> itemRunLogIds = idgenerator.genMultiIds(itemIDSet.size());
    List<ExptItemResultRunLog> itemRunLogs = new ArrayList<>();
    int logIdIdx = 0;
    for (Long itemID : itemIDSet) {
      itemRunLogs.add(ExptItemResultRunLog.builder()
        .id(itemRunLogIds.get(logIdIdx))
        .spaceId(event.getSpaceId())
        .exptId(event.getExptId())
        .exptRunId(event.getExptRunId())
        .itemId(itemID)
        .status(ItemRunState.QUEUEING.getValue())
        .build());
      logIdIdx++;
    }
    exptItemResultRepo.batchCreateNxRunLogs(itemRunLogs);
  }

  /**
   * 更新统计信息
   */
  private void updateStatistics(ExptScheduleEvent event) {
    ExptStats got = exptStatsRepo.get(event.getExptId(), event.getSpaceId());
    int pendingCnt = got.getPendingItemCnt() + got.getFailItemCnt() + got.getTerminatedItemCnt() + got.getProcessingItemCnt();
    got.setPendingItemCnt(pendingCnt);
    got.setFailItemCnt(0);
    got.setTerminatedItemCnt(0);
    got.setProcessingItemCnt(0);
    exptStatsRepo.save(got);
    logger.info("ExptFailRetryExec.ExptStart reset pending_cnt: {}, expt_id: {}", pendingCnt, event.getExptId());
  }

  /**
   * 更新实验状态
   */
  private void updateExperimentStatus(ExptScheduleEvent event) {
    Experiment updateExpt = Experiment.builder()
      .status(ExptStatus.PROCESSING)
      .id(event.getExptId())
      .spaceId(event.getSpaceId())
      .build();
    exptRepo.update(updateExpt);
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public ScanEvalItemResult scanEvalItems(ExptScheduleEvent event, Experiment expt) {
    return exptBaseExec.scanEvalItems(event, expt);
  }

  @Override
  public boolean exptEnd(ExptScheduleEvent event, Experiment expt, int toSubmit, int incomplete) {
    if (toSubmit == 0 && incomplete == 0) {
      logger.info("[ExptEval] expt daemon finished, expt_id: {}, expt_run_id: {}", event.getExptId(), event.getExptRunId());
      exptBaseExec.exptEnd(event);
      return false;
    }
    return true;
  }

  @Override
  public void scheduleStart(ExptScheduleEvent event, Experiment expt) {
    // 失败重试模式不需要特殊处理
  }

  @Override
  public void scheduleEnd(ExptScheduleEvent event, Experiment expt, int toSubmit, int incomplete) {
    // 失败重试模式不需要特殊处理
  }

  @Override
  public void nextTick(ExptScheduleEvent event, Experiment expt, boolean nextTick) {
    if (!nextTick) {
      return;
    }
    try {
      ExptExecConf execConf = configer.getExptExecConf(event.getSpaceId());
      Duration interval = Duration.ofSeconds(execConf.getDaemonIntervalSecond());
      publisher.publishExptScheduleEvent(event, interval);
    }
    catch (Exception e) {
      throw new BssException("下次调度失败: " + e.getMessage(), e);
    }
  }

  @Override
  public void publishResult(List<ExptTurnEvaluatorResultRef> turnEvaluatorRefs, ExptScheduleEvent event) {
    exptBaseExec.publishResult(turnEvaluatorRefs);
  }

}
