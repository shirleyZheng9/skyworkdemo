package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.common.sse.SseInvoker;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.log.OrchestrationStepRunLog;
import com.iwhalecloud.bote.dto.orchestration.step.ServiceStep;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bote.dto.skill.ApiServiceParams;
import com.iwhalecloud.bote.service.engine.ApiSkillEngine;
import com.iwhalecloud.bote.service.orchestration.runner.AbstractStepRunner;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections4.MapUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 服务步骤执行器
 *
 * @author bianjp
 * @since 2024-08-29
 */
public class ServiceStepRunner extends AbstractStepRunner<ServiceStep> {
  private static final ApiSkillEngine apiSkillEngine = SpringUtil.getBean(ApiSkillEngine.class);

  @Override
  protected void doRun(SceneOrchestrationContext context, ServiceStep step) {
    // 请求参数，包括 header, path, query, body 4 个属性
    Map<String, Object> params = buildRequestParametersToMap(step.getParameters());
    OrchestrationStepRunLog log = context.getLastStepRunLogOptional().orElse(null);
    Object response = doRequest(context.getTenantId(), step.getServiceId(), params, log);
    if (response instanceof SseInvoker invoker) {
      // 流式输出完成后，修改节点出参为最终输出内容
      invoker.setFinishCallback(answer -> context.setStepOutput(step, answer.toMap(), log));
      invoker.setErrorCallback(e -> {
        // 删除出参中的 SseInvoker, 避免在异常分支中重复触发
        context.setStepOutput(step, ImmutableMap.of("text", ""), log);
        processStreamException(context, step, e, log);
      });
      context.setStepOutput(step, ImmutableMap.of("text", invoker));
    }
    else {
      context.setStepOutput(step, response);
    }
  }

  @Override
  @Nullable
  public Object runAsTool(SceneChatParamsDTO sceneChatParams, ServiceStep step, String toolCallId, @Nullable Map<String, Object> toolArguments, Optional<OrchestrationStepRunLog> log) {
    // 留给 API 技能引擎转换入参
    Object response = doRequest(sceneChatParams.getTenantId(), step.getServiceId(), toolArguments, log.orElse(null));
    Assert.isTrue(!(response instanceof SseInvoker), () -> "提示词模式的智能体不支持调用 SSE 接口: " + step.getName());
    return response;
  }

  /**
   * 执行请求
   */
  @Nullable
  @SuppressWarnings("unchecked")
  private Object doRequest(Long tenantId, Long serviceId, @Nullable Map<String, Object> params, @Nullable OrchestrationStepRunLog log) {
    Assert.notNull(serviceId, "serviceId 不能为空");
    ApiServiceParams apiParams = new ApiServiceParams();
    if (MapUtils.isNotEmpty(params)) {
      apiParams.setPath((Map<String, Object>) params.get("path"));
      apiParams.setHeader((Map<String, Object>) params.get("header"));
      apiParams.setQuery((Map<String, Object>) params.get("query"));
      apiParams.setBody(params.get("body"));
    }
    if (log != null) {
      log.setInput(apiParams);
    }
    return apiSkillEngine.execute(tenantId, serviceId, apiParams);
  }

}
