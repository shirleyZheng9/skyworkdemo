package com.iwhalecloud.bote.loop.evaluation.domain.service.scheduler;

import com.iwhalecloud.bote.loop.evaluation.domain.component.IConfiger;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorRecord;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorRunError;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorRunStatus;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Experiment;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptEvalItem;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptExecConf;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptItemResultRunLog;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptItemResultState;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptItemRunLogFilter;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnEvaluatorResultRef;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ItemRunState;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.event.ExptScheduleEvent;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.event.OnlineExptTurnEvalResult;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IExptItemResultRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.service.EvaluatorRecordService;
import com.iwhalecloud.bote.loop.evaluation.domain.service.IExptManager;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 实验基础执行类
 * 迁移对应关系: Go语言exptBaseExec
 * - 功能: 提供实验调度的基础功能
 * - 主要方法:
 * * scanEvalItems - 扫描评估项目
 * * exptEnd - 实验结束处理
 * * publishResult - 发布结果
 * <p>
 * Java实现说明:
 * - 对应Go的exptBaseExec结构体
 * - 使用Spring Component注解
 * - 提供公共的实验调度功能
 */
@Component
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class ExptBaseExec {
  private static final Logger logger = LoggerFactory.getLogger(ExptBaseExec.class);

  private final IExptManager manager;
  private final IConfiger configer;
  private final IExptItemResultRepo exptItemResultRepo;
  private final EvaluatorRecordService evaluatorRecordService;

  /**
   * 扫描评估项目
   * 迁移对应关系: Go语言ScanEvalItems
   * - 功能: 扫描需要处理的评估项目
   * - 参数: event - 实验调度事件, expt - 实验信息
   * - 返回: 扫描结果
   * - 异常: BssException - 扫描失败时抛出
   */
  public ExptSchedulerMode.ScanEvalItemResult scanEvalItems(ExptScheduleEvent event, Experiment expt) {
    try {
      // 扫描处理中的项目
      ExptItemRunLogFilter processingFilter = ExptItemRunLogFilter.builder()
        .status(List.of(ItemRunState.PROCESSING))
        .build();
      List<ExptEvalItem> incomplete = scanRunLogEvalItems(event, expt, processingFilter, 0);

      // 计算可提交数量
      int submitCnt = getItemConcurNum(expt) - incomplete.size();
      List<ExptEvalItem> toSubmit = new ArrayList<>();
      if (submitCnt > 0) {
        ExptItemRunLogFilter queueingFilter = ExptItemRunLogFilter.builder()
          .status(List.of(ItemRunState.QUEUEING))
          .build();
        toSubmit = scanRunLogEvalItems(event, expt, queueingFilter, submitCnt);
      }

      // 扫描已完成的项目
      ExptItemRunLogFilter completeFilter = ExptItemRunLogFilter.builder()
        .resultState(ExptItemResultState.LOGGED)
        .build();
      List<ExptEvalItem> complete = scanRunLogEvalItems(event, expt, completeFilter, 0);

      ExptSchedulerMode.ScanEvalItemResult scanEvalItemResult = new ExptSchedulerMode.ScanEvalItemResult();
      scanEvalItemResult.setToSubmit(toSubmit);
      scanEvalItemResult.setComplete(complete);
      scanEvalItemResult.setIncomplete(incomplete);
      return scanEvalItemResult;

    }
    catch (Exception e) {
      throw new BssException("扫描评估项目失败: " + e.getMessage(), e);
    }
  }

  /**
   * 实验结束处理
   * 迁移对应关系: Go语言exptEnd
   * - 功能: 处理实验结束逻辑
   * - 参数: event - 实验调度事件, expt - 实验信息
   * - 返回: 无
   * - 异常: BssException - 处理失败时抛出
   */
  public void exptEnd(ExptScheduleEvent event) {
    try {
      String completeCID = "exptexec:onend:" + event.getExptRunId();
      manager.completeRun(event.getExptId(), event.getExptRunId(), event.getExptRunMode(),
        event.getSpaceId(), event.getSession(), option -> option.setCid(completeCID));
      manager.completeExpt(event.getExptId(), event.getSpaceId(), event.getSession(),
        option -> option.setCid(completeCID));
      ExptExecConf execConf = configer.getExptExecConf(event.getSpaceId());
      Duration duration = Duration.ofSeconds(execConf.getZombieIntervalSecond() * 2);
      logger.info("ExptSchedulerImpl set end idem key: {}", duration);
    }
    catch (Exception e) {
      logger.error("ExptSchedulerImpl set end idem key fail, err: {}", e.getMessage());
      throw new BssException("实验结束处理失败: " + e.getMessage(), e);
    }
  }

  /**
   * 发布结果
   * 迁移对应关系: Go语言publishResult
   * - 功能: 发布评估结果
   * - 参数: turnEvaluatorRefs - 轮次评估器结果引用列表, event - 实验调度事件
   * - 返回: 无
   * - 异常: BssException - 发布失败时抛出
   */
  public void publishResult(List<ExptTurnEvaluatorResultRef> turnEvaluatorRefs) {
    if (turnEvaluatorRefs.isEmpty()) {
      return;
    }
    List<Long> evaluatorResultIDs = turnEvaluatorRefs.stream()
      .map(ExptTurnEvaluatorResultRef::getEvaluatorResultId)
      .collect(Collectors.toList());

    List<EvaluatorRecord> evaluatorRecords = evaluatorRecordService.batchGetEvaluatorRecord(evaluatorResultIDs, true);

    for (EvaluatorRecord record : evaluatorRecords) {
      OnlineExptTurnEvalResult onlineResult = OnlineExptTurnEvalResult.builder()
        .evaluatorVersionId(record.getEvaluatorVersionId())
        .evaluatorRecordId(record.getId())
        .status(record.getStatus().getValue())
        .ext(record.getExt())
        .baseInfo(record.getBaseInfo())
        .build();

      if (record.getEvaluatorOutputData() != null) {
        if (record.getStatus() == EvaluatorRunStatus.FAIL && record.getEvaluatorOutputData().getEvaluatorRunError() != null) {
          onlineResult.setEvaluatorRunError(EvaluatorRunError.builder()
            .code(record.getEvaluatorOutputData().getEvaluatorRunError().getCode())
            .message(record.getEvaluatorOutputData().getEvaluatorRunError().getMessage())
            .build());
        }
        else if (record.getStatus() == EvaluatorRunStatus.SUCCESS && record.getEvaluatorOutputData().getEvaluatorResult() != null) {
          onlineResult.setScore(record.getEvaluatorOutputData().getEvaluatorResult().getScore());
          onlineResult.setReasoning(record.getEvaluatorOutputData().getEvaluatorResult().getReasoning());
        }
      }
    }
  }

  /**
   * 获取项目并发数
   * 迁移对应关系: Go语言getItemConcurNum
   * - 功能: 获取项目并发数量
   * - 参数: expt - 实验信息
   * - 返回: 并发数量
   */
  private int getItemConcurNum(Experiment expt) {
    if (expt.getEvalConf() != null && expt.getEvalConf().getItemConcurNum() != null) {
      return expt.getEvalConf().getItemConcurNum();
    }
    ExptExecConf execConf = configer.getExptExecConf(expt.getSpaceId());
    int concurNum = execConf.getExptItemEvalConf().getConcurNum();
    logger.info("GetConcurNum, expt_id: {}, concur_num: {}", expt.getId(), concurNum);
    return concurNum;
  }

  /**
   * 扫描运行日志评估项目
   * 迁移对应关系: Go语言ScanRunLogEvalItems
   * - 功能: 扫描运行日志中的评估项目
   * - 参数: event - 实验调度事件, expt - 实验信息, filter - 过滤条件, limit - 限制数量
   * - 返回: 评估项目列表
   * - 异常: BssException - 扫描失败时抛出
   */
  private List<ExptEvalItem> scanRunLogEvalItems(ExptScheduleEvent event, Experiment expt,
                                                 ExptItemRunLogFilter filter, long limit) {
    try {
      List<ExptItemResultRunLog> result = exptItemResultRepo.scanItemRunLogs(
        event.getExptId(), event.getExptRunId(), filter, 0L, limit, event.getSpaceId());

      return result.stream()
        .map(log -> ExptEvalItem.builder()
          .exptId(event.getExptId())
          .evalSetVersionId(expt.getEvalSet().getEvaluationSetVersion().getId())
          .itemId(log.getItemId())
          .state(ItemRunState.fromValue(log.getStatus()))
          .updatedAt(log.getUpdatedAt())
          .build())
        .collect(Collectors.toList());

    }
    catch (Exception e) {
      throw new BssException("扫描运行日志评估项目失败: " + e.getMessage(), e);
    }
  }
}
