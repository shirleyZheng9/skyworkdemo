package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.step.ContinueStep;
import com.iwhalecloud.bote.service.orchestration.runner.AbstractStepRunner;

/**
 * 继续循环步骤执行器
 *
 * @author bianjp
 * @since 2024-08-29
 */
public class ContinueStepRunner extends AbstractStepRunner<ContinueStep> {
  @Override
  protected void doRun(SceneOrchestrationContext context, ContinueStep step) {
    throw new ContinueLoopException();
  }
}
