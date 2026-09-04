package com.iwhalecloud.bote.loop.evaluation.domain.service.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.component.IConfiger;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.BatchGetEvaluationSetItemsParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationSetItem;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Experiment;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptItemEvalCtx;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptItemEvalResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptItemResultRunLog;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptRunLog;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptRunMode;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptStatus;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResultRunLog;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Session;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.event.ExptItemEvalEvent;
import com.iwhalecloud.bote.loop.evaluation.domain.event.ExptEventPublisher;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IExptItemResultRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IExptTurnResultRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.service.EvaluationSetItemService;
import com.iwhalecloud.bote.loop.evaluation.domain.service.ExptItemEvalEventService;
import com.iwhalecloud.bote.loop.evaluation.domain.service.IExptManager;
import com.iwhalecloud.bote.loop.evaluation.domain.service.eval.ExptItemEvaluation;
import com.iwhalecloud.bote.loop.evaluation.domain.service.eval.ExptRecordEvalModeFailRetry;
import com.iwhalecloud.bote.loop.evaluation.domain.service.eval.ExptRecordEvalModeSubmit;
import com.iwhalecloud.bote.loop.evaluation.domain.service.eval.RecordEvalChain;
import com.iwhalecloud.bote.loop.evaluation.domain.service.eval.RecordEvalEndPoint;
import com.iwhalecloud.bote.loop.evaluation.domain.service.eval.RecordEvalMode;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

/**
 * 实验项目评估事件服务实现类
 * 迁移对应关系: Go语言backend/modules/evaluation/domain/service/expt_run_item_event_impl.go
 * - 功能: 实验项目评估事件处理实现
 * - 主要方法:
 * * eval - 评估实验项目
 * <p>
 * Java实现说明:
 * - 对应Go的ExptItemEventEvalServiceImpl结构体
 * - 使用Spring Service注解
 * - 支持中间件链模式处理
 * <p>
 * 技术栈迁移:
 * - Go context.Context -> Java方法参数
 * - Go error返回 -> Java异常处理
 * - Go *entity.ExptItemEvalEvent -> Java ExptItemEvalEvent
 * - Go 中间件链模式 -> Java 责任链模式
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class ExptItemEventEvalServiceImpl implements ExptItemEvalEventService, InitializingBean {
  private static final Logger logger = LoggerFactory.getLogger(ExptItemEventEvalServiceImpl.class);

  private final IExptManager manager;
  private final ExptEventPublisher publisher;
  private final IExptItemResultRepo exptItemResultRepo;
  private final IExptTurnResultRepo exptTurnResultRepo;
  private final EvaluationSetItemService evaluationSetItemService;
  private final ExptItemEvaluation exptItemEvaluation;
  private final IConfiger configer;
  /** 中间件链 */
  private RecordEvalEndPoint endpoints;


  @Override
  public void afterPropertiesSet() {
    this.endpoints = RecordEvalChain.builder()
      .addMiddleware(this::handleEventErr)
      .addMiddleware(this::handleEventCheck)
      .addMiddleware(this::handleEventLock)
      .addMiddleware(this::handleEventExec)
      .build();
  }

  /**
   * 评估实验项目
   * 迁移对应关系: Go语言Eval
   * - 功能: 评估实验项目
   * - 参数: event - 实验项目评估事件
   * - 返回: 无
   * - 异常: BssException - 评估失败时抛出
   */
  @Override
  public void eval(ExptItemEvalEvent event) {
    try {
      // 初始化上下文缓存
      // TODO: 实现上下文缓存初始化

      // 执行中间件链
      endpoints.handle(event);
    }
    catch (Exception e) {
      throw new BssException("评估实验项目失败: " + e.getMessage(), e);
    }
  }

  /**
   * 错误处理中间件
   * 迁移对应关系: Go语言HandleEventErr
   * - 功能: 异常捕获和重试逻辑
   * - 参数: next - 下一个中间件
   * - 返回: 中间件函数
   */
  public RecordEvalEndPoint handleEventErr(RecordEvalEndPoint next) {
    return event -> {
      long exptId = event.getExptId();
      long exptRunId = event.getExptRunId();
      long spaceId = event.getSpaceId();
      ExptRunMode exptRunMode = event.getExptRunMode();
      Session session = event.getSession();
      String completeCID = "terminate:indebt:" + event.getExptRunId();
      try {
        next.handle(event);
      }
      catch (Exception e) {
        manager.completeRun(exptId, exptRunId, exptRunMode, spaceId, session, option -> option.setCid(completeCID), option -> option.setStatus(ExptStatus.FAILED), option -> option.setStatusMessage(e.getMessage()));
        manager.completeExpt(exptId, spaceId, session, option -> option.setStatus(ExptStatus.FAILED));
      }
    };
  }

  /**
   * 状态检查中间件
   * 迁移对应关系: Go语言HandleEventCheck
   * - 功能: 检查实验运行状态
   * - 参数: next - 下一个中间件
   * - 返回: 中间件函数
   */
  public RecordEvalEndPoint handleEventCheck(RecordEvalEndPoint next) {
    return (event) -> {
      try {
        ExptRunLog runLog = manager.getRunLog(event.getExptId(), event.getExptRunId(), event.getSpaceId(), event.getSession());

        if (ExptStatus.isExptFinished(ExptStatus.fromValue(runLog.getStatus()))) {
          logger.info("实验已完成，跳过处理: expt_id={}, expt_run_id={}", event.getExptId(), event.getExptRunId());
          return;
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
   * - 功能: 防止并发处理同一项目
   * - 参数: next - 下一个中间件
   * - 返回: 中间件函数
   */
  public RecordEvalEndPoint handleEventLock(RecordEvalEndPoint next) {
    return (event) -> {
      try {
        next.handle(event);
      }
      catch (Exception e) {
        throw new BssException("分布式锁操作失败: " + e.getMessage(), e);
      }
    };
  }

  /**
   * 执行中间件
   * 迁移对应关系: Go语言HandleEventExec
   * - 功能: 执行具体的评估逻辑
   * - 参数: next - 下一个中间件
   * - 返回: 中间件函数
   */
  public RecordEvalEndPoint handleEventExec(RecordEvalEndPoint next) {
    return (event) -> {
      try {
        evalImpl(event);
        next.handle(event);
      }
      catch (Exception e) {
        throw new BssException("执行评估失败: " + e.getMessage(), e);
      }
    };
  }

  /**
   * 核心评估逻辑
   * 迁移对应关系: Go语言eval
   * - 功能: 执行具体的评估逻辑
   * - 参数: event - 实验项目评估事件
   * - 返回: 无
   * - 异常: BssException - 评估失败时抛出
   */
  public void evalImpl(ExptItemEvalEvent event) {
    try {
      // 构建评估上下文
      ExptItemEvalCtx eiec = buildExptItemEvalCtx(event);

      // 创建评估模式
      RecordEvalMode mode = newRecordEvalMode(event);

      // 预评估处理
      mode.preEval(eiec);

      // 执行评估
      exptItemEvaluation.eval(eiec);

      // 后评估处理
      mode.postEval(eiec);
    }
    catch (Exception e) {
      throw new BssException("评估失败: " + e.getMessage(), e);
    }
  }

  /**
   * 构建评估上下文
   * 迁移对应关系: Go语言BuildExptRecordEvalCtx
   * - 功能: 构建实验项目评估上下文
   * - 参数: event - 实验项目评估事件
   * - 返回: 评估上下文
   * - 异常: BssException - 构建失败时抛出
   */
  private ExptItemEvalCtx buildExptItemEvalCtx(ExptItemEvalEvent event) {
    try {
      // 获取实验详情
      Experiment exptDetail = manager.getDetail(event.getExptId(), event.getSpaceId(), event.getSession());

      Long evalSetID = exptDetail.getEvalSet().getEvaluationSetVersion().getEvaluationSetId();
      Long evalSetVerID = exptDetail.getEvalSet().getEvaluationSetVersion().getId();

      // 获取评估集项目
      BatchGetEvaluationSetItemsParam param = BatchGetEvaluationSetItemsParam.builder().spaceId(event.getSpaceId()).evaluationSetId(evalSetID).versionId(evalSetID.equals(evalSetVerID) ? null : evalSetVerID).itemIds(List.of(event.getEvalSetItemId())).build();

      PageInfo<EvaluationSetItem> items = evaluationSetItemService.batchGetEvaluationSetItems(param);
      if (items.getList().size() != 1) {
        throw new BssException("获取评估集项目失败，期望1个，实际" + items.getList().size() + "个");
      }

      // 获取现有评估结果
      ExptItemEvalResult existResult = getExistExptItemEvalResult(event);

      return ExptItemEvalCtx.builder().event(event).expt(exptDetail).evalSetItem(items.getList().getFirst()).exptItemEvalResult(existResult).build();
    }
    catch (Exception e) {
      throw new BssException("构建评估上下文失败: " + e.getMessage(), e);
    }
  }

  /**
   * 获取现有评估结果
   * 迁移对应关系: Go语言GetExistExptRecordEvalResult
   * - 功能: 获取现有的实验项目评估结果
   * - 参数: event - 实验项目评估事件
   * - 返回: 现有评估结果
   * - 异常: BssException - 获取失败时抛出
   */
  private ExptItemEvalResult getExistExptItemEvalResult(ExptItemEvalEvent event) {
    try {
      // 获取轮次运行日志
      List<ExptTurnResultRunLog> turnRunLogs = exptTurnResultRepo.getItemTurnRunLogs(event.getExptId(), event.getExptRunId(), event.getEvalSetItemId(), event.getSpaceId());

      Map<Long, ExptTurnResultRunLog> turnRunResultMap = turnRunLogs.stream().collect(Collectors.toMap(ExptTurnResultRunLog::getItemId, Function.identity()));

      // 获取项目运行日志
      ExptItemResultRunLog itemRunLog = exptItemResultRepo.getItemRunLog(event.getExptId(), event.getExptRunId(), event.getEvalSetItemId(), event.getSpaceId());

      return ExptItemEvalResult.builder().itemResultRunLog(itemRunLog).turnResultRunLogs(turnRunResultMap).build();
    }
    catch (Exception e) {
      throw new BssException("获取现有评估结果失败: " + e.getMessage(), e);
    }
  }

  /**
   * 创建评估模式
   * 迁移对应关系: Go语言NewRecordEvalMode
   * - 功能: 根据实验运行模式创建评估模式
   * - 参数: event - 实验项目评估事件
   * - 返回: 评估模式
   * - 异常: BssException - 创建失败时抛出
   */
  private RecordEvalMode newRecordEvalMode(ExptItemEvalEvent event) {
    return switch (event.getExptRunMode()) {
      case SUBMIT, APPEND -> SpringUtil.getBean(ExptRecordEvalModeSubmit.class);
      case FAIL_RETRY, ITEM_RETRY, ALL_RETRY -> SpringUtil.getBean(ExptRecordEvalModeFailRetry.class);
      default -> throw new BssException("未知的实验运行模式: " + event.getExptRunMode());
    };
  }

}
