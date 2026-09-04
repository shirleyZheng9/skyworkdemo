package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.dto.orchestration.context.ParallelBranchContext;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.step.ParallelStep;
import com.iwhalecloud.bote.service.orchestration.runner.AbstractStepRunner;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

/**
 * 并行步骤执行器
 *
 * @author bianjp
 * @since 2024-11-06
 */
public class ParallelStepRunner extends AbstractStepRunner<ParallelStep> {
  @Override
  protected void doRun(SceneOrchestrationContext context, ParallelStep step) {
    if (CollectionUtils.isEmpty(step.getBranches())) {
      context.addStepLog("并行分支列表为空，跳过步骤");
      return;
    }

    // 构造任务
    List<Runnable> tasks = new ArrayList<>(step.getBranches().size());
    List<ParallelBranchContext> parallelBranchContexts = new ArrayList<>(step.getBranches().size());
    for (String branch : step.getBranches()) {
      ParallelBranchContext parallelBranchContext = new ParallelBranchContext(branch, context.isEnableStepLogs());
      parallelBranchContexts.add(parallelBranchContext);
      tasks.add(new ParallelBranchTask(context, parallelBranchContext));
    }

    // 执行任务
    try {
      ThreadPools.invokeTasks(ThreadPools.getOrchestration(), tasks);
    }
    catch (Exception e) {
      // 忽略退出并行异常
      if (!ExpUtil.hasCause(e, BreakParallelException.class)) {
        throw e;
      }
    }
    finally {
      // 将所有并行分支的执行日志设置到并行步骤的日志对象上
      context.getLastStepRunLogOptional().ifPresent(log -> log.setParallelBranchesLogs(parallelBranchContexts.stream()
        .map(ParallelBranchContext::getStepLogs)
        .collect(Collectors.toList())));
    }
  }

  /**
   * 并行分支任务
   */
  @RequiredArgsConstructor
  private static final class ParallelBranchTask implements Runnable {
    /** 编排上下文 */
    private final SceneOrchestrationContext context;
    /** 并行分支上下文 */
    private final ParallelBranchContext parallelBranchContext;

    @Override
    public void run() {
      try {
        context.pushParallelBranchContext(parallelBranchContext);
        // 执行分支步骤及其所有下一步
        executeSteps(context, parallelBranchContext.getBranch());
        // 如果返回了出参，抛个异常，以便提前结束其它并行分支
        if (context.isReturned()) {
          throw new BreakParallelException();
        }
      }
      finally {
        context.removeParallelBranchContext();
      }
    }
  }

}
