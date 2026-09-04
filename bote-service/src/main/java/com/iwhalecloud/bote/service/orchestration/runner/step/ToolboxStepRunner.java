package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.cache.ToolboxCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.util.GroovyUtil;
import com.iwhalecloud.bote.common.util.ParamConverterUtil;
import com.iwhalecloud.bote.common.util.PythonUtil;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.log.OrchestrationStepRunLog;
import com.iwhalecloud.bote.dto.orchestration.step.ToolboxStep;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bote.dto.skill.SimpleFunctionSkillDTO;
import com.iwhalecloud.bote.service.orchestration.runner.AbstractStepRunner;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 工具箱步骤执行器
 *
 * @author bianjp
 * @since 2024-08-29
 */
public class ToolboxStepRunner extends AbstractStepRunner<ToolboxStep> {
  private static final ToolboxCache toolboxCache = SpringUtil.getBean(ToolboxCache.class);

  @Override
  protected void doRun(SceneOrchestrationContext context, ToolboxStep step) {
    Map<String, Object> scriptParams = buildRequestParametersToMap(step.getParameters());
    Object output = invokeToolbox(context.getTenantId(), step.getFuncId(), scriptParams, context.getLastStepRunLogOptional().orElse(null));
    context.setStepOutput(step, output);
  }

  @Override
  @Nullable
  public Object runAsTool(SceneChatParamsDTO sceneChatParams, ToolboxStep step, String toolCallId, @Nullable Map<String, Object> toolArguments, Optional<OrchestrationStepRunLog> log) {
    return invokeToolbox(sceneChatParams.getTenantId(), step.getFuncId(), toolArguments, log.orElse(null));
  }

  /**
   * 调用工具箱
   */
  @Nullable
  public static Object invokeToolbox(Long tenantId, Long funcId, @Nullable Map<String, Object> scriptParams, @Nullable OrchestrationStepRunLog log) {
    Assert.notNull(funcId, "funcId 不能为空");
    SimpleFunctionSkillDTO function = toolboxCache.getFunction(tenantId, funcId);
    Assert.notNull(function, () -> "服务函数不存在: " + funcId);
    Assert.hasLength(function.getScriptContent(), () -> "服务函数的脚本内容为空: id=" + funcId);
    Assert.isTrue(function.getRequest() == null || function.getRequest().isObject(), "服务函数的入参必须是对象");

    // 转换后的参数，用于记录日志，使用 Map 相比数组的可读性更好
    Map<String, Object> convertedParams = ParamConverterUtil.convertRoot(function.getRequest(), scriptParams);
    // 脚本参数，必须使用数组
    Object[] arguments = ParamConverterUtil.convertMapToArray(function.getRequest(), convertedParams);
    if (log != null) {
      log.setInput(convertedParams);
    }
    Object result;
    if (BaseConsts.SCRIPT_TYPE_GROOVY.equalsIgnoreCase(function.getFuncType())) {
      result = GroovyUtil.invoke(function.getScriptContent(), arguments);
    }
    else if (BaseConsts.SCRIPT_TYPE_PYTHON3.equalsIgnoreCase(function.getFuncType())) {
      // 解析 Python 包依赖列表
      String pyPackage = function.getPyPackage();
      List<String> pyPackageList = null;
      if (StringUtils.isNotEmpty(pyPackage)) {
        pyPackageList = JsonUtil.parseJson(pyPackage, new TypeReference<>() { });
      }
      result = PythonUtil.invoke(function.getScriptContent(), pyPackageList, arguments);
    }
    else {
      throw new BssException("未知的脚本类型: " + function.getFuncType());
    }
    // 转换出参结构
    if (result != null && function.getResponse() != null) {
      result = ParamConverterUtil.convert("", function.getResponse(), result);
    }
    return result;
  }

}
