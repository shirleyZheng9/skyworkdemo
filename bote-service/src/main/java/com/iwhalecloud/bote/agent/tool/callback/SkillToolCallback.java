package com.iwhalecloud.bote.agent.tool.callback;

import com.fasterxml.jackson.databind.JsonNode;
import com.iwhalecloud.bote.agent.tool.context.ToolContext;
import com.iwhalecloud.bote.agent.tool.support.ToolExecutionResult;
import com.iwhalecloud.bote.agent.tool.util.ToolCallResultUtil;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.model.SkillToolDTO;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bote.llm.client.dto.Tool;
import com.iwhalecloud.bote.service.orchestration.SceneStepRegistry;
import com.iwhalecloud.bote.service.orchestration.runner.AbstractStepRunner;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

/**
 * 普通技能工具回调
 *
 * @author bianjp
 * @since 2026-02-03
 */
@RequiredArgsConstructor
public class SkillToolCallback implements ToolCallback {
  private final SceneChatParamsDTO sceneChatParams;
  @Getter
  private final SkillToolDTO skillTool;

  @Override
  public Tool getTool() {
    return skillTool.getTool();
  }

  @Override
  public boolean hideToolCall() {
    // 对于页面、页面函数隐藏工具调用
    return ChatMessageType.PAGE.getCode().equals(skillTool.getSkillType()) || ChatMessageType.PAGE_FUNC.getCode().equals(skillTool.getSkillType());
  }

  @Override
  @Nullable
  @SuppressWarnings({"rawtypes", "unchecked"})
  public Object call(@Nullable Map<String, Object> parameters, @Nullable ToolContext toolContext) {
    AbstractStep step = skillTool.getStep();
    AbstractStepRunner runner = SceneStepRegistry.getRunner(skillTool.getSkillType());
    Object result = runner.runAsTool(sceneChatParams, step, "", parameters, Optional.empty());
    // 从 MCP 工具返回结果中解析特殊事件
    if (StepType.MCP.equals(skillTool.getSkillType()) && result instanceof Map<?, ?>) {
      JsonNode json = JsonUtil.convert(result, JsonNode.class);
      boolean isError = json.path("isError").asBoolean();
      if (!isError) {
        String text = json.path("content").path(0).path("text").asText();
        ToolExecutionResult toolExecutionResult = ToolCallResultUtil.extractEvent(text, toolContext != null ? toolContext.tenantId() : null);
        if (toolExecutionResult != null) {
          return toolExecutionResult;
        }
      }
    }
    return result;
  }
}
