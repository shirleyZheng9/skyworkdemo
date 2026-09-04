package com.iwhalecloud.bote.loop.evaluation.domain.service.scheduler;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.component.IConfiger;
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
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ListEvaluationSetItemsParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Turn;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.TurnRunState;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.event.ExptScheduleEvent;
import com.iwhalecloud.bote.loop.evaluation.domain.event.ExptEventPublisher;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IExperimentRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IExptItemResultRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IExptStatsRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IExptTurnResultRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.service.EvaluationSetItemService;
import com.iwhalecloud.bote.loop.evaluation.domain.service.ExptResultService;
import com.iwhalecloud.bote.loop.infra.idgen.IIDGenerator;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 实验提交模式实现
 * 迁移对应关系: Go语言ExptSubmitExec
 * - 功能: 处理实验提交模式的调度逻辑
 * - 主要方法:
 * * exptStart - 实验开始处理
 * * scanEvalItems - 扫描评估项目
 * * exptEnd - 实验结束处理
 * <p>
 * Java实现说明:
 * - 对应Go的ExptSubmitExec结构体
 * - 使用Spring Component注解
 * - 实现ExptSchedulerMode接口
 */
@Component
@RequiredArgsConstructor
public class ExptSubmitMode implements ExptSchedulerMode {
  private static final Logger logger = LoggerFactory.getLogger(ExptSubmitMode.class);

  private final IExptStatsRepo exptStatsRepo;
  private final IExptItemResultRepo exptItemResultRepo;
  private final IExptTurnResultRepo exptTurnResultRepo;
  private final IIDGenerator idgenerator;
  private final EvaluationSetItemService evaluationSetItemService;
  private final IExperimentRepo exptRepo;
  private final IConfiger configer;
  private final ExptEventPublisher publisher;
  private final ExptResultService resultSvc;
  private final ExptBaseExec exptBaseExec;

  @Override
  public ExptRunMode mode() {
    return ExptRunMode.SUBMIT;
  }

  @Override
  public void exptStart(ExptScheduleEvent event, Experiment expt) {
    if (ExptStatus.PENDING != expt.getStatus()) {
      return;
    }
    try {
      Long evalSetID = expt.getEvalSet().getId();
      Long evalSetVersionID = expt.getEvalSet().getEvaluationSetVersion().getId();
      int maxLoop = 10000;
      int itemIdx = 0;
      int page = 1;
      int pageSize = 100;
      int itemCnt = 0;
      long total = 0;
      for (int i = 0; i < maxLoop; i++) {
        ListEvaluationSetItemsParam param = ListEvaluationSetItemsParam.builder()
          .spaceId(event.getSpaceId())
          .evaluationSetId(evalSetID)
          .versionId(evalSetVersionID)
          .pageNumber(page)
          .pageSize(pageSize)
          .build();
        PageInfo<EvaluationSetItem> result = evaluationSetItemService.listEvaluationSetItems(param);
        if (result == null || result.getList() == null) {
          break;
        }
        List<EvaluationSetItem> items = result.getList();
        total = result.getTotal();
        itemCnt += items.size();
        page++;
        int turnCnt = items.stream()
          .mapToInt(item -> item.getTurns().size())
          .sum();
        List<Long> ids = idgenerator.genMultiIds(items.size() + turnCnt);
        List<ExptItemResult> eirs = new ArrayList<>();
        List<ExptTurnResult> etrs = new ArrayList<>();
        int idIdx = 0;
        for (EvaluationSetItem item : items) {
          ExptItemResult eir = ExptItemResult.builder()
            .id(ids.get(idIdx))
            .spaceId(event.getSpaceId())
            .exptId(event.getExptId())
            .exptRunId(event.getExptRunId())
            .itemId(item.getItemId())
            .itemIdx(itemIdx)
            .status(ItemRunState.QUEUEING)
            .build();
          eirs.add(eir);
          itemIdx++;
          idIdx++;
          for (int turnIdx = 0; turnIdx < item.getTurns().size(); turnIdx++) {
            Turn turn = item.getTurns().get(turnIdx);
            ExptTurnResult etr = ExptTurnResult.builder()
              .id(ids.get(idIdx))
              .spaceId(event.getSpaceId())
              .exptId(event.getExptId())
              .exptRunId(event.getExptRunId())
              .itemId(item.getItemId())
              .turnId(turn.getId())
              .turnIdx(turnIdx)
              .status(TurnRunState.QUEUEING.getValue())
              .build();
            etrs.add(etr);
            idIdx++;
          }
        }
        createItemTurnResults(eirs, etrs);
        if (itemCnt >= total || items.isEmpty()) {
          break;
        }
      }
      // 更新统计信息
      ExptStats stats = ExptStats.builder()
        .exptId(event.getExptId())
        .spaceId(event.getSpaceId())
        .pendingItemCnt(itemCnt)
        .build();
      exptStatsRepo.updateByExptId(event.getExptId(), event.getSpaceId(), stats);
      // 更新实验状态
      Experiment updateExpt = Experiment.builder()
        .status(ExptStatus.PROCESSING)
        .id(event.getExptId())
        .spaceId(event.getSpaceId())
        .build();
      exptRepo.update(updateExpt);
    }
    catch (Exception e) {
      throw new BssException("实验开始处理失败: " + e.getMessage(), e);
    }
  }


  @Override
  public void scheduleStart(ExptScheduleEvent event, Experiment expt) {
    // 提交模式不需要特殊处理
  }

  @Override
  public void scheduleEnd(ExptScheduleEvent event, Experiment expt, int toSubmit, int incomplete) {
    // 提交模式不需要特殊处理
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
  public ScanEvalItemResult scanEvalItems(ExptScheduleEvent event, Experiment expt) {
    return exptBaseExec.scanEvalItems(event, expt);
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public boolean exptEnd(ExptScheduleEvent event, Experiment expt, int toSubmit, int incomplete) {
    if (toSubmit == 0 && incomplete == 0) {
      logger.info("[ExptEval] expt daemon finished, expt_id: {}, expt_run_id: {}", event.getExptId(), event.getExptRunId());
      exptBaseExec.exptEnd(event);
      return false;
    }
    return true;
  }

  @Override
  public void publishResult(List<ExptTurnEvaluatorResultRef> turnEvaluatorRefs, ExptScheduleEvent event) {
    // 提交模式不需要发布结果
  }

  /**
   * 创建项目和轮次结果
   * 迁移对应关系: Go语言createItemTurnResults
   * - 功能: 批量创建项目和轮次结果
   * - 参数: eirs - 项目结果列表, etrs - 轮次结果列表, session - 会话信息
   * - 返回: 无
   * - 异常: BssException - 创建失败时抛出
   */
  private void createItemTurnResults(List<ExptItemResult> eirs, List<ExptTurnResult> etrs) {
    try {
      // 创建轮次结果
      exptTurnResultRepo.batchCreateNx(etrs);
      // 创建项目结果
      exptItemResultRepo.batchCreateNx(eirs);
      // 生成日志ID
      List<Long> ids = idgenerator.genMultiIds(eirs.size());
      // 创建项目运行日志
      List<ExptItemResultRunLog> eirLogs = new ArrayList<>();
      for (int idx = 0; idx < eirs.size(); idx++) {
        ExptItemResult eir = eirs.get(idx);
        ExptItemResultRunLog eirLog = ExptItemResultRunLog.builder()
          .id(ids.get(idx))
          .spaceId(eir.getSpaceId())
          .exptId(eir.getExptId())
          .exptRunId(eir.getExptRunId())
          .itemId(eir.getItemId())
          .status(eir.getStatus().getValue())
          .errMsg(eir.getErrMsg())
          .logId(eir.getLogId())
          .build();
        eirLogs.add(eirLog);
      }
      exptItemResultRepo.batchCreateNxRunLogs(eirLogs);
    }
    catch (Exception e) {
      throw new BssException("创建项目和轮次结果失败: " + e.getMessage(), e);
    }
  }
}
