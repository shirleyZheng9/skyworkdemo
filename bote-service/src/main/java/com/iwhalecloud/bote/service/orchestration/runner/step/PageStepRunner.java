package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.iwhalecloud.bote.agent.tool.support.ToolExecutionResult;
import com.iwhalecloud.bote.cache.PageCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.util.ParamConverterUtil;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.log.OrchestrationStepRunLog;
import com.iwhalecloud.bote.dto.orchestration.step.PageStep;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bote.dto.skill.SimplePageDTO;
import com.iwhalecloud.bote.dto.skill.SimplePageTemplateDTO;
import com.iwhalecloud.bote.service.orchestration.runner.AbstractStepRunner;
import com.iwhalecloud.bote.service.skill.impl.helper.ParsePageTemplateHelper;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 页面步骤执行器
 *
 * @author bianjp
 * @since 2024-08-30
 */
public class PageStepRunner extends AbstractStepRunner<PageStep> {
  private static final PageCache pageCache = SpringUtil.getBean(PageCache.class);
  private static final ParsePageTemplateHelper pageHelper = SpringUtil.getBean(ParsePageTemplateHelper.class);

  @Override
  protected void doRun(SceneOrchestrationContext context, PageStep step) {
    Map<String, Object> parameters = buildRequestParametersToMap(step.getParameters());
    context.setStepInputLog(parameters);
    Map<String, Object> output = buildPageOutput(context.getTenantId(), null, step.getPageId(), parameters);
    if (Boolean.TRUE.equals(step.getMemorized())) {
      // 记忆内容使用转换后的入参
      addMemoryContent(output, output.get("parameters"), step.getCustomMemorized(), step.getMemoryContent());
    }
    context.setStepOutput(step, output);
    context.getReplyHandler().reply(ChatMessageType.PAGE, output, step.getCode(), step.getName());
  }

  @Override
  public Object runAsTool(SceneChatParamsDTO sceneChatParams, PageStep step, String toolCallId, @Nullable Map<String, Object> toolArguments,
                          Optional<OrchestrationStepRunLog> log) {
    log.ifPresent(l -> l.setInput(toolArguments));
    Map<String, Object> output = buildPageOutput(sceneChatParams.getTenantId(), toolCallId, step.getPageId(), toolArguments);

    // 如果是通用智能体，交给外层发送事件
    if (sceneChatParams.isGeneralAgent()) {
      return ToolExecutionResult.builder()
        .success(true)
        .result("Done")
        // 结束 ReAct 循环
        .returnDirect(true)
        .msgType(ChatMessageType.PAGE)
        .msgContent(output)
        .build();
    }

    sceneChatParams.getReplyHandler().reply(ChatMessageType.PAGE, output, step.getCode(), step.getName());
    return output;
  }

  /**
   * 构造页面的输出
   */
  public static Map<String, Object> buildPageOutput(Long tenantId, @Nullable String toolCallId, Long pageId, @Nullable Map<String, Object> parameters) {
    Assert.notNull(pageId, "pageId 不能为空");
    SimplePageDTO page = pageCache.get(tenantId, pageId);
    Assert.notNull(page, () -> "页面不存在: pageId=" + pageId);

    // 转换入参
    Map<String, Object> convertedParams = ParamConverterUtil.convertRoot(page.getRequest(), parameters);

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("toolCallId", toolCallId);
    result.put("pageName", page.getPageName());
    result.put("pageCode", page.getPageCode());
    result.put("pageTitle", page.getPageTitle());
    result.put("pageSourceType", page.getPageSourceType());
    result.put("pageType", page.getPageType());
    result.put("parameters", convertedParams);
    if (BaseConsts.PAGE_SOURCE_PLATFORM.equals(page.getPageSourceType()) && page.getPageTemplate() != null) {
      result.put("pageContent", page.getPageTemplate());
      result.put("staticCodeList", pageHelper.getStaticCodeList(tenantId, JsonUtil.convert(page.getPageTemplate(), SimplePageTemplateDTO.class)));
    }
    return result;
  }
}
