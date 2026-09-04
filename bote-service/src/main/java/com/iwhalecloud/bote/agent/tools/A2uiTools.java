package com.iwhalecloud.bote.agent.tools;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iwhalecloud.bote.agent.agents.A2uiGenerator;
import com.iwhalecloud.bote.agent.annotation.Tool;
import com.iwhalecloud.bote.agent.annotation.ToolParam;
import com.iwhalecloud.bote.agent.tool.context.ToolContext;
import com.iwhalecloud.bote.agent.tool.exception.ToolExecutionException;
import com.iwhalecloud.bote.agent.tool.support.ToolExecutionResult;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.dto.agent.A2uiGenerateResult;
import com.iwhalecloud.bote.llm.client.LlmClient;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * A2UI 相关工具
 *
 * @author bianjp
 * @since 2026-03-17
 */
public final class A2uiTools {
  /** Agent Skill 技能名称 */
  public static final String SKILL_NAME = "a2ui";
  /** 工具名称 */
  public static final String TOOL_NAME = "render_a2ui";

  private A2uiTools() {
  }

  /**
   * 根据提示词生成并渲染 A2UI 卡片
   */
  @Tool(name = TOOL_NAME,
    description = "根据业务需求生成 A2UI 界面（表单、列表、卡片等）并渲染",
    returnDirect = true, hideToolCall = true)
  public static ToolExecutionResult renderA2ui(@ToolParam(description = """
                                                 用自然语言描述待生成的 A2UI 界面需求，尽量具体：界面类型、字段名、校验规则、布局交互、展示数据、默认值等。
                                                 示例:
                                                 1. 生成一个用户登录表单，包含用户名(username)、密码(password)
                                                 2. 生成一个客户信息展示卡片：姓名=张三，年龄=18，性别=男
                                                 3. 生成一个收集客户信息的表单，字段包含姓名(name)、性别(gender)、年龄(age)；默认值：name=张三，gender=男
                                                 """) String prompt,
                                               ToolContext toolContext) {
    LlmClient modelClient = toolContext.modelClient();
    Assert.notNull(modelClient, "Error: 当前会话未提供大模型客户端，无法生成 A2UI");
    Assert.hasText(prompt, "Error: prompt is required");

    A2uiGenerateResult result = A2uiGenerator.generateA2UI(modelClient, prompt);
    if (!result.success()) {
      throw new ToolExecutionException("Error: " + result.error());
    }
    List<A2uiEvent> events = result.events();
    Assert.notEmpty(events, "Error: 生成的 A2UI 事件为空");
    return ToolExecutionResult.builder()
      .success(true)
      .result("Done")
      .msgType(ChatMessageType.A2UI)
      .msgContent(events)
      .build();
  }

  /**
   * A2UI 事件
   */
  @Schema(description = "A2UI event. Must provide exactly one of surfaceUpdate, dataModelUpdate, beginRendering, or deleteSurface")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record A2uiEvent(
    @Schema(description = "Define or update UI components")
    @Nullable
    Map<String, Object> surfaceUpdate,
    @Schema(description = "Update application state")
    @Nullable
    Map<String, Object> dataModelUpdate,
    @Schema(description = "Signal the client to render")
    @Nullable
    Map<String, Object> beginRendering,
    @Schema(description = "Remove a UI surface")
    @Nullable
    Map<String, Object> deleteSurface) {
  }
}
