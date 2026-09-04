package com.iwhalecloud.bote.loop.evaluation.domain.service.impl;

import com.iwhalecloud.bote.loop.evaluation.domain.component.IConfiger;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Experiment;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptConsumerConf;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptEvalItem;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptExecConf;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptItemResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptRunLog;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptStatus;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ItemRunState;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.StatsCntArithOp;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.TurnRunState;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.event.ExptItemEvalEvent;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.event.ExptScheduleEvent;
import com.iwhalecloud.bote.loop.evaluation.domain.event.ExptEventPublisher;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IExptItemResultRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IExptStatsRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IExptTurnResultRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.service.ExptSchedulerEventService;
import com.iwhalecloud.bote.loop.evaluation.domain.service.IExptManager;
import com.iwhalecloud.bote.loop.evaluation.domain.service.scheduler.ExptSchedulerMode;
import com.iwhalecloud.bote.loop.evaluation.domain.service.scheduler.ExptSchedulerMode.ScanEvalItemResult;
import com.iwhalecloud.bote.loop.evaluation.domain.service.scheduler.SchedulerChain;
import com.iwhalecloud.bote.loop.evaluation.domain.service.scheduler.SchedulerEndPoint;
import com.iwhalecloud.bote.loop.evaluation.domain.service.scheduler.SchedulerModeFactory;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

/**
 * 实验调度服务实现类
 * 迁移对应关系: Go语言backend/modules/evaluation/domain/service/expt_run_scheduler_event_impl.go
 * - 功能: 实验调度事件处理实现
 * - 主要方法:
 * * schedule - 调度实验
 * <p>
 * Java实现说明:
 * - 对应Go的ExptSchedulerImpl结构体
 * - 使用Spring Service注解
 * - 支持中间件链模式处理
 * <p>
 * 技术栈迁移:
 * - Go context.Context -> Java方法参数
 * - Go error返回 -> Java异常处理
 * - Go *entity.ExptScheduleEvent -> Java ExptScheduleEvent
 * - Go 中间件链模式 -> Java 责任链模式
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class ExptSchedulerServiceImpl implements ExptSchedulerEventService, InitializingBean {
  private static final Logger logger = LoggerFactory.getLogger(ExptSchedulerServiceImpl.class);

  private final IExptManager manager;
  private final ExptEventPublisher publisher;
  private final IExptItemResultRepo exptItemResultRepo;
  private final IExptTurnResultRepo exptTurnResultRepo;
  private final IExptStatsRepo exptStatsRepo;
  private final IConfiger configer;
  /** 中间件链 */
  private SchedulerEndPoint endpoints;

  @Override
  public void afterPropertiesSet() {
    this.endpoints = SchedulerChain.builder()
      .addMiddleware(this::handleEventErr)
      .addMiddleware(this::sysOps)
      .addMiddleware(this::handleEventCheck)
      .addMiddleware(this::handleEventLock)
      .addMiddleware(this::handleEventEndpoint)
      .build();
  }

  /**
   * 调度实验
   * 迁移对应关系: Go语言Schedule
   * - 功能: 调度实验执行
   * - 参数: event - 实验调度事件
   * - 返回: 无
   * - 异常: BssException - 调度失败时抛出
   */
  @Override
  public void schedule(ExptScheduleEvent event) {
    try {
      // 初始化上下文缓存
      // TODO: 实现上下文缓存初始化

      // 执行中间件链
      endpoints.handle(event);
    }
    catch (Exception e) {
      logger.error("[ExptScheduler] expt schedule fail, event: {}, err: {}", event, e.getMessage(), e);
      throw new BssException("调度实验失败: " + e.getMessage(), e);
    }
  }

  /**
   * 系统操作中间件
   * 迁移对应关系: Go语言SysOps
   * - 功能: 系统操作处理
   * - 参数: next - 下一个中间件
   * - 返回: 中间件函数
   */
  public SchedulerEndPoint sysOps(SchedulerEndPoint next) {
    return next;
  }

  /**
   * 状态检查中间件
   * 迁移对应关系: Go语言HandleEventCheck
   * - 功能: 检查实验运行状态和超时
   * - 参数: next - 下一个中间件
   * - 返回: 中间件函数
   */
  public SchedulerEndPoint handleEventCheck(SchedulerEndPoint next) {
    return event -> {
      try {
        ExptRunLog runLog = manager.getRunLog(event.getExptId(), event.getExptRunId(),
          event.getSpaceId(), event.getSession());

        if (ExptStatus.isExptFinished(ExptStatus.fromValue(runLog.getStatus()))) {
          logger.info("ExptSchedulerConsumer consume finished expt run event, expt_id: {}, expt_run_id: {}",
            event.getExptId(), event.getExptRunId());
          return;
        }

        // 检查超时
        ExptExecConf execConf = configer.getExptExecConf(event.getSpaceId());
        long interval = execConf.getZombieIntervalSecond();
        long currentTime = System.currentTimeMillis() / 1000;

        if (currentTime - event.getCreatedAt() >= interval) {
          throw new BssException(String.format("expt exec found timeout event, expt_id: %d, expt_run_id: %d",
            event.getExptId(), event.getExptRunId()));
        }

        next.handle(event);
      }
      catch (Exception e) {
        throw new BssException("检查实验状态失败: " + e.getMessage(), e);
      }
    };
  }

  /**
   * 分布式锁中间件
   * 迁移对应关系: Go语言HandleEventLock
   * - 功能: 防止并发处理同一实验
   * - 参数: next - 下一个中间件
   * - 返回: 中间件函数
   */
  public SchedulerEndPoint handleEventLock(SchedulerEndPoint next) {
    return event -> {
      try {
        // TODO: 实现分布式锁逻辑
        logger.info("ExptSchedulerConsumer.HandleEventLock locked expt eval event: {}", event);

        next.handle(event);
      }
      catch (Exception e) {
        logger.warn("ExptSchedulerConsumer.HandleEventLock found locked expt eval event: {}. Abort event, err: {}",
          event, e.getMessage());
        throw new BssException("分布式锁操作失败: " + e.getMessage(), e);
      }
    };
  }

  /**
   * 执行端点中间件
   * 迁移对应关系: Go语言HandleEventEndpoint
   * - 功能: 执行具体的调度逻辑
   * - 参数: next - 下一个中间件
   * - 返回: 中间件函数
   */
  public SchedulerEndPoint handleEventEndpoint(SchedulerEndPoint next) {
    return event -> {
      try {
        scheduleImpl(event);
        next.handle(event);
      }
      catch (Exception e) {
        throw new BssException("执行调度失败: " + e.getMessage(), e);
      }
    };
  }

  /**
   * 错误处理中间件
   * 迁移对应关系: Go语言HandleEventErr
   * - 功能: 异常捕获和实验终止
   * - 参数: next - 下一个中间件
   * - 返回: 中间件函数
   */
  public SchedulerEndPoint handleEventErr(SchedulerEndPoint next) {
    return event -> {
      Exception nextErr = null;
      try {
        next.handle(event);
      }
      catch (Exception e) {
        nextErr = e;
      }

      if (nextErr == null) {
        logger.info("[ExptEval] handle event success, event: {}", event);
        return;
      }

      logger.error("[ExptEval] HandleEventErr found error: {}, event: {}", nextErr.getMessage(), event);

      // 终止实验
      String completeCID = "exptexec:onerr:" + event.getExptRunId();
      try {
        manager.completeRun(event.getExptId(), event.getExptRunId(), event.getExptRunMode(),
          event.getSpaceId(), event.getSession(), option -> option.setCid(completeCID));

        Exception finalNextErr = nextErr;
        manager.completeExpt(event.getExptId(), event.getSpaceId(), event.getSession(), option -> {
          option.setStatus(ExptStatus.FAILED);
          option.setStatusMessage(finalNextErr.getMessage());
        });
      }
      catch (Exception e) {
        throw new BssException("终止实验失败: " + e.getMessage(), e);
      }
    };
  }

  /**
   * 核心调度逻辑
   * 迁移对应关系: Go语言schedule
   * - 功能: 执行具体的调度逻辑
   * - 参数: event - 实验调度事件
   * - 返回: 无
   * - 异常: BssException - 调度失败时抛出
   */
  private void scheduleImpl(ExptScheduleEvent event) {
    Experiment exptDetail = getExperimentDetail(event);
    ExptSchedulerMode mode = createSchedulerMode(event);

    executeSchedulerFlow(event, exptDetail, mode);
  }

  private Experiment getExperimentDetail(ExptScheduleEvent event) {
    return manager.getDetail(event.getExptId(), event.getSpaceId(), event.getSession());
  }

  private ExptSchedulerMode createSchedulerMode(ExptScheduleEvent event) {
    return SchedulerModeFactory.newSchedulerMode(event.getExptRunMode());
  }

  private void executeSchedulerFlow(ExptScheduleEvent event, Experiment exptDetail, ExptSchedulerMode mode) {
    startExperimentAndScheduler(event, exptDetail, mode);
    ScanEvalItemResult scanResult = scanEvaluationItems(event, exptDetail, mode);
    ZombieHandleResult zombieResult = handleZombies(event, scanResult.getIncomplete());
    List<ExptEvalItem> finalIncomplete = zombieResult.alives();
    endSchedulerAndExperiment(event, exptDetail, mode, scanResult.getToSubmit().size(), finalIncomplete.size());
    handleToSubmits(event, scanResult.getToSubmit());
    scanResult = scanEvaluationItems(event, exptDetail, mode);
    scheduleNextTick(event, exptDetail, mode, scanResult.getToSubmit().size(), scanResult.getIncomplete().size());
  }

  private void startExperimentAndScheduler(ExptScheduleEvent event, Experiment exptDetail, ExptSchedulerMode mode) {
    mode.exptStart(event, exptDetail);
    mode.scheduleStart(event, exptDetail);
  }

  private ScanEvalItemResult scanEvaluationItems(ExptScheduleEvent event, Experiment exptDetail, ExptSchedulerMode mode) {
    return mode.scanEvalItems(event, exptDetail);
  }

  private void endSchedulerAndExperiment(ExptScheduleEvent event, Experiment exptDetail, ExptSchedulerMode mode, int toSubmitCount, int incompleteCount) {
    mode.scheduleEnd(event, exptDetail, toSubmitCount, incompleteCount);
  }

  private void scheduleNextTick(ExptScheduleEvent event, Experiment exptDetail, ExptSchedulerMode mode, int toSubmitCount, int incompleteCount) {
    boolean nextTickResult = mode.exptEnd(event, exptDetail, toSubmitCount, incompleteCount);
    logger.info("[ExptEval] expt daemon with next tick, expt_id: {}, event: {}", event.getExptId(), event);
    mode.nextTick(event, exptDetail, nextTickResult);
  }

  /**
   * 处理待提交项目
   * 迁移对应关系: Go语言handleToSubmits
   * - 功能: 处理待提交的评估项目
   * - 参数: event - 实验调度事件, toSubmits - 待提交项目列表
   * - 返回: 无
   * - 异常: BssException - 处理失败时抛出
   */
  private void handleToSubmits(ExptScheduleEvent event, List<ExptEvalItem> toSubmits) {
    if (toSubmits.isEmpty()) {
      return;
    }

    long now = System.currentTimeMillis() / 1000;
    List<Long> itemIDs = toSubmits.stream()
      .filter(ts -> !ts.getState().isItemRunFinished())
      .map(ExptEvalItem::getItemId)
      .collect(Collectors.toList());

    List<ExptItemEvalEvent> itemEvalEvents = toSubmits.stream()
      .filter(ts -> !ts.getState().isItemRunFinished())
      .map(ts -> ExptItemEvalEvent.builder()
        .spaceId(event.getSpaceId())
        .exptId(event.getExptId())
        .exptRunId(event.getExptRunId())
        .exptRunMode(event.getExptRunMode())
        .evalSetItemId(ts.getItemId())
        .createAt(now)
        .ext(event.getExt())
        .session(event.getSession())
        .build())
      .collect(Collectors.toList());

    logger.info("submit item eval events: {}", itemEvalEvents);

    ExptExecConf execConf = configer.getExptExecConf(event.getSpaceId());
    Duration interval = Duration.ofSeconds(execConf.getExptItemEvalConf().getInterval());

    try {
      publisher.batchPublishExptRecordEvalEvent(itemEvalEvents, interval);
    }
    catch (Exception e) {
      throw new BssException("批量发布评估事件失败: " + e.getMessage(), e);
    }

//    // 更新项目状态为处理中
//    Map<String, Object> updateFields = Map.of("status", ItemRunState.PROCESSING.getValue());
//    try {
//      exptItemResultRepo.updateItemRunLog(event.getExptId(), event.getExptRunId(), itemIDs, updateFields, event.getSpaceId());
//      exptItemResultRepo.updateItemsResult(event.getSpaceId(), event.getExptId(), itemIDs, updateFields);
//    }
//    catch (Exception e) {
//      throw new BssException("更新项目状态失败: " + e.getMessage(), e);
//    }
//    // 更新轮次结果状态
//    try {
//      exptTurnResultRepo.updateTurnResultsWithItemIds(event.getExptId(), itemIDs, event.getSpaceId(), updateFields);
//    }
//    catch (Exception e) {
//      throw new BssException("更新轮次结果状态失败: " + e.getMessage(), e);
//    }

    // 获取项目结果并更新统计
    try {
      List<ExptItemResult> itemResults = exptItemResultRepo.batchGet(event.getSpaceId(), event.getExptId(), itemIDs);
      StatsCntArithOp arithOp = StatsCntArithOp.builder()
        .opStatusCnt(calculateItemStats(itemResults))
        .build();
      exptStatsRepo.arithOperateCount(event.getExptId(), event.getSpaceId(), arithOp);
    }
    catch (Exception e) {
      throw new BssException("更新统计信息失败: " + e.getMessage(), e);
    }
  }

  public static Map<ItemRunState, Integer> calculateItemStats(List<ExptItemResult> itemResultList) {
    int pendingCnt = -itemResultList.size();
    int failCnt = 0;
    int successCnt = 0;
    int processingCnt = 0;
    int terminatedCnt = 0;
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
    return Map.of(
      ItemRunState.PROCESSING, processingCnt,
      ItemRunState.QUEUEING, pendingCnt,
      ItemRunState.SUCCESS, successCnt,
      ItemRunState.FAIL, failCnt,
      ItemRunState.TERMINAL, terminatedCnt
    );
  }

  /**
   * 处理僵尸项目
   * 迁移对应关系: Go语言handleZombies
   * - 功能: 处理超时的僵尸项目
   * - 参数: event - 实验调度事件, items - 项目列表
   * - 返回: 处理结果
   * - 异常: BssException - 处理失败时抛出
   */
  private ZombieHandleResult handleZombies(ExptScheduleEvent event, List<ExptEvalItem> items) {
    ExptConsumerConf consumerConf = configer.getConsumerConf();
    ExptExecConf execConf = consumerConf.getExptExecConfBySpaceId(event.getSpaceId());
    int zombieSecond = execConf.getExptItemEvalConf().getZombieSecond();

    List<ExptEvalItem> alives = items.stream()
      .filter(item -> {
        if (item.getState() == ItemRunState.PROCESSING && item.getUpdatedAt() != null) {
          long timeSinceUpdate = System.currentTimeMillis() - item.getUpdatedAt().getTime();
          return (timeSinceUpdate / 1000) <= zombieSecond;
        }
        return true;
      })
      .collect(Collectors.toList());

    List<ExptEvalItem> zombies = items.stream()
      .filter(item -> {
        if (item.getState() == ItemRunState.PROCESSING && item.getUpdatedAt() != null) {
          long timeSinceUpdate = System.currentTimeMillis() - item.getUpdatedAt().getTime();
          return (timeSinceUpdate / 1000) <= zombieSecond;
        }
        return false;
      })
      .map(item -> item.setState(ItemRunState.FAIL))
      .collect(Collectors.toList());

    if (zombies.isEmpty()) {
      return new ZombieHandleResult(alives, zombies);
    }

    List<Long> zombieItemIDs = zombies.stream()
      .map(ExptEvalItem::getItemId)
      .collect(Collectors.toList());

    logger.warn("[ExptEval] found zombie items, set failure state, expt_id: {}, expt_run_id: {}, item_ids: {}, zombie_second: {}",
      event.getExptId(), event.getExptRunId(), zombieItemIDs, zombieSecond);

    // 更新僵尸项目状态
    Map<String, Object> updateFields = Map.of("status", ItemRunState.FAIL.getValue());
    try {
      exptItemResultRepo.updateItemRunLog(event.getExptId(), event.getExptRunId(), zombieItemIDs, updateFields, event.getSpaceId());
      exptTurnResultRepo.createOrUpdateItemsTurnRunLogStatus(event.getSpaceId(), event.getExptId(), event.getExptRunId(), zombieItemIDs, TurnRunState.FAIL);
    }
    catch (Exception e) {
      throw new BssException("更新僵尸项目状态失败: " + e.getMessage(), e);
    }
    return new ZombieHandleResult(alives, zombies);
  }

  /**
   * 僵尸处理结果
   */
  private record ZombieHandleResult(List<ExptEvalItem> alives, List<ExptEvalItem> zombies) {
  }
}
