package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.util.GroovyUtil;
import com.iwhalecloud.bote.common.util.ParamConverterUtil;
import com.iwhalecloud.bote.common.util.PythonUtil;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.step.ScriptStep;
import com.iwhalecloud.bote.service.orchestration.runner.AbstractStepRunner;
import com.iwhalecloud.bss.litchi.base.exception.BssException;

import java.io.StringWriter;
import java.util.Map;
import org.springframework.util.Assert;

/**
 * 代码块步骤执行器
 *
 * @author bianjp
 * @since 2024-08-29
 */
public class ScriptStepRunner extends AbstractStepRunner<ScriptStep> {
  @Override
  protected void doRun(SceneOrchestrationContext context, ScriptStep step) {
    Assert.isTrue(step.getParameters() == null || step.getParameters().isObject(), "脚本的入参必须是对象");
    // 转换后的参数，用于记录日志，使用 Map 相比数组的可读性更好
    Map<String, Object> convertedParams = buildRequestParametersToMap(step.getParameters());
    // 脚本参数，必须使用数组
    Object[] arguments = ParamConverterUtil.convertMapToArray(step.getParameters(), convertedParams);
    context.setStepInputLog(convertedParams);
    Object result;
    if (BaseConsts.SCRIPT_TYPE_GROOVY.equalsIgnoreCase(step.getScriptType())) {
      result = GroovyUtil.invoke(step.getScriptContent(), arguments);
    }
    else if (BaseConsts.SCRIPT_TYPE_PYTHON3.equalsIgnoreCase(step.getScriptType())) {
      StringWriter stdoutWriter = new StringWriter();
      try {
        result = PythonUtil.invoke(step.getScriptContent(), "temp.py", step.getPyPackageList(), stdoutWriter, arguments);
      } finally {
        // 将脚本的标准输出记录到步骤日志中，方便排查问题
        context.addStepLog("stdout: %s", stdoutWriter.toString());
      }

    }
    else {
      throw new BssException("未知的脚本类型: " + step.getScriptType());
    }
    // 转换出参结构
    if (result != null && step.getOutData() != null) {
      result = ParamConverterUtil.convert("", step.getOutData(), result);
    }
    context.setStepOutput(step, result);
  }

}
