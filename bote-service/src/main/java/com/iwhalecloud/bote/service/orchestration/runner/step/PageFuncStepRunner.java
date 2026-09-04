package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.iwhalecloud.bote.agent.tool.support.ToolExecutionResult;
import com.iwhalecloud.bote.cache.PageFuncCache;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.util.ParamConverterUtil;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.log.OrchestrationStepRunLog;
import com.iwhalecloud.bote.dto.orchestration.step.PageFuncStep;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bote.dto.skill.SimplePageFuncDTO;
import com.iwhalecloud.bote.service.orchestration.runner.AbstractStepRunner;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 页面函数步骤执行器
 *
 * @author bianjp
 * @since 2024-08-30
 */
public class PageFuncStepRunner extends AbstractStepRunner<PageFuncStep> {
  private static final PageFuncCache pageFuncCache = SpringUtil.getBean(PageFuncCache.class);

  @Override
  protected void doRun(SceneOrchestrationContext context, PageFuncStep step) {
    Map<String, Object> parameters = buildRequestParametersToMap(step.getParameters());
    context.setStepInputLog(parameters);
    Map<String, Object> output = buildPageFuncOutput(context.getTenantId(), null, step.getPageFuncId(), parameters);
    if (Boolean.TRUE.equals(step.getMemorized())) {
      // 记忆内容使用转换后的入参
      addMemoryContent(output, output.get("parameters"), step.getCustomMemorized(), step.getMemoryContent());
    }
    context.setStepOutput(step, output);
    context.getReplyHandler().reply(ChatMessageType.PAGE_FUNC, output, step.getCode(), step.getName());
  }

  @Override
  public Object runAsTool(SceneChatParamsDTO sceneChatParams, PageFuncStep step, String toolCallId, @Nullable Map<String, Object> toolArguments, Optional<OrchestrationStepRunLog> log) {
    log.ifPresent(l -> l.setInput(toolArguments));
    Map<String, Object> output = buildPageFuncOutput(sceneChatParams.getTenantId(), toolCallId, step.getPageFuncId(), toolArguments);
    // 如果是通用智能体，交给外层发送事件
    if (sceneChatParams.isGeneralAgent()) {
      return ToolExecutionResult.builder()
        .success(true)
        .result("Done")
        .returnDirect(false)
        .msgType(ChatMessageType.PAGE_FUNC)
        .msgContent(output)
        .build();
    }
    sceneChatParams.getReplyHandler().reply(ChatMessageType.PAGE_FUNC, output, step.getCode(), step.getName());
    return output;
  }

  /**
   * 构造页面函数的输出
   */
  private Map<String, Object> buildPageFuncOutput(Long tenantId, @Nullable String toolCallId, Long pageFuncId, @Nullable Map<String, Object> parameters) {
    Assert.notNull(pageFuncId, "pageFuncId 不能为空");
    SimplePageFuncDTO pageFunc = pageFuncCache.get(tenantId, pageFuncId);
    Assert.notNull(pageFunc, () -> "页面函数不存在: pageFuncId=" + pageFuncId);

    // 转换入参
    Map<String, Object> convertedParams = ParamConverterUtil.convertRoot(pageFunc.getRequest(), parameters);

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("toolCallId", toolCallId);
    result.put("tenantId", pageFunc.getTenantId());
    result.put("funcCode", pageFunc.getFuncCode());
    result.put("funcName", pageFunc.getFuncCode());
    result.put("parameters", convertedParams);
    return result;
  }

}
