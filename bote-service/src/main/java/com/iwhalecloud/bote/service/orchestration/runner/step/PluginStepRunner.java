package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.log.OrchestrationStepRunLog;
import com.iwhalecloud.bote.dto.orchestration.step.PluginStep;
import com.iwhalecloud.bote.dto.plugin.PluginExecuteParams;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bote.service.orchestration.runner.AbstractStepRunner;
import com.iwhalecloud.bote.service.plugin.IPluginEngine;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.Map;
import java.util.Optional;
import org.springframework.lang.Nullable;

/**
 * 插件步骤执行器
 *
 * @author qian.sisheng
 * @since 2025-04-22
 */
public class PluginStepRunner extends AbstractStepRunner<PluginStep> {

  private static final IPluginEngine pluginEngine = SpringUtil.getBean(IPluginEngine.class);

  @Override
  protected void doRun(SceneOrchestrationContext context, PluginStep step) {
    Map<String, Object> params = buildRequestParametersToMap(step.getParameters());
    context.setStepInputLog(params);
    Long modelId = getEffectiveModelId(context, step.getModelId());
    Object response = executePlugin(context.getTenantId(), modelId, step, params, false);
    context.setStepOutput(step, response);
  }

  @Override
  @Nullable
  public Object runAsTool(SceneChatParamsDTO sceneChatParams, PluginStep step, String toolCallId, @Nullable Map<String, Object> toolArguments, Optional<OrchestrationStepRunLog> log) {
    log.ifPresent(l -> l.setInput(toolArguments));
    Long modelId = getEffectiveModelId(sceneChatParams.getTenantId(), step.getModelId());
    return executePlugin(sceneChatParams.getTenantId(), modelId, step, toolArguments, true);
  }

  private Object executePlugin(Long tenantId, Long modelId, PluginStep step, @Nullable Map<String, Object> params, boolean isToolCall) {
    PluginExecuteParams pluginParams = new PluginExecuteParams();
    pluginParams.setPluginId(step.getPluginId());
    pluginParams.setTenantId(tenantId);
    pluginParams.setParams(params);
    pluginParams.setModelId(modelId);
    pluginParams.setIsPluginHub(step.getIsPluginHub());
    pluginParams.setToolName(step.getToolName());
    pluginParams.setIsToolCall(isToolCall);
    return pluginEngine.execute(pluginParams);
  }

}
