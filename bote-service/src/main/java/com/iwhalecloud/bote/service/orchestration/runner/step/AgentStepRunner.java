package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.agent.tool.util.ToolCallUtil;
import com.iwhalecloud.bote.cache.DynamicMcpClientCache;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.sse.SseInvoker;
import com.iwhalecloud.bote.common.sse.event.SseEvent;
import com.iwhalecloud.bote.common.sse.event.ToolCallEvent;
import com.iwhalecloud.bote.common.sse.event.ToolCallResultEvent;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.SceneParamUtil;
import com.iwhalecloud.bote.dto.chat.AnswerDTO;
import com.iwhalecloud.bote.dto.chat.agent.AgentReplyAssistantPart;
import com.iwhalecloud.bote.dto.chat.agent.AgentReplyPart;
import com.iwhalecloud.bote.dto.chat.agent.AgentReplyToolCallPart;
import com.iwhalecloud.bote.dto.model.SkillToolDTO;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.log.OrchestrationStepRunLog;
import com.iwhalecloud.bote.dto.orchestration.step.AgentStep;
import com.iwhalecloud.bote.dto.orchestration.step.AgentStep.CustomMcpService;
import com.iwhalecloud.bote.dto.orchestration.step.AgentStep.DynamicSkillsConfig;
import com.iwhalecloud.bote.dto.orchestration.step.AgentStep.FileProcessingStrategy;
import com.iwhalecloud.bote.dto.orchestration.step.McpStep;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bote.dto.skill.LlmSkillItem;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.Function;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.llm.client.dto.message.MessageContent;
import com.iwhalecloud.bote.llm.client.dto.message.SystemMessage;
import com.iwhalecloud.bote.llm.client.dto.message.UserMessage;
import com.iwhalecloud.bote.llm.helper.MarkdownHelper;
import com.iwhalecloud.bote.mcp.client.McpClient;
import com.iwhalecloud.bote.mcp.dto.McpTool;
import com.iwhalecloud.bote.service.model.helper.SkillToolConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.AgentStepConverter;
import com.iwhalecloud.bote.service.orchestration.helper.ToolUseBuffer;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import okhttp3.HttpUrl;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Agent 步骤执行器
 *
 * @author bianjp
 * @since 2025-05-24
 */
@SuppressWarnings("PMD.GuardLogStatement")
public class AgentStepRunner extends AbstractLlmStepRunner<AgentStep> {
  /** 系统提示词模板 */
  private static final Resource SYSTEM_PROMPT_TEMPLATE = new ClassPathResource("prompt-templates/agent-prompt.md");
  /** 默认的工具使用规则提示词 */
  public static final Resource DEFAULT_TOOL_USE_RULES_PROMPT = new ClassPathResource("prompt-templates/agent-prompt-tool-use-rules.md");
  private static final DynamicMcpClientCache dynamicMcpClientCache = SpringUtil.getBean(DynamicMcpClientCache.class);

  @Override
  protected void doRun(SceneOrchestrationContext context, AgentStep step) {
    Assert.hasText(step.getModelId(), "模型 ID 不能为空");
    // 获取模型 ID
    Long modelId = getEffectiveModelId(context, step.getModelId());
    LlmClient modelClient = modelClientCache.getLlmClient(context.getTenantId(), modelId);

    // 解析图片
    List<MessageContent> imageMessageContents = parseVisonImages(step.getVision(), modelClient, modelId);

    // 技能转为工具
    List<SkillToolDTO> skillTools = buildSkillTools(context, step, modelId);
    // 解析提示词
    List<Message> messages = buildMessages(context, modelId, step, skillTools, imageMessageContents);

    // 记录节点入参日志
    Optional<OrchestrationStepRunLog> logOptional = context.getLastStepRunLogOptional();
    logOptional.ifPresent(log -> {
      Map<String, Object> input = new HashMap<>();
      input.put("modelId", modelId);
      input.put("modelName", modelClient.defaultModel());
      input.put("messages", new ArrayList<>(messages));
      input.put("tools", skillTools.stream().map(SkillToolDTO::getTool).collect(Collectors.toList()));
      log.setInput(input);
    });

    OrchestrationStepRunLog log = logOptional.orElse(null);
    AgentInvoker handler = new AgentInvoker(context, step, messages, skillTools, modelClient, log);
    SseInvoker invoker = new SseInvoker(handler);
    invoker.setFinishCallback(handler::onFinish);
    invoker.setErrorCallback(e -> {
      // 删除出参中的 SseInvoker, 避免在异常分支中重复触发
      context.setStepOutput(step, ImmutableMap.of("text", "", "invokedTools", Collections.emptyList()), log);
      processStreamException(context, step, e, log);
    });
    context.setStepOutput(step, ImmutableMap.of("text", invoker, "invokedTools", Collections.emptyList()));
  }

  /**
   * 获取技能列表
   */
  private List<LlmSkillItem> getSkillItems(AgentStep step) {
    List<LlmSkillItem> skills = ListUtils.emptyIfNull(step.getSkills());
    DynamicSkillsConfig dynamicSkillsConfig = step.getDynamicSkillsConfig();
    // 未启用动态技能时直接返回
    if (dynamicSkillsConfig == null || !Boolean.TRUE.equals(dynamicSkillsConfig.getEnabled()) || StringUtils.isEmpty(dynamicSkillsConfig.getSkills())) {
      return skills;
    }

    // 合并固定技能和动态技能
    List<LlmSkillItem> dynamicSkills = resolveDynamicSkillItems(step, dynamicSkillsConfig);
    if (!dynamicSkills.isEmpty()) {
      List<LlmSkillItem> finalSkills = new ArrayList<>(skills);
      for (LlmSkillItem item : dynamicSkills) {
        // 忽略重复的技能
        if (finalSkills.stream().noneMatch(s -> Objects.equals(s.getSkillType(), item.getSkillType()) && Objects.equals(s.getSkillId(), item.getSkillId()))) {
          finalSkills.add(item);
        }
      }
      return finalSkills;
    }
    return skills;
  }

  /**
   * 解析动态技能列表
   */
  private List<LlmSkillItem> resolveDynamicSkillItems(AgentStep step, DynamicSkillsConfig dynamicSkillsConfig) {
    Object value = SceneParamUtil.getParamValue(dynamicSkillsConfig.getSkills());
    if (ObjectUtils.isEmpty(value)) {
      return Collections.emptyList();
    }
    if (!(value instanceof Collection)) {
      logger.warn("Invalid dynamic skills: step={}/{}, value={}", step.getName(), step.getCode(), value);
      throw new BssException("非法的动态技能列表，实际类型为 " + value.getClass().getName());
    }
    List<LlmSkillItem> dynamicSkillList;
    try {
      dynamicSkillList = JsonUtil.convert(value, new TypeReference<List<LlmSkillItem>>() {
      });
    }
    catch (IllegalArgumentException e) {
      logger.warn("Invalid dynamic skills: step={}/{}, value={}", step.getName(), step.getCode(), value);
      throw new BssException("非法的动态技能列表: " + ExpUtil.getMsg(e), e);
    }
    // 校验列表元素的合法性
    for (LlmSkillItem item : dynamicSkillList) {
      if (StringUtils.isEmpty(item.getSkillType()) || item.getSkillId() == null) {
        logger.warn("Invalid dynamic skill: step={}/{}, value={}", step.getName(), step.getCode(), item);
        throw new BssException("非法的动态技能列表，skillType, skillId 不能为空");
      }
      if (!AgentStepConverter.ALLOWED_SKILL_TYPES.contains(item.getSkillType())) {
        logger.warn("Invalid dynamic skill type: step={}/{}, value={}", step.getName(), step.getCode(), item);
        throw new BssException("非法的动态技能列表，不支持的技能类型: " + item.getSkillType());
      }
    }
    return dynamicSkillList;
  }

  /**
   * 构造技能工具列表
   */
  private List<SkillToolDTO> buildSkillTools(SceneOrchestrationContext context, AgentStep step, Long modelId) {
    boolean isDebug = context.getRequest() != null && Boolean.TRUE.equals(context.getRequest().getDebug());
    List<LlmSkillItem> skillItems = getSkillItems(step);
    List<SkillToolDTO> skillTools = SkillToolConverter.convert(context.getTenantId(), modelId, skillItems, isDebug);
    CustomMcpService customMcpService = step.getCustomMcpService();
    // 处理自定义 MCP 服务
    if (customMcpService != null && Boolean.TRUE.equals(customMcpService.getEnabled())) {
      String serverName = customMcpService.getServerName();
      String serverType = customMcpService.getServerType();
      String serverUrl = Objects.toString(SceneParamUtil.getParamValue(customMcpService.getServerUrl()), "");
      Assert.hasLength(serverName, "自定义 MCP 服务名称不能为空");
      Assert.hasLength(serverType, "自定义 MCP 服务类型不能为空");
      Assert.hasLength(serverUrl, "自定义 MCP 服务地址不能为空");
      Assert.notNull(HttpUrl.parse(serverUrl), () -> "自定义 MCP 服务地址不合法: " + serverUrl);
      McpClient client = dynamicMcpClientCache.getClient(serverName, serverType, serverUrl);

      // 确保列表可写
      skillTools = new ArrayList<>(skillTools);
      for (McpTool mcpTool : client.listAllToolsWithCache()) {
        skillTools.add(SkillToolConverter.buildMcpSkillTool(mcpTool, null, null, client));
      }
    }

    // 设置 MCP 技能的文件处理策略
    FileProcessingStrategy fileProcessingStrategy = step.getFileProcessingStrategy();
    if (fileProcessingStrategy != null) {
      for (SkillToolDTO skillTool : skillTools) {
        if (skillTool.getStep() instanceof McpStep) {
          ((McpStep) skillTool.getStep()).setFileProcessingStrategy(fileProcessingStrategy);
        }
      }
    }

    return skillTools;
  }

  /**
   * 构造消息列表
   */
  private List<Message> buildMessages(SceneOrchestrationContext context, Long modelId, AgentStep step, List<SkillToolDTO> skillTools,
                                      List<MessageContent> imageMessageContents) {
    // 解析工具使用规则
    String toolUseRulesPrompt = resolveTemplate(step.getToolUseRulesPrompt());
    if (StringUtils.isEmpty(toolUseRulesPrompt)) {
      try (InputStream inputStream = DEFAULT_TOOL_USE_RULES_PROMPT.getInputStream()) {
        toolUseRulesPrompt = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
      }
      catch (IOException e) {
        throw new BssException("加载 Agent 工具使用规则提示词失败", e);
      }
    }

    // 系统提示词
    String systemPrompt = buildSystemPrompt(skillTools, toolUseRulesPrompt);
    // 用户消息
    String userMessageText = resolvePrompt(context.getTenantId(), modelId, step.getPromptId(), step.getPromptParameters(), step.getPromptContent());
    Assert.hasLength(userMessageText, "提示词不能为空");
    // 历史消息
    List<Message> historyMessages = loadHistoryMessages(context, step.getMemory());
    List<Message> result = new ArrayList<>(historyMessages.size() + 2);
    result.add(new SystemMessage(systemPrompt));
    result.addAll(historyMessages);
    result.add(buildUserMessage(imageMessageContents, userMessageText));
    return result;
  }

  /**
   * 构造系统提示词
   */
  private String buildSystemPrompt(List<SkillToolDTO> skillTools, String toolUseRulesPrompt) {
    // 读取模板
    String systemPrompt;
    try (InputStream inputStream = SYSTEM_PROMPT_TEMPLATE.getInputStream()) {
      systemPrompt = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    }
    catch (IOException e) {
      throw new BssException("加载 Agent 系统提示词失败", e);
    }

    // 构建工具列表
    String toolsSection;
    if (skillTools.isEmpty()) {
      toolsSection = "";
    }
    else {
      StringBuilder sb = new StringBuilder(skillTools.size() * 200);
      for (SkillToolDTO tool : skillTools) {
        Function function = tool.getTool().getFunction();
        if (sb.length() > 0) {
          sb.append("\n\n");
        }
        sb.append("<tool>\n  <name>").append(function.getName()).append("</name>\n")
          .append("  <description>").append(function.getDescription()).append("</description>\n")
          .append("  <arguments>")
          .append(function.getParameters() != null ? JsonUtil.toJsonStringCompact(function.getParameters()) : "")
          .append("</arguments>\n</tool>");
      }
      toolsSection = sb.toString();
    }
    systemPrompt = Strings.CS.replace(systemPrompt, "{{AVAILABLE_TOOLS}}", toolsSection);
    systemPrompt = Strings.CS.replace(systemPrompt, "{{TOOL_USE_RULES}}", toolUseRulesPrompt.trim());
    return systemPrompt;
  }

  /**
   * Agent SSE 调用器
   */
  private class AgentInvoker implements Consumer<Consumer<SseEvent>> {
    private static final Logger logger = LoggerFactory.getLogger(AgentInvoker.class);
    /** 工具使用标签的匹配模式，用于提取工具名称和参数。注意 arguments 标签内可能有换行符，需要使用 DOTALL 模式; 不匹配结束标签，以兼容大模型返回的结束标签不正确的情况 */
    private static final Pattern TOOL_USE_PATTERN = Pattern.compile("<tool_use>\\s*<name>(.*?)</name>\\s*(?:<arguments>(.*?)</arguments>)?", Pattern.DOTALL);

    private final SceneOrchestrationContext context;
    private final AgentStep step;
    /** 消息列表（可变） */
    private final List<Message> messages;
    /** 初始的消息列表数量 */
    private final int initialMessageCount;
    private final List<SkillToolDTO> skillTools;
    private final LlmClient modelClient;
    @Nullable
    private final OrchestrationStepRunLog log;
    private final SceneChatParamsDTO sceneChatParams;

    /** 调用过的工具列表 */
    private final List<Map<String, Object>> invokedTools = new ArrayList<>();
    /** 回复片段列表，用于聊天窗口中的历史消息展示 */
    private final List<AgentReplyPart> replyParts = new ArrayList<>();

    public AgentInvoker(SceneOrchestrationContext context, AgentStep step, List<Message> messages, List<SkillToolDTO> skillTools,
                        LlmClient modelClient, @Nullable OrchestrationStepRunLog log) {
      this.context = context;
      this.step = step;
      this.skillTools = skillTools;
      this.messages = messages;
      this.initialMessageCount = messages.size();
      this.modelClient = modelClient;
      this.log = log;
      // 构造一个场景会话参数对象，用于复用 AbstractStepRunner#runAsTool 方法
      this.sceneChatParams = new SceneChatParamsDTO(context.getRequest());
    }

    @Override
    public void accept(Consumer<SseEvent> partialHandler) {
      Consumer<SseEvent> wrappedPartialHandler = wrapPartialHandler(partialHandler, !Boolean.FALSE.equals(step.getShowToolInvocation()));
      // 工具使用匹配缓冲，用于在流式输出的过程中动态匹配 tool_use 标签
      ToolUseBuffer toolUseBuffer = new ToolUseBuffer();
      Consumer<ChatCompletionResponse> eventHandler =
        response -> handlePartial(response, wrappedPartialHandler, toolUseBuffer);

      int toolCallLimit = SystemParameter.LLM_TOOL_CALL_LIMIT.getRequiredIntegerValueFromDb();
      try {
        for (int i = 0; i < toolCallLimit; i++) {
          toolUseBuffer.reset();
          ChatCompletionResponse response = invokeLlmStream(context, step, modelClient, messages, eventHandler, step.getCustomModelConfig());
          if (log != null) {
            log.addLog("第 %s 次响应: %s", i + 1, JsonUtil.toJsonString(response));
          }
          // 发送结尾的剩余片段
          String remainingContent = toolUseBuffer.getRemainingText();
          if (StringUtils.isNotBlank(remainingContent)) {
            partialHandler.accept(SseEvent.ofText(remainingContent));
          }
          String toolUseContent = toolUseBuffer.getToolUseContent();
          // 没有返回工具调用，直接结束
          if (StringUtils.isEmpty(toolUseContent)) {
            String text = response.getMessageContent();
            replyParts.add(new AgentReplyAssistantPart(text));
            messages.add(new AssistantMessage(text));
            return;
          }

          // 处理工具调用
          // 回复片段要删除 <tool_use> 标签
          replyParts.add(new AgentReplyAssistantPart(Strings.CS.remove(response.getMessageContent(), toolUseContent).trim()));
          // 调用工具
          String toolUseResult = processToolUse(toolUseContent, wrappedPartialHandler);
          messages.add(new AssistantMessage(response.getMessageContent()));
          messages.add(new UserMessage(toolUseResult));
        }
      }
      catch (BssException e) {
        // 确保报错信息中包含步骤名称
        e.setFailMsg(buildFailMsg(context, e.getMessage(), step.getName()));
        throw e;
      }
      catch (Exception e) {
        throw new BssException(buildFailMsg(context, ExpUtil.getMsg(e), step.getName()), e);
      }

      throw new BssException("调用工具次数超出限制: " + toolCallLimit);
    }

    /**
     * 封装片段处理器
     */
    private Consumer<SseEvent> wrapPartialHandler(Consumer<SseEvent> partialHandler, boolean showToolInvocation) {
      if (showToolInvocation) {
        return partialHandler;
      }
      // 不展示工具调用时，忽略工具调用事件
      return event -> {
        if (!(event instanceof ToolCallEvent || event instanceof ToolCallResultEvent)) {
          partialHandler.accept(event);
        }
      };
    }

    /**
     * 处理流式输出片段
     */
    private void handlePartial(ChatCompletionResponse response, Consumer<SseEvent> partialHandler, ToolUseBuffer toolUseBuffer) {
      String content = response.getDeltaContent();
      if (StringUtils.isNotEmpty(content)) {
        String nonTagContent = toolUseBuffer.processChunk(content);
        if (!nonTagContent.isEmpty()) {
          partialHandler.accept(SseEvent.ofText(nonTagContent));
        }
      }
    }

    /**
     * 处理工具调用
     */
    private String processToolUse(String toolUseContent, Consumer<SseEvent> partialHandler) {
      // 找出工具
      Matcher matcher = TOOL_USE_PATTERN.matcher(toolUseContent);
      if (!matcher.find()) {
        logger.error("Invalid tool_use: {}", toolUseContent);
        throw new BssException("大模型返回的 tool_use 不合法");
      }
      String toolName = StringUtils.trimToEmpty(matcher.group(1));
      String toolArguments = extractToolArguments(matcher, toolUseContent);
      // 在日志中记录原始参数，以便排查参数问题
      logger.trace("Received tool_use: {}", toolUseContent);
      if (log != null) {
        log.addLog("收到工具调用，工具: %s, 参数: %s", toolName, toolArguments);
      }
      return processToolUse(toolName, toolArguments, partialHandler);
    }

    /**
     * 提取工具参数
     */
    private String extractToolArguments(Matcher matcher, String toolUseContent) {
      String toolArguments = StringUtils.trimToEmpty(matcher.group(2));
      // 兼容缺少 </arguments> 结束标签的情况
      if (toolArguments.isEmpty() && toolUseContent.contains("<arguments>") && !toolUseContent.contains("</arguments>")) {
        toolArguments = StringUtils.substringBetween(toolUseContent, "<arguments>", "</tool_use>");
        toolArguments = StringUtils.defaultString(toolArguments);
      }
      return toolArguments;
    }

    /**
     * 处理工具调用
     */
    @SuppressFBWarnings("VA_FORMAT_STRING_USES_NEWLINE")
    private String processToolUse(String toolName, String toolArguments, Consumer<SseEvent> partialHandler) {
      SkillToolDTO skillTool = IterableUtils.find(skillTools, t -> toolName.equals(t.getTool().getFunction().getName()));
      if (skillTool == null) {
        throw new BssException("工具不存在: " + toolName);
      }
      // 解析参数。大模型有时返回的 JSON 有语法错误，解析失败时自动尝试修复
      JsonNode json = MarkdownHelper.parseJsonAndAutoFix(toolArguments, modelClient);
      Map<String, Object> rawParameters = json == null ? null : JsonUtil.convert(json, new TypeReference<Map<String, Object>>() {
      });
      // 合并自定义参数
      Map<String, Object> parameters = mergeCustomParameters(skillTool, rawParameters);

      String toolDescription = skillTool.getTool().getFunction().getDescription();
      // 向前端通知工具调用
      partialHandler.accept(new ToolCallEvent(null, toolName, toolDescription, parameters));

      // 调用工具
      long toolCallStartTime = System.currentTimeMillis();
      Object result;
      try {
        result = invokeTool(sceneChatParams, skillTool, parameters);
      }
      catch (Exception e) {
        logger.error("Failed to invoke tool: tenantId={}, tool={}, params={}", sceneChatParams.getTenantId(), skillTool, parameters, e);
        long toolCallSpentTime = System.currentTimeMillis() - toolCallStartTime;
        // 向前端通知工具调用失败，避免一直显示调用中
        partialHandler.accept(new ToolCallResultEvent(null, toolName, ExpUtil.getMsg(e), false, toolCallSpentTime));
        throw new BssException("调用技能失败: " + ExpUtil.getMsg(e), e);
      }
      long toolCallSpentTime = System.currentTimeMillis() - toolCallStartTime;
      boolean toolCallSuccess = ToolCallUtil.checkToolCallSuccess(skillTool, result);
      // 向前端通知工具调用结果
      partialHandler.accept(new ToolCallResultEvent(null, toolName, result, toolCallSuccess, toolCallSpentTime));
      // 记录工具调用信息
      Map<String, Object> toolCallInfo = new LinkedHashMap<>();
      toolCallInfo.put("skillType", skillTool.getSkillType());
      toolCallInfo.put("skillId", skillTool.getSkillId());
      toolCallInfo.put("toolName", toolName);
      toolCallInfo.put("input", parameters);
      toolCallInfo.put("output", result);
      invokedTools.add(toolCallInfo);
      // 不展示工具调用时，不记录到回复中，以避免渲染历史消息时显示工具调用过程
      if (!Boolean.FALSE.equals(step.getShowToolInvocation())) {
        replyParts.add(new AgentReplyToolCallPart(toolName, toolDescription, parameters, result, toolCallSuccess, toolCallSpentTime));
      }
      return String.format("<tool_use_result>\n<name>%s</name>\n<result>%s</result>\n</tool_use_result>",
        toolName, result == null ? "" : JsonUtil.toJsonString(result));
    }

    /**
     * 处理调用成功
     */
    public void onFinish(AnswerDTO answer) {
      // 防止异常情况出现报错
      if (messages.size() <= initialMessageCount) {
        logger.error("Invalid agent message count: initialCount={}, messages={}, parts={}", initialMessageCount, messages, replyParts);
        context.setStepOutput(step, ImmutableMap.of("text", "", "invokedTools", invokedTools), log);
        return;
      }
      // 使用大模型的原始回复作为记忆内容（聊天窗口中下一轮会话使用）
      String memoryContent = messages.subList(initialMessageCount, messages.size()).stream()
        .map(m -> {
          // 只记录大模型返回的文本内容，排除 <tool_use> 和 <tool_use_result>, 以减少 token 占用，也避免影响下次 Agent 节点执行时调用工具
          if (m instanceof AssistantMessage assistantMessage) {
            return StringUtils.trim(StringUtils.substringBefore(assistantMessage.getContent(), "<tool_use>"));
          }
          return null;
        })
        .filter(StringUtils::isNotEmpty)
        .collect(Collectors.joining("\n"));
      // 节点出参，保留工具调用信息，无人驾驶项目需要使用
      String text = messages.subList(initialMessageCount, messages.size()).stream()
        .map(m -> m instanceof AssistantMessage ? ((AssistantMessage) m).getContent() : (String) ((UserMessage) m).getContent())
        .collect(Collectors.joining("\n"));
      // 使用结构化的会话片段列表作为消息内容，以方便前端渲染历史消息
      answer.setText(JsonUtil.toJsonString(replyParts));
      answer.setMemoryContent(memoryContent);
      // 更新节点出参
      context.setStepOutput(step, ImmutableMap.of("text", text, "invokedTools", invokedTools), log);
    }
  }

}
