package com.iwhalecloud.bote.dto.orchestration.context;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iwhalecloud.bote.cache.PlanContextCache;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.consts.PlanConsts;
import com.iwhalecloud.bote.common.consts.SceneConsts;
import com.iwhalecloud.bote.common.jdbc.ConnectionWrapper;
import com.iwhalecloud.bote.dto.base.SimpleFlowStepDTO;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineRequest;
import com.iwhalecloud.bote.dto.orchestration.SceneDslDTO;
import com.iwhalecloud.bote.dto.orchestration.log.OrchestrationStepRunLog;
import com.iwhalecloud.bote.dto.planning.PlanRecordDTO;
import com.iwhalecloud.bote.dto.planning.PlanStepDTO;
import com.iwhalecloud.bote.service.base.IDataSourceProviderService;
import com.iwhalecloud.bote.service.orchestration.reply.ReplyHandler;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.database.consts.DatabaseType;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 场景编排引擎上下文
 *
 * @author bianjp
 * @since 2024-08-29
 */
@Getter
@Setter
public class SceneOrchestrationContext {
  private static final Logger logger = LoggerFactory.getLogger(SceneOrchestrationContext.class);

  /** 开始时间 */
  private Date startTime;
  /** 请求对象 */
  private OrchestrationEngineRequest request;
  /** 回复处理器 */
  @JsonIgnore
  private ReplyHandler replyHandler;
  /** DSL */
  private SceneDslDTO dsl;
  /** 解析后的入参 */
  private Map<String, Object> parsedInputParameters;

  /** 是否结束场景 */
  private Boolean sceneFinished;
  /** 出参 */
  private Map<String, Object> outputParameters = new ConcurrentHashMap<>();
  /** 全局变量 */
  @Setter(AccessLevel.NONE)
  private Map<String, Object> globalVariables = new ConcurrentHashMap<>();
  /** 中间步骤输出。key 为步骤编码 */
  @Setter(AccessLevel.NONE)
  private Map<String, Object> stepOutput = new ConcurrentHashMap<>();
  /** 节点异常信息。key 为步骤编码 */
  @Setter(AccessLevel.NONE)
  private Map<String, Map<String, Object>> exceptionInfoMap = new HashMap<>();
  /** 循环变量 map。key 为循环步骤编码 */
  @Setter(AccessLevel.NONE)
  private Map<String, Object> loopVariables = new ConcurrentHashMap<>(0);
  /** 并行分支上下文栈线程变量。使用栈结构以支持并行任务嵌套的情况 */
  @SuppressWarnings("PMD.LooseCoupling")
  @Setter(AccessLevel.NONE)
  private ThreadLocal<LinkedList<ParallelBranchContext>> parallelBranchContextStackThreadLocal = ThreadLocal.withInitial(LinkedList::new);
  /** 是否已返回出参 */
  @Setter(AccessLevel.NONE)
  private volatile boolean returned;
  /** 是否已中断. Future#cancel 有时不能成功设置线程的 interrupt 状态，因此我们自己额外记录一下 */
  private volatile boolean interrupted;
  /** 节点执行次数（包含嵌套流程） */
  @Setter(AccessLevel.NONE)
  private AtomicInteger stepExecutionCount = new AtomicInteger(0);
  /** 数据源提供者 */
  private IDataSourceProviderService dataSourceProvider;
  /** 数据源持有者映射。key 为数据源编码 */
  @Setter(AccessLevel.NONE)
  private Map<Long, DataSourceHolder> dataSourceHolderMap = new ConcurrentHashMap<>(4);

  /** 步骤执行日志。只在调试时生成；只包含已执行的步骤，按执行顺序排序 */
  @Setter(AccessLevel.NONE)
  private List<OrchestrationStepRunLog> stepLogs;
  /** 是否启用步骤执行日志 */
  @Setter(AccessLevel.NONE)
  private boolean enableStepLogs;

  /** 清理器列表，在工作流执行结束后执行，用于释放资源 */
  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private final List<Runnable> cleaners = new CopyOnWriteArrayList<>();

  /**
   * 查找步骤
   */
  public AbstractStep findStep(String stepCode) {
    AbstractStep step = IterableUtils.find(dsl.getSteps(), s -> stepCode.equals(s.getCode()));
    Assert.notNull(step, () -> "节点不存在: " + stepCode);
    return step;
  }

  /**
   * 获取租户 ID
   */
  public Long getTenantId() {
    return request.getTenantId();
  }

  /**
   * 获取机器人 ID
   *
   * <p>单独调试工作流时机器人 ID 为 null</p>
   */
  @Nullable
  public Long getBotId() {
    return request.getBotId();
  }

  /**
   * 是否需要持久化上下文（场景变量）
   */
  public boolean shouldPersistContext() {
    // 只有对话流需要，未定义场景变量时不处理
    // 简单场景调用对话流时不需要持久化
    return Boolean.TRUE.equals(dsl.getChatflow()) &&
      !Boolean.FALSE.equals(request.getPersistContext()) &&
      CollectionUtils.isNotEmpty(dsl.getVariables());
  }

  /**
   * 获取循环变量的值
   *
   * @param stepCode 循环步骤编码
   * @return 变量的值
   */
  public Object getLoopVariable(String stepCode) {
    return loopVariables.get(stepCode);
  }

  /**
   * 设置循环变量
   *
   * @param stepCode 循环步骤编码
   * @param value 变量的值
   */
  public void setLoopVariable(String stepCode, Object value) {
    if (value != null) {
      loopVariables.put(stepCode, value);
    }
  }

  /**
   * 清除循环变量
   *
   * @param stepCode 循环步骤编码
   */
  public void removeLoopVariable(String stepCode) {
    loopVariables.remove(stepCode);
  }

  /**
   * 设置步骤的异常信息
   */
  public void setExceptionInfo(AbstractStep step, String errorCode, String errorMessage) {
    Map<String, Object> exceptionInfo = new LinkedHashMap<>();
    exceptionInfo.put(SceneConsts.ERROR_CODE_KEY, errorCode);
    exceptionInfo.put(SceneConsts.ERROR_MESSAGE_KEY, errorMessage);
    exceptionInfoMap.put(step.getCode(), exceptionInfo);
  }

  /**
   * 获取步骤的异常信息
   */
  @Nullable
  public Map<String, Object> getExceptionInfo(String stepCode) {
    return exceptionInfoMap.get(stepCode);
  }

  /**
   * 设置步骤出参和节点日志出参
   *
   * <p>用于流式输出结束时更新步骤出参和节点日志</p>
   */
  public void setStepOutput(AbstractStep step, Object value, @Nullable OrchestrationStepRunLog log) {
    stepOutput.put(step.getCode(), value);
    if (log != null) {
      log.setOutput(value);
    }
  }

  /**
   * 设置步骤输出
   *
   * @param step 步骤
   * @param value 步骤出参
   */
  public void setStepOutput(AbstractStep step, @Nullable Object value) {
    if (value != null) {
      stepOutput.put(step.getCode(), value);
      OrchestrationStepRunLog stepLog = getLastStepRunLog();
      if (stepLog != null) {
        stepLog.setOutput(value);
      }
    }
    else {
      // 每次循环时可能会对同一个节点设置出参，如果出参为 null, 需要删除节点出参，否则上次循环的节点出参会残留，导致被后面的节点引用到
      stepOutput.remove(step.getCode());
    }
  }

  /**
   * 批量删除步骤输出
   */
  public void removeStepOutput(@Nullable List<String> stepCodes) {
    if (CollectionUtils.isEmpty(stepCodes)) {
      return;
    }
    for (String stepCode : stepCodes) {
      stepOutput.remove(stepCode);
      exceptionInfoMap.remove(stepCode);
    }
  }

  /**
   * 设置步骤输出
   *
   * @param stepCode 步骤编码
   * @return 步骤出参
   */
  @Nullable
  public Object getStepOutput(String stepCode) {
    return stepOutput.get(stepCode);
  }

  /**
   * 设置已返回出参标记
   *
   * <p>加锁以防止执行并行分支时多个分支重复设置。</p>
   *
   * @return 是否设置成功
   */
  public boolean setReturned() {
    if (!this.returned) {
      synchronized (this) {
        if (!this.returned) {
          this.returned = true;
          return true;
        }
      }
    }
    return false;
  }

  /**
   * 初始化执行日志
   */
  public void initRunLog() {
    if (request.isDebugEnabled() || Boolean.TRUE.equals(request.getLogEnabled())) {
      stepLogs = new ArrayList<>(dsl.getSteps().size());
      enableStepLogs = true;
    }
  }

  /**
   * 开始步骤执行日志
   */
  public void startStepLog(AbstractStep step) {
    if (enableStepLogs) {
      // 如果前一个步骤执行日志还未结束，标记为成功
      OrchestrationStepRunLog lastStepLog = getLastStepRunLog();
      if (lastStepLog != null && !lastStepLog.isFinished()) {
        lastStepLog.succeed();
      }
      OrchestrationStepRunLog stepLog = new OrchestrationStepRunLog();
      stepLog.setStepName(step.getName());
      stepLog.setStepCode(step.getCode());
      stepLog.setStepType(step.getType());
      stepLog.setStartTime(new Date());
      getParallelAwareStepRunLogs().add(stepLog);
    }
    if (!Boolean.TRUE.equals(request.getDebug()) && replyHandler != null) {
      if (StringUtils.isNotEmpty(step.getDesc())) {
        // 输出关联的步骤信息，作为执行过程
        Map<String, Object> flowStep = new HashMap<>();
        flowStep.put("type", step.getType());
        flowStep.put("name", step.getDesc());
        flowStep.put("code", step.getCode());
        replyHandler.reply(ChatMessageType.FLOW_STEP, flowStep, step.getName(), step.getCode());
      }
      if (request.getPlanId() != null) {
        // 尝试更新计划步骤状态
        updatePlanState(step.getCode(), request.getFlowId());
      }
    }
  }

  /**
   * 设置步骤入参日志
   */
  public void setStepInputLog(@Nullable Object input) {
    OrchestrationStepRunLog stepLog = getLastStepRunLog();
    if (stepLog != null) {
      stepLog.setInput(input);
    }
  }

  /**
   * 设置步骤出参日志
   */
  public void setStepOutputLog(@Nullable Object output) {
    OrchestrationStepRunLog stepLog = getLastStepRunLog();
    if (stepLog != null) {
      stepLog.setOutput(output);
    }
  }

  /**
   * 添加步骤自定义日志
   *
   * @param format 日志格式。使用 String#format 格式，可包含占位符，以避免不需要生成日志拼接字符串的开销
   * @param args 参数列表。支持使用 Supplier 以避免不需要生成日志时的开销
   */
  public void addStepLog(String format, Object... args) {
    OrchestrationStepRunLog stepLog = getLastStepRunLog();
    if (stepLog != null) {
      stepLog.addLog(format, args);
    }
  }

  /**
   * 将步骤执行日志标记为成功
   */
  public void succeedStepLog() {
    OrchestrationStepRunLog stepLog = getLastStepRunLog();
    if (stepLog != null && !stepLog.isFinished()) {
      stepLog.succeed();
    }
  }

  /**
   * 将步骤执行日志标记为失败
   */
  public void failStepLog(Throwable throwable) {
    OrchestrationStepRunLog stepLog = getLastStepRunLog();
    if (stepLog != null && !stepLog.isFinished()) {
      stepLog.fail(throwable);
    }
  }

  /**
   * 获取最后一条步骤执行日志
   */
  public Optional<OrchestrationStepRunLog> getLastStepRunLogOptional() {
    return Optional.ofNullable(getLastStepRunLog());
  }

  /**
   * 获取最后一条步骤执行日志
   */
  @Nullable
  private OrchestrationStepRunLog getLastStepRunLog() {
    if (enableStepLogs) {
      List<OrchestrationStepRunLog> logs = getParallelAwareStepRunLogs();
      if (!logs.isEmpty()) {
        return logs.get(logs.size() - 1);
      }
    }
    return null;
  }

  /**
   * 获取可感知并行环境的步骤执行日志列表
   *
   * <p>并行环境中每个线程使用单独的执行日志列表，以避免线程安全问题</p>
   */
  private List<OrchestrationStepRunLog> getParallelAwareStepRunLogs() {
    // 如果当前正在执行并行分支的步骤，则返回并行分支的执行日志列表
    ParallelBranchContext parallelBranchContext = pollParallelBranchContext();
    if (parallelBranchContext != null) {
      return parallelBranchContext.getStepLogs();
    }
    return stepLogs;
  }

  /**
   * 将并行分支上下文压到栈顶
   */
  public void pushParallelBranchContext(ParallelBranchContext context) {
    parallelBranchContextStackThreadLocal.get().push(context);
  }

  /**
   * 删除栈顶的并行分支上下文
   */
  @SuppressWarnings("PMD.LooseCoupling")
  public void removeParallelBranchContext() {
    LinkedList<ParallelBranchContext> stack = parallelBranchContextStackThreadLocal.get();
    stack.pop();
    if (stack.isEmpty()) {
      parallelBranchContextStackThreadLocal.remove();
    }
  }

  /**
   * 获取栈顶的并行分支上下文
   */
  @Nullable
  public ParallelBranchContext pollParallelBranchContext() {
    return parallelBranchContextStackThreadLocal.get().peek();
  }

  /**
   * 更新计划中的步骤状态
   */
  private void updatePlanState(String stepCode, @Nullable Long flowId) {
    PlanRecordDTO record = SpringUtil.getBean(PlanContextCache.class).get(request.getPlanId());
    if (record == null) {
      return;
    }
    for (PlanStepDTO step : record.getSteps()) {
      for (SimpleFlowStepDTO sceneStep : CollectionUtils.emptyIfNull(step.getFlowSteps())) {
        if (flowId == null) {
          // 匹配智能体编排的节点
          if (sceneStep.getNodeCode().equals(stepCode)) {
            sceneStep.setStepStatus(PlanConsts.STATUS_RUNNING);
            replyHandler.updatePlanState(record);
            return;
          }
        }
        else {
          // 匹配工作流编排的节点
          for (SimpleFlowStepDTO flowStep : CollectionUtils.emptyIfNull(sceneStep.getChildren())) {
            if (flowStep.getNodeCode().equals(stepCode) && Objects.equals(PlanConsts.STATUS_NOT_STARTED, flowStep.getStepStatus())) {
              // 前面的同级步骤状态置为已完成
              sceneStep.getChildren().stream().filter(p -> Objects.equals(PlanConsts.STATUS_RUNNING, p.getStepStatus()))
                .forEach(p -> p.setStepStatus(PlanConsts.STATUS_SUCCESS));
              flowStep.setStepStatus(PlanConsts.STATUS_RUNNING);
              replyHandler.updatePlanState(record);
              return;
            }
          }
        }
      }
    }
  }

  /**
   * 获取 JdbcTemplate 实例
   *
   * @param dataSourceId 数据源 ID
   */
  public JdbcTemplate getJdbcTemplate(Long dataSourceId) {
    return initDataSource(dataSourceId).get(dataSourceId).getJdbcTemplate();
  }

  /**
   * 获取数据源类型
   *
   * @param dataSourceId 数据源 ID
   * @return 数据源类型
   */
  public DatabaseType getDataSourceType(Long dataSourceId) {
    return initDataSource(dataSourceId).get(dataSourceId).getDataSourceType();
  }

  /**
   * 初始化数据源
   *
   * @param dataSourceId 数据源 ID
   */
  public Map<Long, DataSourceHolder> initDataSource(Long dataSourceId) {
    // 嵌套调用编排服务时，通过最顶层服务的上下文对象管理所有服务用到的数据源和事务，以保证所有服务使用相同的事务
    // 如果使用不同的事务，不同服务中修改同一个表时可能会出现死锁问题（比如上层服务中删除一条数据，嵌套服务中插入相同 ID 的一条数据）
    // 目前不会出现跨应用调用服务，所以通过一个上下文管理所有服务用到的数据源没有问题
    // TODO 嵌套调用服务时无法正确判断一个数据源是否存在并行操作
    // TODO 编排服务中调用内部服务时，内部服务也应该使用编排服务上下文管理的数据源
    //    SceneOrchestrationContext rootContext = SceneContextUtil.getRootContext();
    //    if (!this.equals(rootContext)) {
    //      return rootContext.initDataSource(dataSourceId);
    //    }

    Long tenantId = getTenantId();
    dataSourceHolderMap.computeIfAbsent(dataSourceId, (key) -> {
      DataSource dataSource = dataSourceProvider.getDataSource(tenantId, dataSourceId);
      DatabaseType dataSourceType = dataSourceProvider.getDataSourceType(tenantId, dataSourceId);
      ConnectionWrapper connection = dataSourceProvider.getConnectionWrapper(dataSource);
      NamedParameterJdbcTemplate namedJdbcTemplate = dataSourceProvider.createNamedJdbcTemplate(connection);
      return new DataSourceHolder(dataSource, dataSourceType, connection, namedJdbcTemplate);
    });
    return dataSourceHolderMap;
  }

  /**
   * 提交数据库事务
   * <p>提交失败时抛异常</p>
   */
  public void commitTransaction() {
    // 提交所有数据源的事务
    // 暂不考虑部分成功部分失败的情况
    for (Entry<Long, DataSourceHolder> entry : dataSourceHolderMap.entrySet()) {
      Long dataSourceId = entry.getKey();
      try {
        ConnectionWrapper connection = entry.getValue().getConnection();
        if (connection != null) {
          connection.commit();
        }
        logger.debug("Commit database transaction successfully: startTime={}, dataSourceId={}", startTime, dataSourceId);
      }
      catch (SQLException e) {
        logger.error("Failed to commit database transaction: startTime={}, dataSourceId={}", startTime, dataSourceId, e);
        throw new BssException("提交数据库事务失败: dataSourceId=" + dataSourceId, e);
      }
    }
    // 释放连接
    releaseConnections();
  }

  /**
   * 回滚数据库事务
   * <p>回滚失败时只记录日志，不抛异常，以免掩盖原始异常</p>
   */
  public void rollbackTransaction() {
    // 是否应该匹配数据源编码
    for (Entry<Long, DataSourceHolder> entry : dataSourceHolderMap.entrySet()) {
      Long dataSourceId = entry.getKey();
      try {
        ConnectionWrapper connection = entry.getValue().getConnection();
        if (connection != null) {
          connection.rollback();
        }
        logger.debug("Rollback database transaction successfully: startTime={}, dataSourceId={}", startTime, dataSourceId);
      }
      catch (Exception e) {
        logger.error("Failed to rollback database transaction: startTime={}, dataSourceId={}", startTime, dataSourceId, e);
      }
    }
    // 释放连接
    releaseConnections();
  }

  /**
   * 释放数据库连接
   * <p>提交/回滚事务时释放连接到连接池（后面需要使用时再从连接池获取新连接），避免长时间占用连接导致连接池耗尽，影响其它服务执行。
   * 主要应对服务中调用了外部服务等耗时较长的场景，这种情况下可以在调用外部服务前先释放数据库连接。</p>
   */
  private void releaseConnections() {
    for (Entry<Long, DataSourceHolder> dataSource : dataSourceHolderMap.entrySet()) {
      DataSourceHolder holder = dataSource.getValue();
      if (holder != null) {
        ConnectionWrapper connection = holder.getConnection();
        if (connection != null) {
          // 释放数据库连接
          dataSourceProvider.closeConnection(connection);
        }
      }
    }
  }

  /**
   * 添加清理器
   */
  public void addCleaner(Runnable cleaner) {
    cleaners.add(cleaner);
  }

  /**
   * 执行清理器
   */
  public void cleanup() {
    for (Runnable cleaner : cleaners) {
      try {
        cleaner.run();
      }
      catch (Exception e) {
        logger.warn("Failed to execute cleaner", e);
      }
    }
  }
}
