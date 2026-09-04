package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.cache.LlmPluginCache;
import com.iwhalecloud.bote.common.consts.SceneConsts;
import com.iwhalecloud.bote.common.sse.SseUtil;
import com.iwhalecloud.bote.common.util.GroovyUtil;
import com.iwhalecloud.bote.common.util.ParamConverterUtil;
import com.iwhalecloud.bote.common.util.ParamUtil;
import com.iwhalecloud.bote.common.util.ScenePromptUtil;
import com.iwhalecloud.bote.common.util.TemplateUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.log.OrchestrationStepRunLog;
import com.iwhalecloud.bote.dto.orchestration.step.LlmSkillStep;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bote.dto.skill.SimpleLlmPluginDTO;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import groovy.lang.GString;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 大模型能力步骤执行器
 *
 * @author bianjp
 * @since 2024-08-30
 */
public class LlmSkillStepRunner extends AbstractLlmStepRunner<LlmSkillStep> {
  private final LlmPluginCache llmPluginCache = SpringUtil.getBean(LlmPluginCache.class);

  @Override
  protected void doRun(SceneOrchestrationContext context, LlmSkillStep step) {
    Assert.notNull(step.getApiId(), "apiId 不能为空");
    // 解析参数
    Map<String, Object> params = buildRequestParametersToMap(step.getParameters());
    String result = invokePlugin(step.getApiId(), params, context.getRequest().getMessageContent(), context.getTenantId(),
      context.getLastStepRunLogOptional().orElse(null));
    context.setStepOutput(step, ImmutableMap.of("text", StringUtils.defaultString(result)));
  }

  @Override
  public Object runAsTool(SceneChatParamsDTO sceneChatParams, LlmSkillStep step, String toolCallId, @Nullable Map<String, Object> toolArguments, Optional<OrchestrationStepRunLog> log) {
    Assert.notNull(step.getApiId(), "apiId 不能为空");
    return invokePlugin(step.getApiId(), toolArguments, sceneChatParams.getMessageContent(), sceneChatParams.getTenantId(), log.orElse(null));
  }

  /**
   * 调用插件
   */
  private String invokePlugin(Long pluginId, @Nullable Map<String, Object> params, String messageContent, Long tenantId, @Nullable OrchestrationStepRunLog log) {
    SimpleLlmPluginDTO plugin = llmPluginCache.getPlugin(tenantId, pluginId);
    Assert.notNull(plugin, () -> "插件不存在: apiId=" + pluginId);
    Assert.notNull(plugin.getModelId(), () -> "插件未配置大模型: apiId=" + pluginId);

    // 转换入参
    Map<String, Object> convertedParams = ParamConverterUtil.convertRoot(plugin.getRequest(), params);
    // 最终使用的参数，允许前置脚本新增、覆盖参数
    Map<String, Object> finalParams = new HashMap<>(convertedParams);
    // 执行前置脚本，用于处理入参
    finalParams.putAll(executePreScript(plugin, plugin.getRequest(), convertedParams));
    // 填充系统参数
    fillSystemParams(messageContent, plugin.getRequest(), finalParams);

    // 获取模型 ID
    Long modelId = getEffectiveModelId(tenantId, plugin.getModelId());
    LlmClient modelClient = modelClientCache.getLlmClient(tenantId, modelId);
    // 解析提示词
    String prompt;
    if (plugin.getPromptId() != null) {
      prompt = ScenePromptUtil.resolvePrompt(tenantId, modelId, plugin.getPromptId(), key -> ParamUtil.getNestedProperty(finalParams, key));
    }
    else {
      prompt = TemplateUtil.resolveTemplate(plugin.getPromptContent(), key -> ParamUtil.getNestedProperty(finalParams, key));
    }
    Assert.hasText(prompt, () -> "插件的提示词不能为空: apiId=" + pluginId);

    // 调用大模型
    if (log != null) {
      log.setInput(Map.of("modelId", modelId, "modelName", modelClient.defaultModel(), "prompt", prompt));
    }
    ChatCompletionRequest request = ChatCompletionRequest.builder().tenantId(tenantId).addUserMessage(prompt).build();
    ChatCompletionResponse response = modelClient.chatCompletion(request, SseUtil.requestListener);
    if (log != null) {
      log.addLog("%s", JsonUtil.toJsonString(response));
    }

    // 执行后置脚本，用于替换大模型返回的消息内容
    return executePostScript(plugin, response.getMessageContent());
  }

  /**
   * 填充系统参数
   */
  private void fillSystemParams(String messageContent, @Nullable ParameterSpec root, Map<String, Object> params) {
    if (root == null || !root.hasChildren()) {
      return;
    }
    for (ParameterSpec child : root.getChildren()) {
      if (SceneConsts.TOOL_PARAM_QUERY.equals(child.getName())) {
        params.put(SceneConsts.TOOL_PARAM_QUERY, messageContent);
      }
    }
  }

  /**
   * 执行前置脚本，用于处理入参
   */
  @SuppressWarnings("unchecked")
  private Map<String, Object> executePreScript(SimpleLlmPluginDTO plugin, @Nullable ParameterSpec spec, Map<String, Object> params) {
    if (StringUtils.isEmpty(plugin.getPreScript())) {
      return Collections.emptyMap();
    }
    Object[] scriptParams;
    if (spec == null || !spec.hasChildren()) {
      scriptParams = new Object[0];
    }
    else {
      scriptParams = spec.getChildren().stream().map(s -> params.get(s.getName())).toArray();
    }
    Object result = GroovyUtil.invoke(plugin.getPreScript(), scriptParams);
    if (result != null) {
      Assert.isTrue(result instanceof Map, () -> "大模型能力的前置脚本必须返回 Map 类型: apiId=" + plugin.getPluginId());
      return (Map<String, Object>) result;
    }
    return Collections.emptyMap();
  }

  /**
   * 执行后置脚本，用于替换大模型返回的消息内容
   */
  private String executePostScript(SimpleLlmPluginDTO plugin, String message) {
    if (StringUtils.isEmpty(plugin.getPostScript())) {
      return message;
    }
    Object result = GroovyUtil.invoke(plugin.getPostScript(), message);
    if (result != null) {
      Assert.isTrue(result instanceof GString || result instanceof String, () -> "大模型能力的后置脚本必须返回 String 类型: apiId=" + plugin.getPluginId());
      message = result instanceof GString ? result.toString() : (String) result;
    }
    return message;
  }

}
