package com.iwhalecloud.bote.loop.evaluation.domain.service.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.BatchGetEvaluationSetItemsParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ColumnEvalSetField;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ColumnEvaluator;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Content;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetRecord;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationSet;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationSetItem;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationSetVersion;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Evaluator;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorRecord;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorResults;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Experiment;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExperimentResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExperimentResultListResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExperimentTurnPayload;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptCalculateStats;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptEvaluatorRef;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptEvaluatorVersionRef;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptItemResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptItemResultRunLog;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptItemResultState;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptStats;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnEvaluatorResultRef;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResultFilterEntityDO;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResultFilterKeyMapping;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResultRunLog;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.FieldSchema;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.FieldTypeMapping;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.IEvaluatorVersion;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ItemResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ItemRunState;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ItemSystemInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ItemTurnID;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.MGetExperimentResultParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Page;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.RunError;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Session;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.StatsCntArithOp;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Turn;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.TurnEvalSet;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.TurnEvaluatorOutput;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.TurnResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.TurnRunState;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.TurnSystemInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.TurnTargetOutput;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.event.ExptTurnResultFilterEvent;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.event.UpsertExptTurnResultFilterType;
import com.iwhalecloud.bote.loop.evaluation.domain.event.ExptEventPublisher;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IEvaluatorRecordRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IExperimentRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IExptItemResultRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IExptStatsRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IExptTurnResultFilterRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IExptTurnResultRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.service.EvaluationSetItemService;
import com.iwhalecloud.bote.loop.evaluation.domain.service.EvaluationSetService;
import com.iwhalecloud.bote.loop.evaluation.domain.service.EvaluationSetVersionService;
import com.iwhalecloud.bote.loop.evaluation.domain.service.EvaluatorRecordService;
import com.iwhalecloud.bote.loop.evaluation.domain.service.EvaluatorService;
import com.iwhalecloud.bote.loop.evaluation.domain.service.ExptResultService;
import com.iwhalecloud.bote.loop.evaluation.domain.service.IEvalTargetService;
import com.iwhalecloud.bote.loop.infra.idgen.IIDGenerator;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 实验结果服务实现类
 * 迁移对应关系: Go语言backend/modules/evaluation/domain/service/expt_result_impl.go
 */
@Component
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class ExptResultServiceImpl implements ExptResultService {
  private static final Logger logger = LoggerFactory.getLogger(ExptResultServiceImpl.class);
  private final IExptItemResultRepo exptItemResultRepo;
  private final IExptTurnResultRepo exptTurnResultRepo;
  private final IExptStatsRepo exptStatsRepo;
  private final IExperimentRepo experimentRepo;
  private final IIDGenerator idgen;
  private final IExptTurnResultFilterRepo exptTurnResultFilterRepo;
  private final IEvalTargetService evalTargetService;
  private final EvaluationSetVersionService evaluationSetVersionService;
  private final EvaluationSetService evaluationSetService;
  private final EvaluatorService evaluatorService;
  private final EvaluatorRecordService evaluatorRecordService;
  private final IEvaluatorRecordRepo evaluatorRecordRepo;
  private final EvaluationSetItemService evaluationSetItemService;
  private final ExptEventPublisher publisher;

  @Override
  public List<ExptTurnResult> getExptItemTurnResults(Long exptId, Long itemId, Long spaceId, Session session) {
    return getExptItemTurnResults(exptId, itemId, spaceId, null, session);
  }

  @Override
  public List<ExptTurnResult> getExptItemTurnResults(Long exptId, Long itemId, Long spaceId, Long exptRunId, Session session) {
    try {
      List<ExptTurnResult> turnResults = exptTurnResultRepo.getItemTurnResults(exptId, itemId, spaceId, exptRunId);
      List<Long> turnResultIDs = turnResults.stream()
        .map(ExptTurnResult::getId)
        .collect(Collectors.toList());
      List<ExptTurnEvaluatorResultRef> refs = exptTurnResultRepo.batchGetTurnEvaluatorResultRef(spaceId, turnResultIDs);
      Map<Long, Map<Long, Long>> turnEvaluatorVerIDToResultID = new HashMap<>();
      for (ExptTurnEvaluatorResultRef ref : refs) {
        turnEvaluatorVerIDToResultID.computeIfAbsent(ref.getExptTurnResultId(), k -> new HashMap<>())
          .put(ref.getEvaluatorVersionId(), ref.getEvaluatorVersionId());
      }
      List<ExptTurnResult> result = new ArrayList<>();
      for (ExptTurnResult tr : turnResults) {
        Map<Long, Long> evalVerID2ResultID = turnEvaluatorVerIDToResultID.getOrDefault(tr.getId(), new HashMap<>());
        tr.setEvaluatorResults(EvaluatorResults.builder()
          .evalVerIdToResId(evalVerID2ResultID)
          .build());
        result.add(tr);
      }
      return result;
    }
    catch (Exception e) {
      throw new BssException("获取实验项目轮次结果失败: " + e.getMessage(), e);
    }
  }

  @Override
  public List<ExptTurnEvaluatorResultRef> recordItemRunLogs(Long exptId, Long exptRunId, Long itemId, Long spaceId) {
    try {
      ExptItemResultRunLog itemRunLog = exptItemResultRepo.getItemRunLog(exptId, exptRunId, itemId, spaceId);
      List<ExptTurnResultRunLog> turnRunLogs = exptTurnResultRepo.getItemTurnRunLogs(exptId, exptRunId, itemId, spaceId);
      List<ExptTurnResult> turnResults = exptItemResultRepo.getItemTurnResults(spaceId, exptId, itemId);
      List<ExptItemResult> itemResults = exptItemResultRepo.batchGet(spaceId, exptId, List.of(itemId));
      ExptItemResult itemResult = itemResults.getFirst();
      StatsCntArithOp statsCntOp = StatsCntArithOp.builder()
        .opStatusCnt(new HashMap<>())
        .build();
      statsCntOp.getOpStatusCnt().put(itemResult.getStatus(),
        statsCntOp.getOpStatusCnt().getOrDefault(itemResult.getStatus(), 0) - 1);
      statsCntOp.getOpStatusCnt().put(ItemRunState.values()[itemRunLog.getStatus()],
        statsCntOp.getOpStatusCnt().getOrDefault(ItemRunState.values()[itemRunLog.getStatus()], 0) + 1);
      Map<Long, ExptTurnResultRunLog> turn2RunLog = turnRunLogs.stream()
        .collect(Collectors.toMap(ExptTurnResultRunLog::getTurnId, rl -> rl));
      logger.info("[ExptEval] expt item result with recording run_log, expt_id={}, expt_run_id={}, item_id={}, cnt_op={}",
        exptId, exptRunId, itemId, statsCntOp);
      List<ExptTurnEvaluatorResultRef> turnEvaluatorRefs = new ArrayList<>();
      Map<Long, ExptTurnResult> turn2Result = turnResults.stream()
        .collect(Collectors.toMap(ExptTurnResult::getTurnId, t -> t));
      for (Map.Entry<Long, ExptTurnResult> entry : turn2Result.entrySet()) {
        Long tid = entry.getKey();
        ExptTurnResult result = entry.getValue();
        ExptTurnResultRunLog rl = turn2RunLog.get(tid);
        if (rl == null) {
          throw new BssException("RecordItemRunLogs found null turn log result, expt_id: " + exptId +
            ", expt_run_id: " + exptRunId + ", item: " + itemId + ", tid: " + tid);
        }
        result.setStatus(rl.getStatus().getValue());
        result.setTargetResultId(rl.getTargetResultId());
        result.setErrMsg(rl.getErrMsg());
        result.setLogId(rl.getLogId());
        result.setExptRunId(rl.getExptRunId());
        turnEvaluatorRefs.addAll(newTurnEvaluatorResultRefs(result.getExptId(), result.getId(), spaceId, rl.getEvaluatorResultIds()));
      }
      if (!turnEvaluatorRefs.isEmpty()) {
        List<Long> ids = idgen.genMultiIds(turnEvaluatorRefs.size());
        for (int idx = 0; idx < turnEvaluatorRefs.size(); idx++) {
          turnEvaluatorRefs.get(idx).setId(ids.get(idx));
        }
        exptTurnResultRepo.createTurnEvaluatorRefs(turnEvaluatorRefs);
      }
      exptTurnResultRepo.saveTurnResults(turnResults);
      Map<String, Object> updateFields = new HashMap<>();
      updateFields.put("status", itemRunLog.getStatus());
      updateFields.put("log_id", itemRunLog.getLogId());
      updateFields.put("err_msg", itemRunLog.getErrMsg());
      exptItemResultRepo.updateItemsResult(spaceId, exptId, List.of(itemId), updateFields);
      Map<String, Object> updateItemRunLogFields = new HashMap<>();
      updateItemRunLogFields.put("result_state", ExptItemResultState.RESULTED.ordinal());
      exptItemResultRepo.updateItemRunLog(exptId, exptRunId, List.of(itemId), updateItemRunLogFields, spaceId);
      exptStatsRepo.arithOperateCount(exptId, spaceId, statsCntOp);
      return turnEvaluatorRefs;
    }
    catch (Exception e) {
      throw new BssException("记录项目运行日志失败: " + e.getMessage(), e);
    }
  }

  @Override
  public ExperimentResultListResult mGetExperimentResult(MGetExperimentResultParam param) {
    try {
      Long spaceId = param.getSpaceId();
      List<Long> exptIds = param.getExptIds();
      Long baselineExptId = param.getBaseExptId();
      Long baseExptId = baselineExptId != null ? baselineExptId : exptIds.getFirst();
      Experiment baseExpt = experimentRepo.getById(baseExptId, spaceId);
      Long baseExptEvalSetVersionID = baseExpt.getEvalSetVersionId();
      List<ColumnEvaluator> columnEvaluators = getColumnEvaluators(spaceId, exptIds);
      List<ColumnEvalSetField> columnEvalSetFields = getColumnEvalSetFields(spaceId, baseExpt.getEvalSetId(), baseExptEvalSetVersionID);
      // 获取baseline 该分页的itemResults以提取itemIds
      Page page = param.getPage() != null ? param.getPage() : new Page(1, 20);
      List<Long> itemIds = param.getItemIds();
      List<ExptItemResult> itemResults;
      long total;
      if (itemIds == null || itemIds.isEmpty()) {
        PageInfo<ExptItemResult> itemResultPageInfo = exptItemResultRepo.listItemResultsByExptId(baseExptId, spaceId, page, false);
        itemIds = itemResultPageInfo.getList().stream()
          .map(ExptItemResult::getItemId)
          .distinct()
          .collect(Collectors.toList());
        total = itemResultPageInfo.getTotal();
        itemResults = itemResultPageInfo.getList();
      }
      else {
        total = itemIds.size();
        itemResults = exptItemResultRepo.batchGet(spaceId, baseExptId, itemIds);
      }
      // 调用listTurnResultByItemIds获取turnResults
      List<ExptTurnResult> turnResults = exptTurnResultRepo.batchGet(spaceId, baseExptId, itemIds);
      // 将turnResults转换为ItemResult列表
      List<ItemResult> itemResultList = buildItemResultsFromTurnResults(spaceId, baseExpt, turnResults, itemResults);
      // 获取总数
      return ExperimentResultListResult.builder()
        .columnEvaluators(columnEvaluators)
        .columnEvalSetFields(columnEvalSetFields)
        .itemResults(itemResultList)
        .total(total)
        .build();
    }
    catch (Exception e) {
      throw new BssException("批量获取实验结果失败: " + e.getMessage(), e);
    }
  }

  private List<ItemResult> buildItemResultsFromTurnResults(Long spaceId, Experiment baseExpt, List<ExptTurnResult> turnResults, List<ExptItemResult> itemResults) {
    Map<Long, ExptItemResult> itemIdToItemResult = buildItemIdToItemResultMap(itemResults);
    Map<Long, List<ExptTurnResult>> itemIdToTurnResults = turnResults.stream()
      .collect(Collectors.groupingBy(ExptTurnResult::getItemId));
    Map<Long, Map<Long, EvaluatorRecord>> turnResultID2EvaluatorRecords = buildEvaluatorRecordsMap(spaceId, turnResults);
    Map<Long, Map<Long, TurnEvalSet>> itemIDTurnID2TurnEvalSet = buildEvalSetMap(spaceId, baseExpt, itemResults);
    Map<Long, TurnTargetOutput> turnResultID2TargetOutput = buildTargetOutputMap(spaceId, turnResults);
    return buildItemResults(itemIdToItemResult, itemIdToTurnResults, turnResultID2EvaluatorRecords,
      itemIDTurnID2TurnEvalSet, turnResultID2TargetOutput);
  }

  private Map<Long, ExptItemResult> buildItemIdToItemResultMap(List<ExptItemResult> itemResults) {
    return itemResults.stream()
      .collect(Collectors.toMap(
        ExptItemResult::getItemId,
        item -> item,
        (existing, replacement) -> existing,
        java.util.LinkedHashMap::new
      ));
  }

  private Map<Long, Map<Long, EvaluatorRecord>> buildEvaluatorRecordsMap(Long spaceId, List<ExptTurnResult> turnResults) {
    if (turnResults == null || turnResults.isEmpty()) {
      return new HashMap<>();
    }
    Long experimentId = turnResults.getFirst().getExptId();
    List<Long> itemIds = turnResults.stream()
      .map(ExptTurnResult::getItemId)
      .distinct()
      .collect(Collectors.toList());
    List<Long> turnIds = turnResults.stream()
      .map(ExptTurnResult::getTurnId)
      .distinct()
      .collect(Collectors.toList());
    List<EvaluatorRecord> evaluatorRecords = evaluatorRecordRepo.batchGetByExperimentItemTurn(spaceId, experimentId, itemIds, turnIds);
    Map<String, Map<Long, EvaluatorRecord>> itemTurnId2EvaluatorRecords = new HashMap<>();
    for (EvaluatorRecord evaluatorRecord : evaluatorRecords) {
      String key = evaluatorRecord.getItemId() + "_" + evaluatorRecord.getExperimentRunId();
      itemTurnId2EvaluatorRecords.computeIfAbsent(key, k -> new HashMap<>())
        .put(evaluatorRecord.getEvaluatorVersionId(), evaluatorRecord);
    }
    Map<Long, Map<Long, EvaluatorRecord>> turnResultID2EvaluatorRecords = new HashMap<>();
    for (ExptTurnResult turnResult : turnResults) {
      String key = turnResult.getItemId() + "_" + turnResult.getExptRunId();
      Map<Long, EvaluatorRecord> evaluatorRecordsForTurn = itemTurnId2EvaluatorRecords.get(key);
      if (evaluatorRecordsForTurn != null && !evaluatorRecordsForTurn.isEmpty()) {
        turnResultID2EvaluatorRecords.put(turnResult.getId(), evaluatorRecordsForTurn);
      }
    }
    return turnResultID2EvaluatorRecords;
  }

  private Map<Long, Map<Long, TurnEvalSet>> buildEvalSetMap(Long spaceId, Experiment baseExpt, List<ExptItemResult> itemResults) {
    List<Long> itemIds = itemResults.stream()
      .map(ExptItemResult::getItemId)
      .distinct()
      .collect(Collectors.toList());
    BatchGetEvaluationSetItemsParam evalSetParam = BatchGetEvaluationSetItemsParam.builder()
      .spaceId(spaceId)
      .evaluationSetId(baseExpt.getEvalSetId())
      .itemIds(itemIds)
      .build();
    Long evalSetVersionID = baseExpt.getEvalSetVersionId();
    if (!evalSetVersionID.equals(baseExpt.getEvalSetId())) {
      evalSetParam.setVersionId(evalSetVersionID);
    }
    PageInfo<EvaluationSetItem> evalSetItems = evaluationSetItemService.batchGetEvaluationSetItems(evalSetParam);
    Map<Long, Map<Long, TurnEvalSet>> itemIDTurnID2TurnEvalSet = new HashMap<>();
    for (EvaluationSetItem item : evalSetItems.getList()) {
      for (Turn turn : item.getTurns()) {
        itemIDTurnID2TurnEvalSet.computeIfAbsent(item.getItemId(), k -> new HashMap<>())
          .put(turn.getId(), TurnEvalSet.builder().turn(turn).build());
      }
    }
    return itemIDTurnID2TurnEvalSet;
  }

  private Map<Long, TurnTargetOutput> buildTargetOutputMap(Long spaceId, List<ExptTurnResult> turnResults) {
    List<Long> targetResultIDs = turnResults.stream()
      .map(ExptTurnResult::getTargetResultId)
      .filter(id -> id != null && id > 0)
      .distinct()
      .collect(Collectors.toList());
    Map<Long, TurnTargetOutput> turnResultID2TargetOutput = new HashMap<>();
    if (targetResultIDs.isEmpty()) {
      return turnResultID2TargetOutput;
    }
    List<EvalTargetRecord> targetRecords = evalTargetService.batchGetRecordByIds(spaceId, targetResultIDs);
    // 构建 targetRecordId -> targetRecord 的映射
    Map<Long, EvalTargetRecord> targetRecordMap = targetRecords.stream()
      .collect(Collectors.toMap(EvalTargetRecord::getId, record -> record));
    // 直接遍历 turnResults，为每个 turnResult 设置对应的 targetOutput
    // 使用 turnResultId 作为 key，避免重复键问题
    for (ExptTurnResult turnResult : turnResults) {
      if (turnResult.getTargetResultId() != null && turnResult.getTargetResultId() > 0) {
        EvalTargetRecord targetRecord = targetRecordMap.get(turnResult.getTargetResultId());
        if (targetRecord != null) {
          turnResultID2TargetOutput.put(turnResult.getId(), TurnTargetOutput.builder()
            .evalTargetRecord(targetRecord)
            .build());
        }
      }
    }
    return turnResultID2TargetOutput;
  }

  private List<ItemResult> buildItemResults(Map<Long, ExptItemResult> itemIdToItemResult,
                                            Map<Long, List<ExptTurnResult>> itemIdToTurnResults,
                                            Map<Long, Map<Long, EvaluatorRecord>> turnResultID2EvaluatorRecords,
                                            Map<Long, Map<Long, TurnEvalSet>> itemIDTurnID2TurnEvalSet,
                                            Map<Long, TurnTargetOutput> turnResultID2TargetOutput) {
    List<ItemResult> result = new ArrayList<>();
    for (Map.Entry<Long, ExptItemResult> entry : itemIdToItemResult.entrySet()) {
      Long itemId = entry.getKey();
      ExptItemResult itemResultPO = entry.getValue();
      ItemResult itemResultDO = buildItemResult(itemId, itemResultPO);
      List<ExptTurnResult> itemTurnResults = itemIdToTurnResults.getOrDefault(itemId, new ArrayList<>());
      for (ExptTurnResult turnResult : itemTurnResults) {
        TurnResult turnResultDO = buildTurnResult(turnResult, turnResultID2EvaluatorRecords,
          itemIDTurnID2TurnEvalSet, turnResultID2TargetOutput, itemId);
        itemResultDO.getTurnResults().add(turnResultDO);
      }
      // 按创建时间排序turnResults（降序：从新到旧）
      itemResultDO.getTurnResults().sort((a, b) -> {
        if (a.getCreatedAt() == null && b.getCreatedAt() == null) {
          return 0;
        }
        if (a.getCreatedAt() == null) {
          return 1; // null值排在后面
        }
        if (b.getCreatedAt() == null) {
          return -1; // null值排在后面
        }
        return b.getCreatedAt().compareTo(a.getCreatedAt()); // 降序：b.compareTo(a)
      });
      result.add(itemResultDO);
    }
    return result;
  }

  private ItemResult buildItemResult(Long itemId, ExptItemResult itemResultPO) {
    ItemResult itemResultDO = ItemResult.builder()
      .itemId(itemId)
      .turnResults(new ArrayList<>())
      .itemIndex(Long.valueOf(itemResultPO.getItemIdx()))
      .build();
    itemResultDO.setSystemInfo(ItemSystemInfo.builder()
      .runState(itemResultPO.getStatus())
      .build());
    return itemResultDO;
  }

  private TurnResult buildTurnResult(ExptTurnResult turnResult,
                                     Map<Long, Map<Long, EvaluatorRecord>> turnResultID2EvaluatorRecords,
                                     Map<Long, Map<Long, TurnEvalSet>> itemIDTurnID2TurnEvalSet,
                                     Map<Long, TurnTargetOutput> turnResultID2TargetOutput,
                                     Long itemId) {
    ExperimentTurnPayload payload = buildExperimentTurnPayload(turnResult, turnResultID2EvaluatorRecords,
      itemIDTurnID2TurnEvalSet, turnResultID2TargetOutput, itemId);
    ExperimentResult experimentResult = ExperimentResult.builder()
      .experimentId(turnResult.getExptId())
      .payload(payload)
      .build();
    return TurnResult.builder()
      .turnId(turnResult.getTurnId())
      .experimentResults(List.of(experimentResult))
      .turnIndex((long) turnResult.getTurnIdx())
      .experimentRunId(turnResult.getExptRunId())
      .createdAt(turnResult.getCreatedAt())
      .build();
  }

  private ExperimentTurnPayload buildExperimentTurnPayload(ExptTurnResult turnResult,
                                                           Map<Long, Map<Long, EvaluatorRecord>> turnResultID2EvaluatorRecords,
                                                           Map<Long, Map<Long, TurnEvalSet>> itemIDTurnID2TurnEvalSet,
                                                           Map<Long, TurnTargetOutput> turnResultID2TargetOutput,
                                                           Long itemId) {
    TurnEvaluatorOutput evaluatorOutput = buildTurnEvaluatorOutput(turnResult, turnResultID2EvaluatorRecords);
    TurnEvalSet evalSet = itemIDTurnID2TurnEvalSet.getOrDefault(itemId, new HashMap<>())
      .get(turnResult.getTurnId());
    TurnTargetOutput targetOutput = turnResultID2TargetOutput.get(turnResult.getId());
    TurnSystemInfo systemInfo = buildTurnSystemInfo(turnResult);
    return ExperimentTurnPayload.builder()
      .turnId(turnResult.getTurnId())
      .evalSet(evalSet)
      .targetOutput(targetOutput)
      .evaluatorOutput(evaluatorOutput)
      .systemInfo(systemInfo)
      .build();
  }

  private TurnEvaluatorOutput buildTurnEvaluatorOutput(ExptTurnResult turnResult,
                                                       Map<Long, Map<Long, EvaluatorRecord>> turnResultID2EvaluatorRecords) {
    Map<Long, EvaluatorRecord> evaluatorRecordsMap = turnResultID2EvaluatorRecords.getOrDefault(turnResult.getId(), new HashMap<>());
    return TurnEvaluatorOutput.builder()
      .evaluatorRecords(evaluatorRecordsMap)
      .build();
  }

  private TurnSystemInfo buildTurnSystemInfo(ExptTurnResult turnResult) {
    TurnRunState turnRunState = convertToTurnRunState(turnResult.getStatus());
    RunError error = buildRunError(turnResult.getErrMsg());
    return TurnSystemInfo.builder()
      .turnRunState(turnRunState)
      .logId(turnResult.getLogId())
      .error(error)
      .build();
  }

  private TurnRunState convertToTurnRunState(Integer status) {
    if (status == null) {
      return null;
    }
    try {
      return TurnRunState.fromValue(status);
    }
    catch (IllegalArgumentException e) {
      logger.warn("Invalid TurnRunState value: {}", status);
      return null;
    }
  }

  private RunError buildRunError(String errMsg) {
    if (errMsg == null || errMsg.isEmpty()) {
      return null;
    }
    return RunError.builder()
      .message(errMsg)
      .build();
  }

  @Override
  public List<ExptStats> mGetStats(List<Long> exptIds, Long spaceId, Session session) {
    try {
      return exptStatsRepo.mGet(exptIds, spaceId);
    }
    catch (Exception e) {
      throw new BssException("批量获取统计信息失败: " + e.getMessage(), e);
    }
  }

  @Override
  public ExptStats getStats(Long exptId, Long spaceId, Session session) {
    try {
      List<ExptStats> stats = mGetStats(List.of(exptId), spaceId, session);
      return stats.getFirst();
    }
    catch (Exception e) {
      throw new BssException("获取统计信息失败: " + e.getMessage(), e);
    }
  }

  @Override
  public void createStats(ExptStats exptStats) {
    exptStatsRepo.create(exptStats);
  }

  @Override
  public ExptCalculateStats calculateStats(Long exptId, Long spaceId, Session session) {
    try {
      ItemStats itemStats = calculateItemStats(exptId, spaceId);
      TurnStats turnStats = calculateTurnStats(exptId, spaceId);
      ExptCalculateStats stats = buildCalculateStats(itemStats, turnStats);
      logCalculateStatsResult(exptId, turnStats, stats);
      return stats;
    }
    catch (Exception e) {
      throw new BssException("计算统计信息失败: " + e.getMessage(), e);
    }
  }

  private ItemStats calculateItemStats(Long exptId, Long spaceId) {
    int maxLoop = 10000;
    int limit = 100;
    int ioffset = 1;

    ItemStats itemStats = new ItemStats(0, 0, 0, 0, 0);
    for (int i = 0; i < maxLoop; i++) {
      PageInfo<ExptItemResult> itemResultList = exptItemResultRepo.listItemResultsByExptId(exptId, spaceId, new Page(ioffset, limit), false);
      if (itemResultList.getList() == null || itemResultList.getList().isEmpty()) {
        return itemStats;
      }
      itemStats = calculateItemStats(itemResultList.getList(), itemStats);
      ioffset++;
    }
    return itemStats;
  }

  public static ItemStats calculateItemStats(List<ExptItemResult> itemResultList, ItemStats itemStats) {
    int pendingCnt = itemStats.pendingCnt;
    int failCnt = itemStats.failCnt;
    int successCnt = itemStats.successCnt;
    int processingCnt = itemStats.processingCnt;
    int terminatedCnt = itemStats.terminatedCnt;
    for (ExptItemResult item : itemResultList) {
      switch (item.getStatus()) {
        case SUCCESS:
          successCnt++;
          break;
        case FAIL:
          failCnt++;
          break;
        case TERMINAL:
          terminatedCnt++;
          break;
        case QUEUEING:
          pendingCnt++;
          break;
        case PROCESSING:
          processingCnt++;
          break;
        default:
          break;
      }
    }
    return new ItemStats(pendingCnt, failCnt, successCnt, processingCnt, terminatedCnt);
  }

  @Override
  public void manualUpsertExptTurnResultFilter(Long spaceId, Long exptId, List<Long> itemIds) {
    try {
      List<Experiment> expts = experimentRepo.mGetById(List.of(exptId), spaceId);
      if (expts.isEmpty()) {
        throw new BssException("实验不存在");
      }
      Experiment expt = expts.getFirst();
      int evaluatorCount = expt.getEvaluatorVersionRef().size();
      List<Long> mappingIds = idgen.genMultiIds(evaluatorCount);
      List<ExptTurnResultFilterKeyMapping> exptTurnResultFilterKeyMappings = new ArrayList<>();
      for (int i = 0; i < expt.getEvaluatorVersionRef().size(); i++) {
        ExptEvaluatorVersionRef ref = expt.getEvaluatorVersionRef().get(i);
        exptTurnResultFilterKeyMappings.add(ExptTurnResultFilterKeyMapping.builder()
          .id(mappingIds.get(i))
          .spaceId(spaceId)
          .exptId(exptId)
          .fromField(String.valueOf(ref.getEvaluatorVersionId()))
          .toKey("key" + (i + 1))
          .fieldType(FieldTypeMapping.EVALUATOR)
          .build());
      }
      insertExptTurnResultFilterKeyMappings(exptTurnResultFilterKeyMappings);
      ExptTurnResultFilterEvent event = ExptTurnResultFilterEvent.builder()
        .experimentId(exptId)
        .spaceId(spaceId)
        .build();
      publisher.publishExptTurnResultFilterEvent(event, Duration.ofSeconds(3L));
    }
    catch (Exception e) {
      throw new BssException("手动更新实验结果过滤条件失败: " + e.getMessage(), e);
    }
  }

  @Override
  public void upsertExptTurnResultFilter(Long spaceId, Long exptId, List<Long> itemIds) {
    try {
      validateUpsertParameters(spaceId, exptId);
      List<ExptTurnResult> allTurnResults = fetchAllTurnResults(spaceId, exptId, itemIds);
      if (allTurnResults.isEmpty()) {
        return;
      }
      List<Long> finalItemIds = extractUniqueItemIds(allTurnResults);
      List<ExptItemResult> itemResults = exptItemResultRepo.batchGet(spaceId, exptId, finalItemIds);
      Map<String, ExptTurnResultFilterKeyMapping> evaluatorMapping = buildEvaluatorMapping(spaceId, exptId);
      List<ExptTurnResultFilterEntityDO> exptTurnResultFilters = buildTurnResultFilters(
        spaceId, exptId, allTurnResults, itemResults, evaluatorMapping);
      exptTurnResultFilterRepo.save(exptTurnResultFilters);
    }
    catch (Exception e) {
      throw new BssException("更新实验结果过滤条件失败: " + e.getMessage(), e);
    }
  }

  private void validateUpsertParameters(Long spaceId, Long exptId) {
    if (spaceId == null || exptId == null || exptId == 0) {
      throw new BssException("UpsertExptTurnResultFilter: invalid space_id or expt_id");
    }
  }

  private List<ExptTurnResult> fetchAllTurnResults(Long spaceId, Long exptId, List<Long> itemIds) {
    int limit = 200;
    int offset = 1;
    int maxLoop = 10000;
    int loopCnt = 0;
    List<ExptTurnResult> allTurnResults = new ArrayList<>();
    while (true) {
      if (loopCnt >= maxLoop) {
        throw new BssException("UpsertExptTurnResultFilter: 超过最大循环次数，可能存在死循环，已查" + allTurnResults.size() + "条");
      }
      List<ExptTurnResult> turnResults = exptTurnResultRepo.listTurnResultByItemIds(spaceId, exptId, itemIds, null, new Page(offset, limit), false);
      if (turnResults.isEmpty()) {
        break;
      }
      allTurnResults.addAll(turnResults);
      if (allTurnResults.size() >= turnResults.size()) {
        break;
      }
      offset++;
      loopCnt++;
    }
    return allTurnResults;
  }

  private List<Long> extractUniqueItemIds(List<ExptTurnResult> allTurnResults) {
    Map<Long, Boolean> itemIDMap = new HashMap<>();
    for (ExptTurnResult turnResult : allTurnResults) {
      itemIDMap.put(turnResult.getItemId(), true);
    }
    return new ArrayList<>(itemIDMap.keySet());
  }

  private Map<String, ExptTurnResultFilterKeyMapping> buildEvaluatorMapping(Long spaceId, Long exptId) {
    List<ExptTurnResultFilterKeyMapping> exptTurnResultFilterKeyMappings =
      exptTurnResultFilterRepo.getExptTurnResultFilterKeyMappings(spaceId, exptId);
    Map<String, ExptTurnResultFilterKeyMapping> evaluatorMap = new HashMap<>();
    for (ExptTurnResultFilterKeyMapping mapping : exptTurnResultFilterKeyMappings) {
      if (mapping.getFieldType() == FieldTypeMapping.EVALUATOR) {
        evaluatorMap.put(mapping.getFromField(), mapping);
      }
    }
    return evaluatorMap;
  }

  private List<ExptTurnResultFilterEntityDO> buildTurnResultFilters(Long spaceId, Long exptId,
                                                                    List<ExptTurnResult> allTurnResults, List<ExptItemResult> itemResults,
                                                                    Map<String, ExptTurnResultFilterKeyMapping> evaluatorMapping) {
    MGetExperimentResultParam param = MGetExperimentResultParam.builder()
      .spaceId(spaceId)
      .exptIds(List.of(exptId))
      .build();
    PayloadBuilder payloadBuilder = new PayloadBuilder(param, exptId, allTurnResults, itemResults,
      experimentRepo, exptTurnResultRepo, evalTargetService, evaluatorRecordService,
      evaluationSetItemService, evaluatorMapping);
    return payloadBuilder.buildTurnResultFilter();
  }

  @Override
  public void compareExptTurnResultFilters(Long spaceId, Long exptId, List<Long> itemIds, Integer retryTimes) {
    try {
      List<Experiment> exptDO = experimentRepo.mGetById(List.of(exptId), spaceId);
      String createdDate = exptDO.getFirst().getStartAt().toString().substring(0, 10);
      List<ExptTurnResultFilterEntityDO> exptTurnResultFilters = exptTurnResultFilterRepo.getByExptIdItemIds(
        String.valueOf(spaceId), String.valueOf(exptId), createdDate,
        itemIds.stream().map(String::valueOf).collect(Collectors.toList()));
      Map<String, ExptTurnResultFilterEntityDO> turnKey2ExptTurnResultFilter = createTurnKeyToFilterMap(exptTurnResultFilters);
      for (Map.Entry<String, ExptTurnResultFilterEntityDO> entry : turnKey2ExptTurnResultFilter.entrySet()) {
        String turnKey = entry.getKey();
        int maxRetryTimes = 3;
        if (retryTimes >= maxRetryTimes) {
          logger.warn("CompareExptTurnResultFilters finish, diff exist, retryTimes >= maxRetryTimes, turnKey: {}", turnKey);
        }
        else {
          logger.info("CompareExptTurnResultFilters finish, diff exist, retrying, turnKey: {}", turnKey);
          ExptTurnResultFilterEvent event = ExptTurnResultFilterEvent.builder()
            .experimentId(exptId)
            .spaceId(spaceId)
            .itemId(List.of(itemIds.getFirst()))
            .retryTimes(retryTimes + 1)
            .filterType(UpsertExptTurnResultFilterType.CHECK)
            .build();
          publisher.publishExptTurnResultFilterEvent(event, Duration.ofSeconds(10L));
        }
      }
    }
    catch (Exception e) {
      throw new BssException("比较实验结果过滤条件失败: " + e.getMessage(), e);
    }
  }

  // 私有辅助方法
  private List<ColumnEvaluator> getColumnEvaluators(Long spaceId, List<Long> exptIds) {
    try {
      // 获取实验的评估器引用
      List<ExptEvaluatorRef> evaluatorRef = experimentRepo.getEvaluatorRefByExptIds(exptIds, spaceId);
      // 去重
      Map<Long, Boolean> evaluatorVersionIDMap = new HashMap<>();
      for (ExptEvaluatorRef ref : evaluatorRef) {
        evaluatorVersionIDMap.put(ref.getEvaluatorVersionId(), true);
      }
      List<Long> evaluatorVersionIDs = new ArrayList<>(evaluatorVersionIDMap.keySet());
      // 获取评估器版本信息
      List<Evaluator> evaluatorVersions = evaluatorService.batchGetEvaluatorVersion(spaceId, evaluatorVersionIDs, true);
      List<ColumnEvaluator> columnEvaluators = new ArrayList<>();
      for (Evaluator e : evaluatorVersions) {
        IEvaluatorVersion evaluatorVersion = e.getEvaluatorVersion();
        if (evaluatorVersion == null || !evaluatorVersionIDs.contains(evaluatorVersion.getId())) {
          continue;
        }
        ColumnEvaluator columnEvaluator = ColumnEvaluator.builder()
          .evaluatorVersionId(evaluatorVersion.getId())
          .evaluatorId(e.getId())
          .evaluatorType(e.getEvaluatorType())
          .name(e.getName())
          .version(evaluatorVersion.getVersion())
          .description(e.getDescription())
          .build();
        columnEvaluators.add(columnEvaluator);
      }
      return columnEvaluators;
    }
    catch (Exception e) {
      throw new BssException("获取列评估器失败: " + e.getMessage(), e);
    }
  }

  private List<ColumnEvalSetField> getColumnEvalSetFields(Long spaceId, Long evalSetId, Long evalSetVersionId) {
    try {
      EvaluationSetVersion version;
      if (evalSetId.equals(evalSetVersionId)) {
        // 如果evalSetID == evalSetVersionID，直接获取评估集
        EvaluationSet evalSet = evaluationSetService.getEvaluationSet(spaceId, evalSetId, true);
        version = evalSet.getEvaluationSetVersion();
      }
      else {
        // 否则获取评估集版本
        version = evaluationSetVersionService.getEvaluationSetVersion(spaceId, evalSetVersionId, true).getVersion();
      }
      List<FieldSchema> fieldSchema = new ArrayList<>();
      if (version != null && version.getEvaluationSetSchema() != null) {
        fieldSchema = version.getEvaluationSetSchema().getFieldSchemas();
      }
      List<ColumnEvalSetField> columnEvalSetFields = new ArrayList<>();
      if (fieldSchema != null) {
        for (FieldSchema field : fieldSchema) {
          columnEvalSetFields.add(ColumnEvalSetField.builder()
            .key(field.getKey())
            .name(field.getName())
            .description(field.getDescription())
            .contentType(field.getContentType())
            .textSchema(field.getTextSchema())
            .build());
        }
      }
      return columnEvalSetFields;
    }
    catch (Exception e) {
      throw new BssException("获取列评估集字段失败: " + e.getMessage(), e);
    }
  }

  private List<ExptTurnEvaluatorResultRef> newTurnEvaluatorResultRefs(Long exptId, Long exptTurnResultId, Long spaceId, EvaluatorResults evaluatorResults) {
    List<ExptTurnEvaluatorResultRef> refs = new ArrayList<>();
    for (Map.Entry<Long, Long> entry : evaluatorResults.getEvalVerIdToResId().entrySet()) {
      Long evalVerId = entry.getKey();
      Long evalResId = entry.getValue();
      refs.add(ExptTurnEvaluatorResultRef.builder()
        .id(0L)
        .exptId(exptId)
        .spaceId(spaceId)
        .exptTurnResultId(exptTurnResultId)
        .evaluatorVersionId(evalVerId)
        .evaluatorResultId(evalResId)
        .build());
    }
    return refs;
  }

  public void insertExptTurnResultFilterKeyMappings(List<ExptTurnResultFilterKeyMapping> mappings) {
    try {
      exptTurnResultFilterRepo.insertExptTurnResultFilterKeyMappings(mappings);
    }
    catch (Exception e) {
      throw new BssException("插入实验结果过滤键映射失败: " + e.getMessage(), e);
    }
  }

  private Map<String, ExptTurnResultFilterEntityDO> createTurnKeyToFilterMap(List<ExptTurnResultFilterEntityDO> filters) {
    Map<String, ExptTurnResultFilterEntityDO> turnKey2Filter = new HashMap<>();
    for (ExptTurnResultFilterEntityDO filter : filters) {
      String turnKey = filter.getExptId() + "_" + filter.getItemId() + "_" + filter.getTurnId();
      turnKey2Filter.put(turnKey, filter);
    }
    return turnKey2Filter;
  }

  private TurnStats calculateTurnStats(Long exptId, Long spaceId) {
    int maxLoop = 10000;
    int limit = 100;
    int offset = 1;
    int total = 0;
    int cnt = 0;
    List<ItemTurnID> incompleteTurns = new ArrayList<>();
    for (int i = 0; i < maxLoop; i++) {
      List<ExptTurnResult> results = exptTurnResultRepo.listTurnResult(spaceId, exptId, null, new Page(offset, limit), false);
      if (results == null || results.isEmpty()) {
        break;
      }
      total = results.size();
      cnt += results.size();
      offset++;
      for (ExptTurnResult tr : results) {
        if (isIncompleteTurn(tr.getStatus())) {
          incompleteTurns.add(ItemTurnID.builder()
            .turnId(tr.getTurnId())
            .itemId(tr.getItemId())
            .build());
        }
      }
    }
    return new TurnStats(total, cnt, incompleteTurns);
  }

  private boolean isIncompleteTurn(int status) {
    TurnRunState turnStatus = TurnRunState.values()[status];
    return turnStatus == TurnRunState.QUEUEING || turnStatus == TurnRunState.PROCESSING;
  }

  private ExptCalculateStats buildCalculateStats(ItemStats itemStats, TurnStats turnStats) {
    return ExptCalculateStats.builder()
      .pendingItemCnt(itemStats.pendingCnt)
      .failItemCnt(itemStats.failCnt)
      .successItemCnt(itemStats.successCnt)
      .processingItemCnt(itemStats.processingCnt)
      .terminatedItemCnt(itemStats.terminatedCnt)
      .incompleteTurnIds(turnStats.incompleteTurns)
      .build();
  }

  private void logCalculateStatsResult(Long exptId, TurnStats turnStats, ExptCalculateStats stats) {
    logger.info("ExptStatsImpl.CalculateStats scan turn result done, expt_id: {}, total_cnt: {}, incomplete_cnt: {}, total: {}, stats: {}",
      exptId, turnStats.cnt, turnStats.incompleteTurns.size(), turnStats.total, stats);
  }

  // 内部类定义
  public static class PayloadBuilder {
    private final MGetExperimentResultParam param;
    private final Long baseExptId;
    private final List<ExptTurnResult> turnResultDAOs;
    private final List<ExptItemResult> itemResultDAOs;
    private final IExperimentRepo experimentRepo;
    private final IExptTurnResultRepo exptTurnResultRepo;
    private final IEvalTargetService evalTargetService;
    private final EvaluatorRecordService evaluatorRecordService;
    private final EvaluationSetItemService evaluationSetItemService;
    private final Map<String, ExptTurnResultFilterKeyMapping> exptTurnResultFilterKeyMappingEvaluatorMap;
    // 需要分实验获取的数据范围
    private final List<Long> itemIDs; // itemID列表 有序
    private final Map<Long, List<Long>> itemID2TurnIDs; // itemID -> turnIDs列表 turnIDs有序
    private final Map<Long, Boolean> itemIDMap; // 去重
    private final Map<Long, Map<Long, Long>> itemIDTurnIDTurnIndex; // itemID -> turnID -> turnIndex
    private final Map<Long, ExptItemResult> itemIDItemResultPO;
    private final Map<Long, Boolean> turnIDMap;
    private List<ExptTurnResultFilterEntityDO> exptTurnResultFilters;
    private List<ExptResultBuilder> exptResultBuilders; // 每个实验的结果builder以及build result

    public PayloadBuilder(MGetExperimentResultParam param, Long baseExptId, List<ExptTurnResult> turnResultDAOs,
                          List<ExptItemResult> itemResultDAOs, IExperimentRepo experimentRepo, IExptTurnResultRepo exptTurnResultRepo,
                          IEvalTargetService evalTargetService, EvaluatorRecordService evaluatorRecordService,
                          EvaluationSetItemService evaluationSetItemService,
                          Map<String, ExptTurnResultFilterKeyMapping> exptTurnResultFilterKeyMappingEvaluatorMap) {
      this.param = param;
      this.baseExptId = baseExptId;
      this.turnResultDAOs = turnResultDAOs;
      this.itemResultDAOs = itemResultDAOs;
      this.experimentRepo = experimentRepo;
      this.exptTurnResultRepo = exptTurnResultRepo;
      this.evalTargetService = evalTargetService;
      this.evaluatorRecordService = evaluatorRecordService;
      this.evaluationSetItemService = evaluationSetItemService;
      this.exptTurnResultFilterKeyMappingEvaluatorMap = exptTurnResultFilterKeyMappingEvaluatorMap;
      this.itemIDs = new ArrayList<>();
      this.itemID2TurnIDs = new HashMap<>();
      this.itemIDMap = new HashMap<>();
      this.itemIDTurnIDTurnIndex = new HashMap<>();
      this.itemIDItemResultPO = new HashMap<>();
      this.turnIDMap = new HashMap<>();
      // 初始化payload结构
      initializePayloadStructure();
    }

    private void initializePayloadStructure() {
      initializeItemResultMap();
      processTurnResults();
      buildPayloadStructure();
    }

    private void initializeItemResultMap() {
      for (ExptItemResult itemResult : itemResultDAOs) {
        itemIDItemResultPO.put(itemResult.getItemId(), itemResult);
      }
    }

    private void processTurnResults() {
      for (ExptTurnResult turnResultDO : turnResultDAOs) {
        processTurnResultItem(turnResultDO);
        processTurnResultTurn(turnResultDO);
      }
    }

    private void processTurnResultItem(ExptTurnResult turnResultDO) {
      if (!itemIDMap.containsKey(turnResultDO.getItemId())) {
        itemIDs.add(turnResultDO.getItemId()); // 使用turnResultDO中的itemID append确保item有序
      }
      itemIDMap.put(turnResultDO.getItemId(), true);
    }

    private void processTurnResultTurn(ExptTurnResult turnResultDO) {
      initializeTurnIndexMap(turnResultDO);
      updateTurnIndex(turnResultDO);
      updateTurnIDMap(turnResultDO);
      updateItemTurnMapping(turnResultDO);
    }

    private void initializeTurnIndexMap(ExptTurnResult turnResultDO) {
      if (!itemIDTurnIDTurnIndex.containsKey(turnResultDO.getItemId())) {
        itemIDTurnIDTurnIndex.put(turnResultDO.getItemId(), new HashMap<>());
      }
    }

    private void updateTurnIndex(ExptTurnResult turnResultDO) {
      itemIDTurnIDTurnIndex.get(turnResultDO.getItemId()).put(turnResultDO.getTurnId(), (long) turnResultDO.getTurnIdx());
    }

    private void updateTurnIDMap(ExptTurnResult turnResultDO) {
      if (turnResultDO.getTurnId() != 0) {
        turnIDMap.put(turnResultDO.getTurnId(), true);
      }
    }

    private void updateItemTurnMapping(ExptTurnResult turnResultDO) {
      if (!itemID2TurnIDs.containsKey(turnResultDO.getItemId())) {
        itemID2TurnIDs.put(turnResultDO.getItemId(), new ArrayList<>());
      }
      itemID2TurnIDs.get(turnResultDO.getItemId()).add(turnResultDO.getTurnId());
    }

    private void buildPayloadStructure() {
      for (Long itemID : itemIDs) {
        if (itemIDItemResultPO.get(itemID) == null) {
          continue;
        }
        buildItemResult(itemID);
      }
    }

    private void buildItemResult(Long itemID) {
      ExptItemResult itemResultPO = itemIDItemResultPO.get(itemID);
      ItemResult itemResult = createItemResult(itemID, itemResultPO);
      addTurnResultsToItem(itemID, itemResult);
    }

    private ItemResult createItemResult(Long itemID, ExptItemResult itemResultPO) {
      ItemResult itemResult = ItemResult.builder()
        .itemId(itemID)
        .turnResults(new ArrayList<>())
        .itemIndex(Long.valueOf(itemResultPO.getItemIdx()))
        .build();
      itemResult.setSystemInfo(ItemSystemInfo.builder()
        .runState(itemResultPO.getStatus())
        .build());
      return itemResult;
    }

    private void addTurnResultsToItem(Long itemID, ItemResult itemResult) {
      for (Long turnID : itemID2TurnIDs.get(itemID)) {
        long turnIndex = getTurnIndex(itemID, turnID);
        itemResult.getTurnResults().add(createTurnResult(turnID, turnIndex));
      }
    }

    private long getTurnIndex(Long itemID, Long turnID) {
      if (itemIDTurnIDTurnIndex.containsKey(itemID) && itemIDTurnIDTurnIndex.get(itemID).containsKey(turnID)) {
        return itemIDTurnIDTurnIndex.get(itemID).get(turnID);
      }
      return 0;
    }

    private TurnResult createTurnResult(Long turnID, long turnIndex) {
      return TurnResult.builder()
        .turnId(turnID)
        .experimentResults(new ArrayList<>())
        .turnIndex(turnIndex)
        .build();
    }

    public List<ExptTurnResultFilterEntityDO> buildTurnResultFilter() {
      // 分实验获取数据
      ExptResultBuilder exptResultBuilder = new ExptResultBuilder(
        baseExptId,
        baseExptId,
        param.getSpaceId(),
        itemIDs,
        turnIDMap,
        experimentRepo,
        exptTurnResultRepo,
        evalTargetService,
        evaluatorRecordService,
        evaluationSetItemService
      );
      exptResultBuilder.setTurnResultDO(turnResultDAOs);
      try {
        Experiment exptDO = experimentRepo.getById(exptResultBuilder.getExptID(), exptResultBuilder.getSpaceID());
        exptResultBuilder.setExptDO(exptDO);
        if (exptResultBuilder.getTurnResultDO().isEmpty()) {
          return new ArrayList<>();
        }
        // 由于turnID可能为0，以turn_result_id为行的唯一标识聚合数据，组装payload数据时再通过turn_result_id与item_id(单轮)或turn_id(多轮)映射进行组装
        exptResultBuilder.setItemIDTurnID2TurnResultID(new HashMap<>());
        for (ExptTurnResult turnResult : exptResultBuilder.getTurnResultDO()) {
          if (!exptResultBuilder.getItemIDTurnID2TurnResultID().containsKey(turnResult.getItemId())) {
            exptResultBuilder.getItemIDTurnID2TurnResultID().put(turnResult.getItemId(), new HashMap<>());
          }
          exptResultBuilder.getItemIDTurnID2TurnResultID().get(turnResult.getItemId()).put(turnResult.getTurnId(), turnResult.getId());
        }
        exptResultBuilder.buildEvaluatorResult();
        if (exptDO.getExptType() != ExptType.ONLINE) {
          exptResultBuilder.buildTargetOutput();
        }
        exptResultBuilders = List.of(exptResultBuilder);
        // 填充数据
        fillExptTurnResultFilters(exptDO.getStartAt(), exptDO.getEvalSetVersionId());
        return exptTurnResultFilters;
      }
      catch (Exception e) {
        throw new BssException("构建实验结果过滤器失败: " + e.getMessage(), e);
      }
    }

    private void fillExptTurnResultFilters(Date createdDate, Long evalSetVersionID) {
      exptTurnResultFilters = new ArrayList<>();
      Date truncatedDate = truncateToDay(createdDate);
      Map<Long, ExptItemResult> itemID2ItemIdx = buildItemIdToItemResultMap();
      Date updatedAt = new Date();
      for (ExptTurnResult exptTurnResult : turnResultDAOs) {
        ExptTurnResultFilterEntityDO filter = createTurnResultFilter(exptTurnResult, truncatedDate, evalSetVersionID);
        setItemResultInfo(filter, exptTurnResult, itemID2ItemIdx);
        fillEvaluatorScores(filter, exptTurnResult);
        fillEvalTargetData(filter, exptTurnResult);
        setScoreCorrectedInfo(filter, exptTurnResult);
        filter.setUpdatedAt(updatedAt);
        exptTurnResultFilters.add(filter);
      }
    }

    private Date truncateToDay(Date createdDate) {
      if (createdDate == null) {
        return null;
      }
      // 将时间截断到当天的00:00:00
      java.util.Calendar cal = java.util.Calendar.getInstance();
      cal.setTime(createdDate);
      cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
      cal.set(java.util.Calendar.MINUTE, 0);
      cal.set(java.util.Calendar.SECOND, 0);
      cal.set(java.util.Calendar.MILLISECOND, 0);
      return cal.getTime();
    }

    private Map<Long, ExptItemResult> buildItemIdToItemResultMap() {
      Map<Long, ExptItemResult> itemID2ItemIdx = new HashMap<>();
      for (ExptItemResult itemResult : itemResultDAOs) {
        itemID2ItemIdx.put(itemResult.getItemId(), itemResult);
      }
      return itemID2ItemIdx;
    }

    private ExptTurnResultFilterEntityDO createTurnResultFilter(ExptTurnResult exptTurnResult,
                                                                Date truncatedDate, Long evalSetVersionID) {
      ExptTurnResultFilterEntityDO filter = ExptTurnResultFilterEntityDO.builder()
        .spaceId(param.getSpaceId())
        .exptId(baseExptId)
        .itemId(exptTurnResult.getItemId())
        .turnId(exptTurnResult.getTurnId())
        .evalTargetData(new HashMap<>())
        .evaluatorScore(new HashMap<>())
        .annotationFloat(new HashMap<>())
        .annotationBool(new HashMap<>())
        .annotationString(new HashMap<>())
        .createdDate(truncatedDate)
        .evalSetVersionId(evalSetVersionID)
        .build();
      filter.setExptId(baseExptId);
      filter.setSpaceId(param.getSpaceId());
      return filter;
    }

    private void setItemResultInfo(ExptTurnResultFilterEntityDO filter, ExptTurnResult exptTurnResult,
                                   Map<Long, ExptItemResult> itemID2ItemIdx) {
      if (itemID2ItemIdx.containsKey(exptTurnResult.getItemId())) {
        ExptItemResult itemResult = itemID2ItemIdx.get(exptTurnResult.getItemId());
        filter.setItemIdx(itemResult.getItemIdx());
        filter.setStatus(itemResult.getStatus());
      }
    }

    private void fillEvaluatorScores(ExptTurnResultFilterEntityDO filter, ExptTurnResult exptTurnResult) {
      ExptResultBuilder exptResultBuilder = exptResultBuilders.getFirst();
      Map<Long, EvaluatorRecord> evaluatorVersionID2Result =
        exptResultBuilder.getTurnResultID2EvaluatorVersionID2Result().get(exptTurnResult.getId());
      if (evaluatorVersionID2Result != null) {
        for (Map.Entry<Long, EvaluatorRecord> entry : evaluatorVersionID2Result.entrySet()) {
          processEvaluatorScore(filter, entry);
        }
      }
    }

    private void processEvaluatorScore(ExptTurnResultFilterEntityDO filter, Map.Entry<Long, EvaluatorRecord> entry) {
      Long evaluatorVersionID = entry.getKey();
      EvaluatorRecord result = entry.getValue();
      if (result.getScore() != null) {
        String key = String.valueOf(evaluatorVersionID);
        if (exptTurnResultFilterKeyMappingEvaluatorMap.containsKey(key)) {
          ExptTurnResultFilterKeyMapping keyMapping = exptTurnResultFilterKeyMappingEvaluatorMap.get(key);
          filter.getEvaluatorScore().put(keyMapping.getToKey(), result.getScore());
        }
      }
    }

    private void fillEvalTargetData(ExptTurnResultFilterEntityDO filter, ExptTurnResult exptTurnResult) {
      ExptResultBuilder exptResultBuilder = exptResultBuilders.getFirst();
      if (exptResultBuilder.getTurnResultID2TargetOutput() == null) {
        return;
      }
      TurnTargetOutput evalTargetOutput = exptResultBuilder.getTurnResultID2TargetOutput().get(exptTurnResult.getId());
      if (evalTargetOutput != null) {
        for (Map.Entry<String, Content> entry : evalTargetOutput.getEvalTargetRecord().getEvalTargetOutputData().getOutputFields().entrySet()) {
          String outputFieldKey = entry.getKey();
          Content outputFieldValue = entry.getValue();
          filter.getEvalTargetData().put(outputFieldKey, outputFieldValue.getText());
        }
      }
    }

    private void setScoreCorrectedInfo(ExptTurnResultFilterEntityDO filter, ExptTurnResult exptTurnResult) {
      ExptResultBuilder exptResultBuilder = exptResultBuilders.getFirst();
      Boolean evaluatorScoreCorrected = exptResultBuilder.getTurnResultID2ScoreCorrected().get(exptTurnResult.getId());
      if (evaluatorScoreCorrected != null) {
        filter.setEvaluatorScoreCorrected(evaluatorScoreCorrected);
      }
    }

    // ExptResultBuilder 构建单实验结果
    @Getter
    @Setter
    public static class ExptResultBuilder {
      private final Long exptID;
      private final Long baselineExptID;
      private final Long spaceID;
      private final List<Long> itemIDs; // 基准实验的itemID, 未匹配的不展示
      private final Map<Long, Boolean> turnIDMap; // 由于是itemID查询，对于多轮需要用turnID过滤. 对于单轮长度为0
      private Map<Long, Map<Long, Long>> itemIDTurnID2TurnResultID; // itemID -> turnID -> turn_result_id
      private Experiment exptDO;
      private List<ExptTurnResult> turnResultDO;
      // 获取的结果
      private Map<Long, Map<Long, EvaluatorRecord>> turnResultID2EvaluatorVersionID2Result; // turn_result_id -> evaluator_version_id -> result
      private Map<Long, TurnTargetOutput> turnResultID2TargetOutput;
      private Map<Long, Map<Long, TurnEvalSet>> itemIDTurnID2Turn; // item_id -> turn_id -> turn
      private Map<Long, Boolean> turnResultID2ScoreCorrected;
      private final IExperimentRepo experimentRepo;
      private final IExptTurnResultRepo exptTurnResultRepo;
      private final EvaluationSetItemService evaluationSetItemService;
      private final IEvalTargetService evalTargetService;
      private final EvaluatorRecordService evaluatorRecordService;

      public ExptResultBuilder(Long exptID, Long baselineExptID, Long spaceID, List<Long> itemIDs,
                               Map<Long, Boolean> turnIDMap, IExperimentRepo experimentRepo, IExptTurnResultRepo exptTurnResultRepo,
                               IEvalTargetService evalTargetService, EvaluatorRecordService evaluatorRecordService,
                               EvaluationSetItemService evaluationSetItemService) {
        this.exptID = exptID;
        this.baselineExptID = baselineExptID;
        this.spaceID = spaceID;
        this.itemIDs = itemIDs;
        this.turnIDMap = turnIDMap;
        this.experimentRepo = experimentRepo;
        this.exptTurnResultRepo = exptTurnResultRepo;
        this.evalTargetService = evalTargetService;
        this.evaluatorRecordService = evaluatorRecordService;
        this.evaluationSetItemService = evaluationSetItemService;
      }

      public void build() throws Exception {
        exptDO = experimentRepo.getById(exptID, spaceID);
        // 查询非基准实验的, turn_result. 基准实验跳过查询
        if (!exptID.equals(baselineExptID)) {
          // 单轮的turnID始终是0
          // 索引（space_id, expt_id, item_id, turn_id）用item_id查询后过滤
          List<ExptTurnResult> itemTurnResults = exptTurnResultRepo.batchGet(spaceID, exptID, itemIDs);
          // 由于是itemID查询，对于多轮需要用turnID过滤
          List<ExptTurnResult> turnResults = new ArrayList<>();
          for (ExptTurnResult itemTurnResult : itemTurnResults) {
            if (itemTurnResult.getTurnId() == 0) {
              turnResults.add(itemTurnResult);
              continue;
            }
            if (turnIDMap.isEmpty() || turnIDMap.getOrDefault(itemTurnResult.getItemId(), false)) {
              turnResults.add(itemTurnResult);
            }
          }
          turnResultDO = turnResults;
        }
        if (turnResultDO == null || turnResultDO.isEmpty()) {
          return;
        }
        // 由于turnID可能为0，以turn_result_id为行的唯一标识聚合数据，组装payload数据时再通过turn_result_id与item_id(单轮)或turn_id(多轮)映射进行组装
        itemIDTurnID2TurnResultID = new HashMap<>(); // itemID -> turnID -> turn_result_id
        for (ExptTurnResult turnResult : turnResultDO) {
          if (!itemIDTurnID2TurnResultID.containsKey(turnResult.getItemId())) {
            itemIDTurnID2TurnResultID.put(turnResult.getItemId(), new HashMap<>());
          }
          itemIDTurnID2TurnResultID.get(turnResult.getItemId()).put(turnResult.getTurnId(), turnResult.getId());
        }
        buildEvaluatorResult();
        buildEvalSet();
        buildTargetOutput();
      }

      public void buildEvaluatorResult() {
        List<Long> turnResultIDs = turnResultDO.stream()
          .map(ExptTurnResult::getId)
          .collect(Collectors.toList());
        List<ExptTurnEvaluatorResultRef> turnEvaluatorResultRefs = exptTurnResultRepo.batchGetTurnEvaluatorResultRef(spaceID, turnResultIDs);
        List<Long> evaluatorResultIDs = turnEvaluatorResultRefs.stream()
          .map(ExptTurnEvaluatorResultRef::getEvaluatorResultId)
          .collect(Collectors.toList());
        Map<Long, Long> evaluatorResultID2TurnResultID = turnEvaluatorResultRefs.stream()
          .collect(Collectors.toMap(
            ExptTurnEvaluatorResultRef::getEvaluatorResultId,
            ExptTurnEvaluatorResultRef::getExptTurnResultId
          ));
        List<EvaluatorRecord> evaluatorRecords = evaluatorRecordService.batchGetEvaluatorRecord(evaluatorResultIDs, false);
        Map<Long, Map<Long, EvaluatorRecord>> turnResultID2VersionID2Result = new HashMap<>(); // turn_result_id -> version_id -> result
        Map<Long, Boolean> turnResultID2ScoreCorrected = new HashMap<>();
        for (EvaluatorRecord evaluatorRecord : evaluatorRecords) {
          Long turnResultID = evaluatorResultID2TurnResultID.get(evaluatorRecord.getId());
          if (turnResultID == null) {
            continue;
          }
          if (!turnResultID2VersionID2Result.containsKey(turnResultID)) {
            turnResultID2VersionID2Result.put(turnResultID, new HashMap<>());
          }
          turnResultID2VersionID2Result.get(turnResultID).put(evaluatorRecord.getEvaluatorVersionId(), evaluatorRecord);
          if (evaluatorRecord.getCorrected() != null && evaluatorRecord.getCorrected()) {
            turnResultID2ScoreCorrected.put(turnResultID, true);
          }
          else {
            turnResultID2ScoreCorrected.putIfAbsent(turnResultID, false);
          }
        }
        this.turnResultID2EvaluatorVersionID2Result = turnResultID2VersionID2Result;
        this.turnResultID2ScoreCorrected = turnResultID2ScoreCorrected;
      }

      public void buildEvalSet() throws Exception {
        if (exptDO == null) {
          throw new Exception("exptPO is null");
        }
        Long evalSetID = exptDO.getEvalSetId();
        Long evalSetVersionID = exptDO.getEvalSetVersionId();
        BatchGetEvaluationSetItemsParam param = BatchGetEvaluationSetItemsParam.builder()
          .spaceId(spaceID)
          .evaluationSetId(evalSetID)
          .itemIds(itemIDs)
          .build();
        if (!evalSetVersionID.equals(evalSetID)) {
          param.setVersionId(evalSetVersionID);
        }
        PageInfo<EvaluationSetItem> items = evaluationSetItemService.batchGetEvaluationSetItems(param);
        Map<Long, Map<Long, TurnEvalSet>> itemIDTurnID2Turn = new HashMap<>(); // item_id -> turn_id -> turn
        for (EvaluationSetItem item : items.getList()) {
          for (Turn turn : item.getTurns()) {
            if (!itemIDTurnID2Turn.containsKey(item.getItemId())) {
              itemIDTurnID2Turn.put(item.getItemId(), new HashMap<>());
            }
            TurnEvalSet turnEvalSet = TurnEvalSet.builder()
              .turn(turn)
              .build();
            itemIDTurnID2Turn.get(item.getItemId()).put(turn.getId(), turnEvalSet);
          }
        }
        this.itemIDTurnID2Turn = itemIDTurnID2Turn;
      }

      public void buildTargetOutput() {
        if (exptDO.getExptType() == ExptType.ONLINE) {
          return;
        }
        List<Long> targetResultIDs = turnResultDO.stream()
          .map(ExptTurnResult::getTargetResultId)
          .collect(Collectors.toList());
        Map<Long, Long> targetResultID2TurnResultID = turnResultDO.stream()
          .collect(Collectors.toMap(
            ExptTurnResult::getTargetResultId,
            ExptTurnResult::getId
          ));
        List<EvalTargetRecord> targetRecords = evalTargetService.batchGetRecordByIds(spaceID, targetResultIDs);
        Map<Long, TurnTargetOutput> turnResultID2TargetOutput = new HashMap<>(); // turn_result_id -> version_id -> result
        for (EvalTargetRecord targetRecord : targetRecords) {
          Long turnResultID = targetResultID2TurnResultID.get(targetRecord.getId());
          if (turnResultID == null) {
            continue;
          }
          turnResultID2TargetOutput.put(turnResultID, TurnTargetOutput.builder()
            .evalTargetRecord(targetRecord)
            .build());
        }
        this.turnResultID2TargetOutput = turnResultID2TargetOutput;
      }
    }
  }

  public record ItemStats(int pendingCnt, int failCnt, int successCnt, int processingCnt, int terminatedCnt) {
  }

  private record TurnStats(int total, int cnt, List<ItemTurnID> incompleteTurns) {
  }
}
