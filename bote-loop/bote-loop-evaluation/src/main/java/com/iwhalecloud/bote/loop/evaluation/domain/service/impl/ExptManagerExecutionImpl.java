package com.iwhalecloud.bote.loop.evaluation.domain.service.impl;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.ArgsSchema;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.CompleteExptOption;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.CompleteExptOptionFn;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Connector;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.CreditCost;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationSetItem;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorConf;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Experiment;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptCalculateStats;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptItemResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptItemResultRunLog;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptRunCheckOption;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptRunLog;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptRunMode;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptStats;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptStatus;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.FieldConf;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.FieldSchema;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.InvokeExptReq;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ItemRunState;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ItemTurnID;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Page;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Session;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.StatsCntArithOp;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Turn;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.TurnRunState;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.event.AggrCalculateEvent;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.event.CalculateMode;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.event.ExptScheduleEvent;
import com.iwhalecloud.bote.loop.evaluation.domain.event.ExptEventPublisher;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IExperimentRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IExptItemResultRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IExptRunLogRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IExptStatsRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IExptTurnResultRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.service.ExptResultService;
import com.iwhalecloud.bote.loop.evaluation.domain.service.IExptExecutionManager;
import com.iwhalecloud.bote.loop.evaluation.domain.service.QuotaService;
import com.iwhalecloud.bote.loop.infra.idgen.IIDGenerator;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * 实验管理执行服务实现类
 * 迁移对应关系: Go语言backend/modules/evaluation/domain/service/expt_manage_execution_impl.go
 * - 功能: 实验管理执行业务逻辑实现
 * - 主要方法:
 * * checkRun - 检查运行条件
 * * checkRunWithTuple - 使用元组检查运行条件
 * * run - 运行实验
 * * retryUnSuccess - 重试失败实验
 * * completeRun - 完成运行
 * * completeExpt - 完成实验
 * * kill - 终止实验
 * * invoke - 调用实验
 * * finish - 完成实验
 * * pendRun - 暂停运行
 * * pendExpt - 暂停实验
 * * logRun - 记录运行
 * * getRunLog - 获取运行日志
 * <p>
 * Java实现说明:
 * - 对应Go的ExptMangerImpl结构体
 * - 使用Spring Service注解
 * - 依赖多个REPO和组件
 * - 统一异常处理
 * <p>
 * 技术栈迁移:
 * - Go context.Context -> Java方法参数
 * - Go error返回 -> Java异常处理
 * - Go sync.Once -> Java单例模式
 * - Go events.ExptEventPublisher -> Java ExptEventPublisher
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class ExptManagerExecutionImpl implements IExptExecutionManager {
  private static final Logger logger = LoggerFactory.getLogger(ExptManagerExecutionImpl.class);

  private final ExptResultService exptResultService;
  private final IExptRunLogRepo runLogRepo;
  private final IExptStatsRepo statsRepo;
  private final IExptItemResultRepo itemResultRepo;
  private final IExptTurnResultRepo turnResultRepo;
  private final ExptEventPublisher publisher;
  private final IIDGenerator idgenerator;
  private final IExperimentRepo experimentRepo;
  private final QuotaService quotaService;

  @Override
  public void checkRun(Experiment expt, Long spaceId, Session session, ExptRunCheckOption... opts) {
    try {
      ExptRunCheckOption opt = new ExptRunCheckOption();
      BiConsumer<Experiment, Session> checkTargetBiConsumer = this::checkTarget;
      BiConsumer<Experiment, Session> checkEvalSetBiConsumer = this::checkEvalSet;
      BiConsumer<Experiment, Session> checkEvaluatorsBiConsumer = this::checkEvaluators;
      BiConsumer<Experiment, Session> checkConnectorBiConsumer = this::checkConnector;
      BiConsumer<Experiment, Session> checkBenefitBiConsumer = this::checkBenefit;
      List<BiConsumer<Experiment, Session>> checkers = new ArrayList<>(List.of(checkTargetBiConsumer, checkEvalSetBiConsumer, checkEvaluatorsBiConsumer, checkConnectorBiConsumer));
      if (expt.getExptType() == ExptType.OFFLINE && opt.getCheckBenefit()) {
        checkers.add(checkBenefitBiConsumer);
      }
      for (BiConsumer<Experiment, Session> check : checkers) {
        check.accept(expt, session);
      }
    }
    catch (Exception e) {
      throw new BssException("检查运行条件失败: " + e.getMessage(), e);
    }
  }

  @Override
  public void run(Long exptId, Long runId, Long spaceId, Session session, ExptRunMode runMode, Map<String, String> ext) {
    try {
      // 检查配额
      quotaService.allowExptRun(exptId, spaceId, session);
      ExptScheduleEvent event = ExptScheduleEvent.builder().spaceId(spaceId).exptId(exptId).exptRunId(runId).exptRunMode(runMode).createdAt(System.currentTimeMillis() / 1000).session(session).ext(ext).build();
      publisher.publishExptScheduleEvent(event, Duration.ofSeconds(3L));
    }
    catch (Exception e) {
      throw new BssException("运行实验失败: " + e.getMessage(), e);
    }
  }

  @Override
  public void retryUnSuccess(Long exptId, Long runId, Long spaceId, Session session, Map<String, String> ext) {
    try {
      // 检查配额
      quotaService.allowExptRun(exptId, spaceId, session);
      ExptScheduleEvent event = ExptScheduleEvent.builder().spaceId(spaceId).exptId(exptId).exptRunId(runId).exptRunMode(ExptRunMode.FAIL_RETRY).createdAt(System.currentTimeMillis() / 1000).session(session).ext(ext).build();
      publisher.publishExptScheduleEvent(event, Duration.ofSeconds(3L));
    }
    catch (Exception e) {
      throw new BssException("重试失败实验失败: " + e.getMessage(), e);
    }
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public void completeRun(Long exptId, Long exptRunId, ExptRunMode mode, Long spaceId, Session session, CompleteExptOptionFn... opts) {
    try {
      CompleteExptOption opt = new CompleteExptOption();
      for (CompleteExptOptionFn fn : opts) {
        fn.apply(opt);
      }
      ExptRunLog runLog = runLogRepo.get(exptId, exptRunId);
      calculateRunLogStats(exptId, runLog, spaceId);
      if (opt.getStatus() != null && opt.getStatus().getValue() > 0) {
        runLog.setStatus(opt.getStatus().getValue());
      }
      if (opt.getStatusMessage() != null && !opt.getStatusMessage().isEmpty()) {
        runLog.setStatusMessage(opt.getStatusMessage());
      }
      logger.info("[ExptEval] CompleteRun, expt_id: {}, expt_run_id: {}, status: {}, msg: {}", exptId, exptRunId, runLog.getStatus(), opt.getStatusMessage());
      runLogRepo.save(runLog);
    }
    catch (Exception e) {
      throw new BssException("完成运行失败: " + e.getMessage(), e);
    }
  }

  @Override
  public void completeExpt(Long exptId, Long spaceId, Session session, CompleteExptOptionFn... opts) {
    try {
      CompleteExptOption opt = buildCompleteExptOption(opts);
      publishAggrCalculateEvent(exptId, spaceId);
      ExptCalculateStats stats = exptResultService.calculateStats(exptId, spaceId, session);
      updateExptStats(exptId, spaceId, stats);

      ExptStatus status = determineExptStatus(opt.getStatus(), stats);
      handleTerminatedStatus(exptId, spaceId, status, stats);

      updateExperimentStatus(exptId, spaceId, status, opt);
      quotaService.releaseExptRun(exptId, spaceId, session);
    }
    catch (Exception e) {
      throw new BssException("完成实验失败: " + e.getMessage(), e);
    }
  }

  private CompleteExptOption buildCompleteExptOption(CompleteExptOptionFn... opts) {
    CompleteExptOption opt = new CompleteExptOption();
    for (CompleteExptOptionFn fn : opts) {
      fn.apply(opt);
    }
    return opt;
  }

  private void publishAggrCalculateEvent(Long exptId, Long spaceId) {
    List<AggrCalculateEvent> events = List.of(AggrCalculateEvent.builder()
      .experimentId(exptId)
      .spaceId(spaceId)
      .calculateMode(CalculateMode.CREATE_ALL_FIELDS)
      .build());
    publisher.publishExptAggrCalculateEvent(events, Duration.ofSeconds(3L));
  }

  private void updateExptStats(Long exptId, Long spaceId, ExptCalculateStats stats) {
    ExptStats exptStats = ExptStats.builder()
      .successItemCnt(stats.getSuccessItemCnt())
      .pendingItemCnt(stats.getPendingItemCnt())
      .failItemCnt(stats.getFailItemCnt())
      .processingItemCnt(stats.getProcessingItemCnt())
      .terminatedItemCnt(stats.getTerminatedItemCnt())
      .build();
    statsRepo.updateByExptId(exptId, spaceId, exptStats);
  }

  private ExptStatus determineExptStatus(ExptStatus optStatus, ExptCalculateStats stats) {
    if (isExptFinished(optStatus)) {
      return optStatus;
    }

    if (stats.getFailItemCnt() > 0 || stats.getTerminatedItemCnt() > 0 || !stats.getIncompleteTurnIds().isEmpty()) {
      return ExptStatus.FAILED;
    }
    return ExptStatus.SUCCESS;
  }

  private void handleTerminatedStatus(Long exptId, Long spaceId, ExptStatus status, ExptCalculateStats stats) {
    if (status == ExptStatus.TERMINATED) {
      List<List<ItemTurnID>> chunks = chunkList(stats.getIncompleteTurnIds());
      for (List<ItemTurnID> chunk : chunks) {
        try {
          terminateItemTurns(exptId, chunk, spaceId);
        }
        catch (Exception e) {
          logger.error("terminateItemTurns fail, err: {}", e.getMessage(), e);
        }
      }
    }
  }

  private void updateExperimentStatus(Long exptId, Long spaceId, ExptStatus status, CompleteExptOption opt) {
    Experiment exptDo = Experiment.builder()
      .id(exptId)
      .spaceId(spaceId)
      .status(status)
      .endAt(new Date())
      .build();

    if (opt.getStatusMessage() != null && !opt.getStatusMessage().isEmpty()) {
      exptDo.setStatusMessage(opt.getStatusMessage());
    }

    experimentRepo.update(exptDo);
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public void invoke(InvokeExptReq invokeExptReq) {
    try {
      if (invokeExptReq.getItems().isEmpty()) {
        return;
      }
      int itemIdx = 0;
      int itemCnt = 0;
      List<Long> existItemIdList = itemResultRepo.getItemIdListByExptId(invokeExptReq.getSpaceId(), invokeExptReq.getExptId());
      List<EvaluationSetItem> toSubmitItems = Lists.newArrayList();
      for (EvaluationSetItem item : invokeExptReq.getItems()) {
        if (existItemIdList.contains(item.getItemId())) {
          logger.debug("InvokeExpt with exist item, expt_id: {}, item_id: {}", invokeExptReq.getExptId(), item.getItemId());
          continue;
        }
        toSubmitItems.add(item);
      }
      if (toSubmitItems.isEmpty()) {
        logger.info("InvokeExpt with no new item, expt_id: {}", invokeExptReq.getExptId());
        return;
      }
      Integer maxItemIdx = itemResultRepo.getMaxItemIdxByExptId(invokeExptReq.getExptId(), invokeExptReq.getSpaceId());
      if (maxItemIdx != null) {
        itemIdx = maxItemIdx + 1;
      }
      itemCnt += toSubmitItems.size();
      int turnCnt = 0;
      for (EvaluationSetItem item : toSubmitItems) {
        turnCnt += item.getTurns().size();
      }
      List<Long> ids = idgenerator.genMultiIds(toSubmitItems.size() + turnCnt);
      int idIdx = 0;
      List<ExptItemResult> eirs = Lists.newArrayList();
      List<ExptTurnResult> etrs = Lists.newArrayList();
      for (EvaluationSetItem item : toSubmitItems) {
        ExptItemResult eir = ExptItemResult.builder().id(ids.get(idIdx)).spaceId(invokeExptReq.getSpaceId()).exptId(invokeExptReq.getExptId()).exptRunId(invokeExptReq.getRunId()).itemId(item.getItemId()).itemIdx(itemIdx).status(ItemRunState.QUEUEING).build();
        eirs.add(eir);
        itemIdx++;
        idIdx++;
        for (int turnIdx = 0; turnIdx < item.getTurns().size(); turnIdx++) {
          Turn turn = item.getTurns().get(turnIdx);
          ExptTurnResult etr = ExptTurnResult.builder().id(ids.get(idIdx)).spaceId(invokeExptReq.getSpaceId()).exptId(invokeExptReq.getExptId()).exptRunId(invokeExptReq.getRunId()).itemId(item.getItemId()).turnId(turn.getId()).turnIdx(turnIdx).status(TurnRunState.QUEUEING.ordinal()).build();
          etrs.add(etr);
          idIdx++;
        }
      }
      createItemTurnResults(eirs, etrs);
      logger.info("ExptAppendExec.Append ListEvaluationSetItem done, expt_id: {}, itemCnt: {}", invokeExptReq.getExptId(), itemCnt);
      // 更新统计
      StatsCntArithOp statsOp = StatsCntArithOp.builder().opStatusCnt(Map.of(ItemRunState.QUEUEING, itemCnt)).build();
      statsRepo.arithOperateCount(invokeExptReq.getExptId(), invokeExptReq.getSpaceId(), statsOp);
      ExptScheduleEvent event = ExptScheduleEvent.builder().spaceId(invokeExptReq.getSpaceId()).exptId(invokeExptReq.getExptId()).exptRunId(invokeExptReq.getRunId()).exptRunMode(ExptRunMode.APPEND).createdAt(System.currentTimeMillis() / 1000).session(invokeExptReq.getSession()).ext(invokeExptReq.getExt()).build();
      publisher.publishExptScheduleEvent(event, Duration.ofSeconds(3L));
    }
    catch (Exception e) {
      throw new BssException("调用实验失败: " + e.getMessage(), e);
    }
  }

  @Override
  public void finish(Experiment expt, Long exptRunId, Session session) {
    try {
      Experiment exptDo = Experiment.builder().id(expt.getId()).spaceId(expt.getSpaceId()).status(ExptStatus.DRAINING).build();
      experimentRepo.update(exptDo);
      ExptScheduleEvent event = ExptScheduleEvent.builder().spaceId(expt.getSpaceId()).exptId(expt.getId()).exptRunId(exptRunId).exptRunMode(ExptRunMode.APPEND).createdAt(System.currentTimeMillis() / 1000).session(session).build();
      publisher.publishExptScheduleEvent(event, Duration.ofSeconds(3L));
    }
    catch (Exception e) {
      throw new BssException("完成实验失败: " + e.getMessage(), e);
    }
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public void pendRun(Long exptId, Long exptRunId, Long spaceId, Session session) {
    try {
      ExptRunLog runLog = getRunLog(exptId, exptRunId, spaceId, session);
      calculateRunLogStats(exptId, runLog, spaceId);
      runLog.setStatus((long) ExptStatus.PENDING.ordinal());
      logger.info("[ExptEval] PendRun, expt_id: {}, expt_run_id: {}, status: {}", exptId, exptRunId, runLog.getStatus());
      runLogRepo.save(runLog);
    }
    catch (Exception e) {
      throw new BssException("暂停运行失败: " + e.getMessage(), e);
    }
  }

  @Override
  public void pendExpt(Long exptId, Long spaceId, Session session, CompleteExptOptionFn... opts) {
    try {
      ExptCalculateStats stats = exptResultService.calculateStats(exptId, spaceId, session);
      ExptStats exptStats = ExptStats.builder().successItemCnt(stats.getSuccessItemCnt()).pendingItemCnt(stats.getPendingItemCnt()).failItemCnt(stats.getFailItemCnt()).processingItemCnt(stats.getProcessingItemCnt()).terminatedItemCnt(stats.getTerminatedItemCnt()).build();
      statsRepo.updateByExptId(exptId, spaceId, exptStats);
    }
    catch (Exception e) {
      throw new BssException("暂停实验失败: " + e.getMessage(), e);
    }
  }

  @Override
  public void logRun(Long exptId, Long exptRunId, ExptRunMode mode, Long spaceId, Session session) {
    try {
      ExptRunLog runLog = ExptRunLog.builder().id(exptRunId).spaceId(spaceId).createdBy(session.getUserId()).exptId(exptId).exptRunId(exptRunId).mode(mode.getValue()).status(ExptStatus.PENDING.getValue()).build();
      runLogRepo.create(runLog);
    }
    catch (Exception e) {
      throw new BssException("记录运行失败: " + e.getMessage(), e);
    }
  }

  @Override
  public ExptRunLog getRunLog(Long exptId, Long exptRunId, Long spaceId, Session session) {
    try {
      return runLogRepo.get(exptId, exptRunId);
    }
    catch (Exception e) {
      throw new BssException("获取运行日志失败: " + e.getMessage(), e);
    }
  }

  // 私有辅助方法
  private void checkEvalSet(Experiment expt, Session session) {
    Assert.notNull(session, () -> "会话为空 ");
    switch (expt.getExptType()) {
      case OFFLINE:
        if (expt.getEvalSetVersionId() == 0 || expt.getEvalSet() == null || expt.getEvalSet().getEvaluationSetVersion() == null) {
          throw new BssException("评估集版本无效: " + expt.getEvalSetVersionId());
        }
        if (expt.getEvalSet().getEvaluationSetVersion().getItemCount() <= 0) {
          throw new BssException("评估集版本为空: " + expt.getEvalSetVersionId());
        }
        break;
      case ONLINE:
        if (expt.getEvalSet() == null) {
          throw new BssException("评估集为空: " + expt.getEvalSetId());
        }
        break;
      default:
        break;
    }
  }

  private void checkTarget(Experiment expt, Session session) {
    Assert.notNull(session, () -> "会话为空 ");
    if (expt.getTargetId() == 0 || expt.getTargetVersionId() == 0 || expt.getTarget() == null) {
      throw new BssException("实验目标无效, target_id=" + expt.getTargetId() + " target_version_id=" + expt.getTargetVersionId());
    }
  }

  private void checkEvaluators(Experiment expt, Session session) {
    Assert.notNull(session, () -> "会话为空 ");
    if (expt.getEvaluatorVersionRef().isEmpty() || expt.getEvaluators().size() != expt.getEvaluatorVersionRef().size()) {
      throw new BssException("实验评估器无效: " + expt.getEvaluatorVersionRef());
    }
  }

  private void checkConnector(Experiment expt, Session session) {
    Assert.notNull(session, () -> "会话为空 ");
    if (expt.getEvalConf() == null) {
      return;
    }

    Connector connectorConf = expt.getEvalConf().getConnectorConf();
    validateConnectorConfiguration(connectorConf, expt.getTarget().getEvalTargetType());

    Map<String, ArgsSchema> targetOutputSchema = buildTargetOutputSchema(expt);
    Map<String, FieldSchema> evalSetFieldSchema = buildEvalSetFieldSchema(expt);

    validateTargetConnector(connectorConf, evalSetFieldSchema);
    validateEvaluatorConnectors(connectorConf, evalSetFieldSchema, targetOutputSchema);
  }

  private void validateConnectorConfiguration(Connector connectorConf, EvalTargetType targetType) {
    if (connectorConf.getEvaluatorsConf().valid()) {
      throw new BssException("评估器连接器无效");
    }

    if (connectorConf.getTargetConf().valid(targetType)) {
      throw new BssException("目标连接器无效");
    }
  }

  private Map<String, ArgsSchema> buildTargetOutputSchema(Experiment expt) {
    return expt.getTarget().getEvalTargetVersion().getOutputSchema().stream()
      .collect(Collectors.toMap(s -> s.getKey() != null ? s.getKey() : "", s -> s));
  }

  private Map<String, FieldSchema> buildEvalSetFieldSchema(Experiment expt) {
    return expt.getEvalSet().getEvaluationSetVersion().getEvaluationSetSchema().getFieldSchemas().stream()
      .collect(Collectors.toMap(FieldSchema::getName, s -> s));
  }

  private void validateTargetConnector(Connector connectorConf, Map<String, FieldSchema> evalSetFieldSchema) {
    for (FieldConf fc : connectorConf.getTargetConf().getIngressConf().getEvalSetAdapter().getFieldConfs()) {
      validateFieldMapping(fc, evalSetFieldSchema, "目标期望接收缺失的评估集");
    }
  }

  private void validateEvaluatorConnectors(Connector connectorConf,
                                           Map<String, FieldSchema> evalSetFieldSchema,
                                           Map<String, ArgsSchema> targetOutputSchema) {
    for (EvaluatorConf evaluatorConf : connectorConf.getEvaluatorsConf().getEvaluatorConf()) {
      validateEvaluatorEvalSetFields(evaluatorConf, evalSetFieldSchema);
      validateEvaluatorTargetFields(evaluatorConf, targetOutputSchema);
    }
  }

  private void validateEvaluatorEvalSetFields(EvaluatorConf evaluatorConf, Map<String, FieldSchema> evalSetFieldSchema) {
    for (FieldConf fc : evaluatorConf.getIngressConf().getEvalSetAdapter().getFieldConfs()) {
      validateFieldMapping(fc, evalSetFieldSchema, "评估器 " + evaluatorConf.getEvaluatorVersionId() + " 期望接收缺失的评估集");
    }
  }

  private void validateEvaluatorTargetFields(EvaluatorConf evaluatorConf,
                                             Map<String, ArgsSchema> targetOutputSchema) {
    for (FieldConf fc : evaluatorConf.getIngressConf().getTargetAdapter().getFieldConfs()) {
      String firstField = getFirstJSONPathField(fc.getFromField());
      if (firstField == null) {
        throw new BssException("无效连接器: 评估器 " + evaluatorConf.getEvaluatorVersionId() + " 期望接收缺失的目标 " + fc.getFromField() + " 列, JSON解析错误");
      }
      if (targetOutputSchema.get(firstField) == null) {
        throw new BssException("无效连接器: 评估器 " + evaluatorConf.getEvaluatorVersionId() + " 期望接收缺失的目标 " + fc.getFromField() + " 字段");
      }
    }
  }

  private void validateFieldMapping(FieldConf fc, Map<String, FieldSchema> fieldSchema, String errorPrefix) {
    String firstField = getFirstJSONPathField(fc.getFromField());
    if (firstField == null) {
      throw new BssException("无效连接器: " + errorPrefix + " " + fc.getFromField() + " 列, JSON解析错误");
    }
    if (fieldSchema.get(firstField) == null) {
      throw new BssException("无效连接器: " + errorPrefix + " " + fc.getFromField() + " 列");
    }
  }

  private void checkBenefit(Experiment expt, Session session) {
    Assert.notNull(session, () -> "会话为空 ");
    if (expt.getCreditCost() == CreditCost.FREE) {
      logger.info("CheckBenefit with credit cost already freed, expt_id: {}", expt.getId());
    }
    // TODO 权益检查逻辑
  }

  private void calculateRunLogStats(Long exptId, ExptRunLog runLog, Long spaceId) {
    TurnResultCounts counts = countTurnResultsByStatus(exptId, spaceId);
    updateRunLogCounts(runLog, counts);
    determineRunLogStatus(runLog);
  }

  private TurnResultCounts countTurnResultsByStatus(Long exptId, Long spaceId) {
    int maxLoop = 10000;
    int limit = 100;
    int page = 1;
    TurnResultCounts counts = new TurnResultCounts();
    for (int i = 0; i < maxLoop; i++) {
      List<ExptTurnResult> results = turnResultRepo.listTurnResult(spaceId, exptId, null, new Page(page, limit), false);
      if (results == null || results.isEmpty()) {
        break;
      }
      page++;
      countResults(results, counts);
    }
    return counts;
  }

  private void countResults(List<ExptTurnResult> results, TurnResultCounts counts) {
    for (ExptTurnResult tr : results) {
      TurnRunState state = TurnRunState.values()[tr.getStatus()];
      incrementCountByState(state, counts);
    }
  }

  private void incrementCountByState(TurnRunState state, TurnResultCounts counts) {
    switch (state) {
      case SUCCESS:
        counts.successCnt++;
        break;
      case FAIL:
        counts.failCnt++;
        break;
      case TERMINAL:
        counts.terminatedCnt++;
        break;
      case QUEUEING:
        counts.pendingCnt++;
        break;
      case PROCESSING:
        counts.processingCnt++;
        break;
      default:
        break;
    }
  }

  private void updateRunLogCounts(ExptRunLog runLog, TurnResultCounts counts) {
    runLog.setPendingCnt(counts.pendingCnt);
    runLog.setFailCnt(counts.failCnt);
    runLog.setSuccessCnt(counts.successCnt);
    runLog.setProcessingCnt(counts.processingCnt);
    runLog.setTerminatedCnt(counts.terminatedCnt);
  }

  private void determineRunLogStatus(ExptRunLog runLog) {
    if (runLog.getPendingCnt() > 0 || runLog.getFailCnt() > 0) {
      runLog.setStatus((long) ExptStatus.FAILED.ordinal());
    }
    else {
      runLog.setStatus((long) ExptStatus.SUCCESS.ordinal());
    }
  }

  private void terminateItemTurns(Long exptId, List<ItemTurnID> itemTurnIds, Long spaceId) {
    List<Long> itemIds = itemTurnIds.stream().map(ItemTurnID::getItemId).collect(Collectors.toList());
    Map<String, Object> updateFields = new HashMap<>();
    updateFields.put("status", ItemRunState.TERMINAL.ordinal());
    itemResultRepo.updateItemsResult(spaceId, exptId, itemIds, updateFields);
    updateFields.put("status", TurnRunState.TERMINAL.ordinal());
    turnResultRepo.updateTurnResults(exptId, itemTurnIds, spaceId, updateFields);
  }

  private void createItemTurnResults(List<ExptItemResult> eirs, List<ExptTurnResult> etrs) {
    turnResultRepo.batchCreateNx(etrs);
    itemResultRepo.batchCreateNx(eirs);
    List<Long> ids = idgenerator.genMultiIds(eirs.size());
    List<ExptItemResultRunLog> eirLogs = new ArrayList<>();
    for (int idx = 0; idx < eirs.size(); idx++) {
      ExptItemResult eir = eirs.get(idx);
      ExptItemResultRunLog eirLog = ExptItemResultRunLog.builder().id(ids.get(idx)).spaceId(eir.getSpaceId()).exptId(eir.getExptId()).exptRunId(eir.getExptRunId()).itemId(eir.getItemId()).status(eir.getStatus().ordinal()).errMsg(eir.getErrMsg()).logId(eir.getLogId()).build();
      eirLogs.add(eirLog);
    }
    itemResultRepo.batchCreateNxRunLogs(eirLogs);
  }


  private String getFirstJSONPathField(String fromField) {
    // 简化实现，实际应该解析JSON路径
    if (fromField == null || fromField.isEmpty()) {
      return null;
    }
    return fromField.split("\\.")[0];
  }

  private boolean isExptFinished(ExptStatus status) {
    return status == ExptStatus.SUCCESS || status == ExptStatus.FAILED || status == ExptStatus.TERMINATED;
  }

  private <T> List<List<T>> chunkList(List<T> list) {
    List<List<T>> chunks = new ArrayList<>();
    for (int i = 0; i < list.size(); i += 30) {
      chunks.add(list.subList(i, Math.min(i + 30, list.size())));
    }
    return chunks;
  }

  private static final class TurnResultCounts {
    int pendingCnt = 0;
    int failCnt = 0;
    int successCnt = 0;
    int terminatedCnt = 0;
    int processingCnt = 0;
  }
}
