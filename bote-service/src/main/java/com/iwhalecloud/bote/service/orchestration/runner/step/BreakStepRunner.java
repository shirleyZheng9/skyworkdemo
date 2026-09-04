package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.step.BreakStep;
import com.iwhalecloud.bote.service.orchestration.runner.AbstractStepRunner;

/**
 * 中断循环步骤执行器
 *
 * @author bianjp
 * @since 2024-08-29
 */
public class BreakStepRunner extends AbstractStepRunner<BreakStep> {
  @Override
  protected void doRun(SceneOrchestrationContext context, BreakStep step) {
    throw new BreakLoopException();
  }
}
