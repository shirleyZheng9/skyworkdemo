package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.iwhalecloud.bote.cache.ChatflowContextCache;
import com.iwhalecloud.bote.common.util.ParamConverterUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineRequest;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.step.StartStep;
import com.iwhalecloud.bote.service.orchestration.runner.AbstractStepRunner;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;

/**
 * 开始步骤执行器
 *
 * @author bianjp
 * @since 2024-08-29
 */
public class StartStepRunner extends AbstractStepRunner<StartStep> {
  private final ChatflowContextCache chatflowContextCache = SpringUtil.getBean(ChatflowContextCache.class);

  @Override
  protected void doRun(SceneOrchestrationContext context, StartStep step) {
    OrchestrationEngineRequest request = context.getRequest();
    // 处理入参
    Map<String, Object> parsedParams = ParamConverterUtil.convertRoot(context.getDsl().getInput(), request.getParameters());
    context.setParsedInputParameters(parsedParams);
    // 对话流需要处理全局变量
    if (context.shouldPersistContext()) {
      initGlobalVariables(context, request);
    }
  }

  /**
   * 初始化全局变量
   */
  private void initGlobalVariables(SceneOrchestrationContext context, OrchestrationEngineRequest request) {
    // 查询上次的全局变量状态
    Map<String, Object> lastGlobalVariables = chatflowContextCache.get(request.getSceneId(), request.getFlowId(), request.getContextId());
    if (MapUtils.isEmpty(lastGlobalVariables)) {
      return;
    }
    // 需要对变量值做类型转换（缓存使用 JSON 格式，不能保证类型正确）
    for (ParameterSpec variable : context.getDsl().getVariables()) {
      Object value = MapUtils.getObject(lastGlobalVariables, variable.getName());
      if (value != null) {
        value = ParamConverterUtil.convert(variable.getName(), variable, value);
        context.getGlobalVariables().put(variable.getName(), value);
      }
    }
  }
}
