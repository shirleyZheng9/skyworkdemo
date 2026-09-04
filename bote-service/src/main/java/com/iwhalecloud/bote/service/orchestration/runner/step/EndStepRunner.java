package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.step.EndStep;
import com.iwhalecloud.bote.service.orchestration.runner.AbstractStepRunner;

/**
 * 结束步骤执行器
 *
 * @author bianjp
 * @since 2024-08-29
 */
public class EndStepRunner extends AbstractStepRunner<EndStep> {
  @Override
  protected void doRun(SceneOrchestrationContext context, EndStep step) {
    if (context.setReturned()) {
      // 对话流、复杂场景中遇到结束节点标记场景结束
      if (Boolean.TRUE.equals(context.getDsl().getChatflow())) {
        context.setSceneFinished(true);
      }
      if (step.getParameters() != null) {
        context.setOutputParameters(buildRequestParametersToMap(step.getParameters()));
      }
    }
  }
}
