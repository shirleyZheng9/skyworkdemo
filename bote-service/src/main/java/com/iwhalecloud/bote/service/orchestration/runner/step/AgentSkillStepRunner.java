package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.iwhalecloud.bote.agent.skill.AgentSkillSpec;
import com.iwhalecloud.bote.agent.tool.InternalToolsLoader;
import com.iwhalecloud.bote.agent.tool.callback.SkillToolCallback;
import com.iwhalecloud.bote.agent.tool.callback.ToolCallback;
import com.iwhalecloud.bote.agent.tool.context.ToolContext;
import com.iwhalecloud.bote.agent.tools.SkillsTool;
import com.iwhalecloud.bote.cache.AgentSkillCache;
import com.iwhalecloud.bote.common.consts.DataSyncConsts;
import com.iwhalecloud.bote.common.enums.SandboxMode;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.sse.SseInvoker;
import com.iwhalecloud.bote.common.sse.SseUtil;
import com.iwhalecloud.bote.common.sse.event.SseEvent;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.common.util.ZipUtil;
import com.iwhalecloud.bote.dto.model.SkillToolDTO;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.log.OrchestrationStepRunLog;
import com.iwhalecloud.bote.dto.orchestration.step.AgentSkillStep;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bote.dto.skill.LlmSkillItem;
import com.iwhalecloud.bote.dto.skill.SimpleAgentSkillDTO;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.Tool;
import com.iwhalecloud.bote.llm.client.dto.ToolCall;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.llm.client.dto.message.SystemMessage;
import com.iwhalecloud.bote.llm.client.dto.message.ToolMessage;
import com.iwhalecloud.bote.service.model.helper.SkillToolConverter;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.Builder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.Consumers;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Agent Skill 步骤执行器
 *
 * @author bianjp
 * @since 2026-02-03
 */
@SuppressFBWarnings("VA_FORMAT_STRING_USES_NEWLINE")
@SuppressWarnings("PMD.GuardLogStatement")
public final class AgentSkillStepRunner extends AbstractLlmStepRunner<AgentSkillStep> {
  /** 系统提示词后缀。用于指导大模型怎么返回文件 */
  private static final String SYSTEM_PROMPT_SUFFIX_TEMPLATE = """
    ------
    The current working directory is %s.
    Use this working directory for all files. Only use skill base directories for environment-specific scripts.
    NOTE: Files are in an isolated container that the user cannot access directly. \
    If you need to reference any file in the reply, you MUST first obtain a download link via the `get_download_url` tool, \
    then present the URL using standard Markdown image or link syntax, \
    e.g., `[report.pdf](api/bote/file/file/id/1)` (this is just an example, you MUST obtain the real download link via the `get_download_url` tool).

    Answer the question directly. Do not provide any background information, context, or unsolicited advice. Stick strictly to the facts requested.
    """.trim();

  private final AgentSkillCache agentSkillCache = SpringUtil.getBean(AgentSkillCache.class);

  @Override
  protected void doRun(SceneOrchestrationContext context, AgentSkillStep step) {
    Assert.hasText(step.getModelId(), "模型 ID 不能为空");
    // 获取模型 ID
    Long modelId = getEffectiveModelId(context, step.getModelId());
    LlmClient modelClient = modelClientCache.getLlmClient(context.getTenantId(), modelId);
    Assert.isTrue(modelClient.supportsFunctionCall(), () -> "大模型 " + modelClient.defaultModel() + "[id=" + modelId + "] 不支持函数调用");
    Optional<OrchestrationStepRunLog> logOptional = context.getLastStepRunLogOptional();

    // 构造工具
    BuildToolsResult buildToolsResult = buildTools(context, step, modelId);
    // 临时目录，存放 Agent Skill 文件、大模型生成的文件。没有 Agent Skill 时为 null
    File tmpDir = buildToolsResult.tmpDir();
    List<AgentSkillSpec> skills = buildToolsResult.skills();
    Map<String, ToolCallback> toolCallbacksMap = buildToolsResult.toolCallbacksMap();
    List<Tool> tools = buildToolsResult.tools();

    // 构造消息列表
    List<Message> messages = buildMessages(context, modelId, step, tmpDir, skills);

    logOptional.ifPresent(log -> {
      Map<String, Object> input = new LinkedHashMap<>();
      input.put("modelId", modelId);
      input.put("modelName", modelClient.defaultModel());
      // 后面会修改消息列表，拷贝一份以避免受影响
      input.put("messages", List.copyOf(messages));
      input.put("tools", tools);
      log.setInput(input);
    });

    // 是否使用流式。条件: 对话型 && 启用流式 && 大模型支持流式
    boolean streaming = Boolean.TRUE.equals(context.getDsl().getChatflow()) && Boolean.TRUE.equals(step.getStream()) && modelClient.supportsStreamingFunctionCall();
    // 是否阻塞。条件: 任务型 || 不使用流式。任务型支持以阻塞的方式使用流式（可以避免网关超时）
    boolean blocking = !Boolean.TRUE.equals(context.getDsl().getChatflow()) || !streaming;

    ToolContext toolContext = ToolContext.builder()
      .sessionId(context.getRequest().getConversationId())
      .userId(SessionUtil.getOptionalUserId())
      .tenantId(context.getTenantId())
      .sandboxMode(SandboxMode.LOCAL)
      .workDir(tmpDir != null ? tmpDir.getAbsolutePath() : null)
      .skills(skills)
      .build();
    AgentSkillLlmInvoker handler = AgentSkillLlmInvoker.builder()
      .context(context)
      .step(step)
      .request(ChatCompletionRequest.builder().tenantId(context.getTenantId()).customModelConfig(step.getCustomModelConfig()).tools(tools).build())
      .messages(messages)
      .streaming(streaming)
      .modelClient(modelClient)
      .toolCallbacksMap(toolCallbacksMap)
      .toolContext(toolContext)
      .logOptional(logOptional)
      .build();
    if (blocking) {
      handler.accept(Consumers.nop());
    }
    else {
      Map<String, Object> output = Map.of("text", new SseInvoker(handler));
      context.setStepOutput(step, output);
    }
  }

  /**
   * 构造工具
   *
   * @return (临时目录, 工具回调映射, 工具列表) 没有 Agent Skill 时临时目录为 null
   */
  private BuildToolsResult buildTools(SceneOrchestrationContext context, AgentSkillStep step, Long modelId) {
    List<LlmSkillItem> skillItems = step.getSkills();
    if (skillItems == null || skillItems.isEmpty()) {
      return new BuildToolsResult(null, List.of(), Map.of(), null);
    }

    // 区分 Agent Skill 和其他技能
    List<LlmSkillItem> agentSkills = new ArrayList<>();
    List<LlmSkillItem> otherSkills = new ArrayList<>();
    for (LlmSkillItem skillItem : skillItems) {
      if (DataSyncConsts.SKILL_TYPE_AGENT_SKILL.equals(skillItem.getSkillType())) {
        agentSkills.add(skillItem);
      }
      else {
        otherSkills.add(skillItem);
      }
    }

    List<ToolCallback> toolCallbacks = new ArrayList<>();
    List<AgentSkillSpec> skills = List.of();
    File tmpDir = null;
    // 处理 Agent Skill
    if (!agentSkills.isEmpty()) {
      // 创建临时目录
      tmpDir = createTempDir(context);
      File skillsDir = new File(tmpDir, "skills");
      // 下载并解压缩 Agent Skill
      downloadAgentSkills(skillsDir.toPath(), context.getTenantId(), agentSkills);
      skills = SkillsTool.loadSkills(tmpDir.getAbsolutePath());
      toolCallbacks.addAll(InternalToolsLoader.loadTools(SkillsTool.class));
      toolCallbacks.addAll(InternalToolsLoader.loadToolsForAgentSkill(SandboxMode.LOCAL, null));
    }
    // 处理其它技能
    if (!otherSkills.isEmpty()) {
      boolean isDebug = context.getRequest() != null && Boolean.TRUE.equals(context.getRequest().getDebug());
      List<SkillToolDTO> skillTools = SkillToolConverter.convert(context.getTenantId(), modelId, otherSkills, isDebug);
      // 构造一个场景会话参数对象，用于复用 AbstractStepRunner#runAsTool 方法
      SceneChatParamsDTO sceneChatParams = new SceneChatParamsDTO(context.getRequest());
      skillTools.forEach(t -> toolCallbacks.add(new SkillToolCallback(sceneChatParams, t)));
    }

    Map<String, ToolCallback> toolCallbacksMap = toolCallbacks.stream().collect(Collectors.toMap(
      ToolCallback::getToolName,
      java.util.function.Function.identity(),
      (a, b) -> {
        throw new BssException("工具名称重复: name=" + a.getToolName());
      }));
    List<Tool> tools = toolCallbacks.stream().map(ToolCallback::getTool).toList();
    return new BuildToolsResult(tmpDir, skills, toolCallbacksMap, tools);
  }

  /**
   * 创建临时目录
   */
  private File createTempDir(SceneOrchestrationContext context) {
    try {
      File tmpDir = Files.createTempDirectory("bote-agent-skill-").toFile();
      // 创建 skills 子目录，存放 Agent Skill 的文件
      FileUtils.forceMkdir(new File(tmpDir, "skills"));
      logger.trace("Created temporary directory: {}", tmpDir.getAbsolutePath());
      // 节点可能会异步执行，确保服务执行结束后清除临时目录
      context.addCleaner(() -> FileUtils.deleteQuietly(tmpDir));
      return tmpDir;
    }
    catch (Exception e) {
      throw new BssException("创建临时目录失败: " + ExpUtil.getMsg(e), e);
    }
  }

  /**
   * 下载并解压缩 Agent Skill
   */
  private void downloadAgentSkills(Path skillsDir, Long tenantId, List<LlmSkillItem> agentSkills) {
    for (LlmSkillItem item : agentSkills) {
      SimpleAgentSkillDTO skill = agentSkillCache.get(tenantId, item.getSkillId());
      // 忽略不合法的 Agent Skill
      if (skill == null || (skill.getResource() == null && skill.getFileInfo() == null)) {
        continue;
      }
      try (InputStream inputStream = getAgentSkillFileStream(skill)) {
        ZipUtil.unzip(inputStream, skillsDir.resolve(skill.getSkillId().toString()));
      }
      catch (Exception e) {
        logger.error("Failed to download agent skill: tenantId={}, skillId={}, fileId={}", skill.getTenantId(), skill.getSkillId(), skill.getFileId(), e);
        throw new BssException("下载 Agent Skill 文件失败: skillId=" + item.getSkillId(), e);
      }
    }
  }

  /**
   * 获取 Agent Skill 文件流
   */
  private InputStream getAgentSkillFileStream(SimpleAgentSkillDTO skill) throws IOException {
    if (skill.getResource() != null) {
      return skill.getResource().getInputStream();
    }
    return fileStoreService.downloadFileStreamFromCache(skill.getFileInfo());
  }

  /**
   * 构造消息列表
   */
  private List<Message> buildMessages(SceneOrchestrationContext context, Long modelId, AgentSkillStep step, @Nullable File workingDirectory, List<AgentSkillSpec> skills) {
    List<Message> result = new ArrayList<>();
    // 添加节点配置的消息列表
    addMessages(context.getTenantId(), modelId, step.getMessages(), result);
    // 添加历史消息
    result.addAll(loadHistoryMessages(context, step.getMemory()));
    // 添加用户消息
    result.add(buildUserMessage(context.getTenantId(), modelId, step.getPromptId(), step.getPromptParameters(), step.getUserMessage(), null));

    // 工作目录不为空表示使用了 Agent Skill, 需要调整系统提示词
    if (workingDirectory != null) {
      String systemMessageSuffix = SkillsTool.buildSystemPrompt(skills) + "\n" + SYSTEM_PROMPT_SUFFIX_TEMPLATE.formatted(workingDirectory);
      if (result.getFirst() instanceof SystemMessage systemMessage) {
        String prompt = StringUtils.stripEnd(systemMessage.getContent(), null) + "\n\n" + systemMessageSuffix;
        systemMessage.setContent(prompt);
      }
      else {
        result.addFirst(new SystemMessage(systemMessageSuffix));
      }
    }
    return result;
  }

  /**
   * 大模型调用器
   */
  @Builder
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  @SuppressFBWarnings("VA_FORMAT_STRING_USES_NEWLINE")
  private static final class AgentSkillLlmInvoker implements Consumer<Consumer<SseEvent>> {
    private final SceneOrchestrationContext context;
    private final AgentSkillStep step;
    private final ChatCompletionRequest request;
    private final List<Message> messages;
    private final boolean streaming;
    private final LlmClient modelClient;
    private final Map<String, ToolCallback> toolCallbacksMap;
    private final ToolContext toolContext;
    private final Optional<OrchestrationStepRunLog> logOptional;

    /** 收集思考内容 */
    private final StringBuilder reasoningCollector = new StringBuilder();
    /** 收集正文 */
    private final StringBuilder contentCollector = new StringBuilder();
    /** 流式调用时，是否是本轮响应中第一次收到思考内容 */
    private final AtomicBoolean isFirstReasoning = new AtomicBoolean(true);
    /** 流式调用时，是否是本轮响应中第一次收到正文 */
    private final AtomicBoolean isFirstContent = new AtomicBoolean(true);

    @Override
    public void accept(@Nullable Consumer<SseEvent> sseEventConsumer) {
      Consumer<ChatCompletionResponse> eventHandler = buildEventHandler(sseEventConsumer);
      int toolCallLimit = SystemParameter.LLM_TOOL_CALL_LIMIT.getRequiredIntegerValueFromDb();
      AssistantMessage message = null;
      for (int i = 0; i < toolCallLimit; i++) {
        int count = i + 1;
        logOptional.ifPresent(log -> log.addLog("第 %d 次调用大模型", count));
        request.setMessages(messages);
        ChatCompletionResponse response;
        if (streaming) {
          isFirstReasoning.set(true);
          isFirstContent.set(true);
          response = modelClient.chatCompletionStreamBlockingAndCollect(request, eventHandler, SseUtil.requestListener);
        }
        else {
          response = modelClient.chatCompletion(request, SseUtil.requestListener);
        }
        message = response.getMessage();
        logOptional.ifPresent(log -> log.addLog("大模型响应: %s", JsonUtil.toJsonString(response)));
        // 合并响应
        collectResponse(message);
        // 调用工具
        if (message.hasToolCall()) {
          ToolCall toolCall = message.getToolCall();
          String toolName = toolCall.getFunction().getName();
          ToolCallback toolCallback = toolCallbacksMap.get(toolName);
          Assert.notNull(toolCallback, () -> "工具不存在: name=" + toolName);
          logOptional.ifPresent(log -> log.addLog("调用工具: %s", toolName));
          String result;
          try {
            result = toolCallback.call(toolCall.getFunction().getArguments(), toolContext);
            String finalResult = result;
            logOptional.ifPresent(log -> log.addLog("工具结果: %s", finalResult));
          }
          catch (Exception e) {
            result = ExpUtil.getMsg(e);
            String finalResult = result;
            logOptional.ifPresent(log -> log.addLog("工具执行失败: %s", finalResult));
          }
          messages.add(message);
          messages.add(new ToolMessage(toolCall.getId(), result));
        }
        else {
          break;
        }
      }
      Assert.notNull(message, "LLM_TOOL_CALL_LIMIT 配置错误");
      Assert.isTrue(!message.hasToolCall(), "调用工具次数超出限制");

      // 设置节点出参
      Map<String, Object> output = new LinkedHashMap<>();
      output.put("reasoning", reasoningCollector.isEmpty() ? null : reasoningCollector.toString());
      output.put("text", contentCollector.toString());
      context.setStepOutput(step, output, logOptional.orElse(null));
    }

    /**
     * 构造流式调用的事件处理器
     */
    @Nullable
    private Consumer<ChatCompletionResponse> buildEventHandler(@Nullable Consumer<SseEvent> sseEventConsumer) {
      if (!streaming || sseEventConsumer == null) {
        return null;
      }
      return response -> {
        String deltaReasoningContent = response.getDeltaReasoningContent();
        String deltaContent = response.getDeltaContent();
        if (!deltaReasoningContent.isEmpty()) {
          // 两次大模型响应之间需要添加段落分隔符
          if (isFirstReasoning.compareAndSet(true, false) && !reasoningCollector.isEmpty()) {
            deltaReasoningContent = "\n\n" + StringUtils.stripStart(deltaReasoningContent, null);
          }
          sseEventConsumer.accept(SseEvent.ofReasoning(deltaReasoningContent));
        }
        if (!deltaContent.isEmpty()) {
          // 两次大模型响应之间需要添加段落分隔符
          if (isFirstContent.compareAndSet(true, false) && !contentCollector.isEmpty()) {
            deltaContent = "\n\n" + StringUtils.stripStart(deltaContent, null);
          }
          sseEventConsumer.accept(SseEvent.ofText(deltaContent));
        }
      };
    }

    /**
     * 收集完整响应
     */
    private void collectResponse(AssistantMessage message) {
      String reasoning = StringUtils.trimToNull(message.getReasoningContent());
      if (reasoning != null) {
        // 两次大模型响应之间需要添加段落分隔符
        if (!reasoningCollector.isEmpty()) {
          reasoningCollector.append("\n\n");
        }
        reasoningCollector.append(reasoning);
      }
      String content = StringUtils.trimToNull(message.getContent());
      if (content != null) {
        // 两次大模型响应之间需要添加段落分隔符
        if (!contentCollector.isEmpty()) {
          contentCollector.append("\n\n");
        }
        contentCollector.append(content);
      }
    }
  }

  /**
   * 构造工具结果
   */
  private record BuildToolsResult(@Nullable File tmpDir,
                                  List<AgentSkillSpec> skills,
                                  Map<String, ToolCallback> toolCallbacksMap,
                                  @Nullable List<Tool> tools) {
  }
}
