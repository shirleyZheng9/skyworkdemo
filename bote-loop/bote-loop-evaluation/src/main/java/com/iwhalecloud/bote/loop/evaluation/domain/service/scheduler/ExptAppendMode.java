package com.iwhalecloud.bote.loop.evaluation.domain.service.scheduler;

import com.iwhalecloud.bote.loop.evaluation.domain.component.IConfiger;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Experiment;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptExecConf;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptRunMode;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptStatus;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnEvaluatorResultRef;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.event.ExptScheduleEvent;
import com.iwhalecloud.bote.loop.evaluation.domain.event.ExptEventPublisher;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IExperimentRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.service.IExptManager;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 实验追加模式实现
 * 迁移对应关系: Go语言ExptAppendExec
 * - 功能: 处理实验追加模式的调度逻辑
 * - 主要方法:
 * * exptStart - 实验开始处理
 * * scanEvalItems - 扫描评估项目
 * * exptEnd - 实验结束处理
 * * scheduleStart - 调度开始处理
 * * scheduleEnd - 调度结束处理
 * * nextTick - 下次调度处理
 * * publishResult - 发布结果
 * <p>
 * Java实现说明:
 * - 对应Go的ExptAppendExec结构体
 * - 使用Spring Component注解
 * - 实现ExptSchedulerMode接口
 */
@Component
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class ExptAppendMode implements ExptSchedulerMode {
  private static final Logger logger = LoggerFactory.getLogger(ExptAppendMode.class);

  private final IExptManager manager;
  private final IExperimentRepo exptRepo;
  private final IConfiger configer;
  private final ExptEventPublisher publisher;
  private final ExptBaseExec exptBaseExec;

  @Override
  public ExptRunMode mode() {
    return ExptRunMode.APPEND;
  }

  @Override
  public void exptStart(ExptScheduleEvent event, Experiment expt) {
    // 追加模式不需要特殊处理
  }

  @Override
  public ScanEvalItemResult scanEvalItems(ExptScheduleEvent event, Experiment expt) {
    try {
      return exptBaseExec.scanEvalItems(event, expt);
    }
    catch (Exception e) {
      logger.error("[ExptEval] expt daemon scan eval items failed, expt_id: {}, expt_run_id: {}, err: {}",
        event.getExptId(), event.getExptRunId(), e.getMessage());
      throw new BssException("扫描评估项目失败: " + e.getMessage(), e);
    }
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public boolean exptEnd(ExptScheduleEvent event, Experiment expt, int toSubmit, int incomplete) {
    if (toSubmit == 0 && incomplete == 0 && expt.getStatus() == ExptStatus.DRAINING) {
      logger.info("[ExptEval] expt daemon finished, expt_id: {}, expt_run_id: {}", event.getExptId(), event.getExptRunId());
      try {
        exptBaseExec.exptEnd(event);
      }
      catch (Exception e) {
        logger.error("[ExptEval] expt daemon end failed, expt_id: {}, expt_run_id: {}, err: {}",
          event.getExptId(), event.getExptRunId(), e.getMessage());
      }
      return false;
    }
    return true;
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public void scheduleStart(ExptScheduleEvent event, Experiment expt) {
    try {
      logger.info("ExptAppendExec.ScheduleStart, expt_id: {}, expt_run_id: {}", event.getExptId(), event.getExptRunId());

      // 检查是否需要结束
      if ((expt.getStatus() == ExptStatus.PROCESSING || expt.getStatus() == ExptStatus.PENDING)
        && expt.getMaxAliveTime() > 0) {


        Date deadline = new Date(expt.getStartAt().getTime() + expt.getMaxAliveTime() / 1_000_000); // maxAliveTime 是纳秒，转换为毫秒

        if (new Date().after(deadline)) {
          expt.setStatus(ExptStatus.DRAINING);
          logger.info("expt max alive time exceeded, expt_id: {}, expt_run_id: {}, deadline: {}",
            event.getExptId(), event.getExptRunId(), deadline);

          Experiment updateExpt = Experiment.builder()
            .id(event.getExptId())
            .spaceId(event.getSpaceId())
            .status(ExptStatus.DRAINING)
            .build();
          exptRepo.update(updateExpt);
        }
      }
      else if (expt.getStatus() == ExptStatus.PENDING) {
        Experiment updateExpt = Experiment.builder()
          .id(event.getExptId())
          .spaceId(event.getSpaceId())
          .status(ExptStatus.PROCESSING)
          .build();
        exptRepo.update(updateExpt);
      }
    }
    catch (Exception e) {
      logger.error("update expt status failed, expt_id: {}, expt_run_id: {}, err: {}",
        event.getExptId(), event.getExptRunId(), e.getMessage());
      throw new BssException("调度开始处理失败: " + e.getMessage(), e);
    }
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public void scheduleEnd(ExptScheduleEvent event, Experiment expt, int toSubmit, int incomplete) {
    try {
      if (toSubmit == 0 && incomplete == 0
        && (expt.getStatus() == ExptStatus.PROCESSING || expt.getStatus() == ExptStatus.PENDING)) {

        // 没有数据且未完成，计算一次stats
        logger.info("[ExptEval] expt daemon found no data, expt_id: {}, expt_run_id: {}",
          event.getExptId(), event.getExptRunId());

        try {
          manager.pendRun(event.getExptId(), event.getExptRunId(), event.getSpaceId(), event.getSession());
        }
        catch (Exception e) {
          logger.error("[ExptEval] expt daemon pend run failed, expt_id: {}, expt_run_id: {}, err: {}",
            event.getExptId(), event.getExptRunId(), e.getMessage());
        }

        try {
          manager.pendExpt(event.getExptId(), event.getSpaceId(), event.getSession());
        }
        catch (Exception e) {
          logger.error("[ExptEval] expt daemon pend expt failed, expt_id: {}, expt_run_id: {}, err: {}",
            event.getExptId(), event.getExptRunId(), e.getMessage());
        }

        Thread.sleep(60000); // 60秒延迟
      }
      else if (ExptStatus.isExptFinished(expt.getStatus())) {
        logger.info("[ExptEval] online expt finished, expt_id: {}, expt_run_id: {}",
          event.getExptId(), event.getExptRunId());
      }
    }
    catch (Exception e) {
      throw new BssException("调度结束处理失败: " + e.getMessage(), e);
    }
  }

  @Override
  public void nextTick(ExptScheduleEvent event, Experiment expt, boolean nextTick) {
    if (!nextTick) {
      return;
    }
    try {
      ExptExecConf execConf = configer.getExptExecConf(event.getSpaceId());
      Duration interval = Duration.ofSeconds(execConf.getDaemonIntervalSecond());
      event.setCreatedAt(System.currentTimeMillis() / 1000);
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
