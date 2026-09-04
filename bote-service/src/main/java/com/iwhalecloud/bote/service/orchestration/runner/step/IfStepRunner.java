package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.iwhalecloud.bote.common.operator.ConditionEvaluator;
import com.iwhalecloud.bote.common.util.SceneParamUtil;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.log.OrchestrationStepRunLog;
import com.iwhalecloud.bote.dto.orchestration.step.IfStep;
import com.iwhalecloud.bote.dto.orchestration.step.IfStep.BranchSpec;
import com.iwhalecloud.bote.service.orchestration.runner.AbstractStepRunner;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.lang.Nullable;

/**
 * 如果步骤执行器
 *
 * @author bianjp
 * @since 2024-09-04
 */
public class IfStepRunner extends AbstractStepRunner<IfStep> {
  @Nullable
  @Override
  protected String doRunWithReturn(SceneOrchestrationContext context, IfStep step) {
    for (BranchSpec branch : step.getBranches()) {
      if (ConditionEvaluator.evaluate(branch.getCondition(), SceneParamUtil::getParamValue)) {
        context.addStepLog("条件匹配: %s", ObjectUtils.getIfNull(branch.getCondition(), ""));
        return branch.getNext();
      }
      context.addStepLog("条件不匹配: %s", branch.getCondition());
    }
    // 分支列表中包含 else, 肯定会匹配，不会执行到这里
    return null;
  }

  @Override
  public void debugStep(SceneOrchestrationContext context, IfStep step, OrchestrationStepRunLog log) {
    for (BranchSpec branch : step.getBranches()) {
      if (ConditionEvaluator.evaluate(branch.getCondition(), SceneParamUtil::getParamValue)) {
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("branchCode", branch.getBranchCode());
        output.put("branchName", branch.getBranchName());
        output.put("next", branch.getNext());
        log.addLog("条件匹配: %s", ObjectUtils.getIfNull(branch.getCondition(), ""));
        log.setOutput(output);
        break;
      }
      log.addLog("条件不匹配: %s", branch.getCondition());
    }
  }
}
