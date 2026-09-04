package com.iwhalecloud.bote.agent.tools;

import com.iwhalecloud.bote.agent.agents.ReActAgent;
import com.iwhalecloud.bote.agent.annotation.Tool;
import com.iwhalecloud.bote.agent.annotation.ToolParam;
import com.iwhalecloud.bote.agent.memory.impl.SessionBasedMemory;
import com.iwhalecloud.bote.agent.skill.PresetAgentSkillsLoader;
import com.iwhalecloud.bote.agent.tool.callback.ToolCallback;
import com.iwhalecloud.bote.agent.tool.context.ToolContext;
import com.iwhalecloud.bote.agent.tool.support.ToolExecutionResult;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.enums.SandboxMode;
import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.SceneContextUtil;
import com.iwhalecloud.bote.dto.bot.SimpleBotSceneDTO;
import com.iwhalecloud.bote.dto.chat.ChatRequestMessageDTO;
import com.iwhalecloud.bote.dto.chat.ChatTraceLogDTO;
import com.iwhalecloud.bote.dto.chat.ChatTraceLogDTO.ChatTraceLogBuilder;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineResponse;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.consts.MessageRole;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.mapper.chat.SessionMsgMapper;
import com.iwhalecloud.bote.service.chat.IChatSessionService;
import com.iwhalecloud.bote.service.chat.context.ChatContext;
import com.iwhalecloud.bote.service.chat.helper.CallSceneHelper;
import com.iwhalecloud.bote.service.orchestration.reply.handlers.NonStreamChatReplyHandler;
import com.iwhalecloud.bote.service.scene.ISceneChatService;
import com.iwhalecloud.bss.litchi.util.DateUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 子智能体调度工具
 *
 * @author bianjp
 * @since 2026-04-17
 */
public final class AgentTools {
  private static final Logger logger = LoggerFactory.getLogger(AgentTools.class);
  /** 调用子智能体工具名称 */
  public static final String TOOL_NAME_AGENT = "agent";
  /** 向子智能体发送消息工具名称 */
  public static final String TOOL_NAME_SEND_MESSAGE_TO_AGENT = "send_message_to_agent";
  /** 工具名称列表 */
  public static final List<String> TOOL_NAMES = List.of(TOOL_NAME_AGENT, TOOL_NAME_SEND_MESSAGE_TO_AGENT);
  /** 不允许在子智能体中调用的工具列表 */
  private static final List<String> DISALLOWED_TOOLS = List.of(TOOL_NAME_AGENT, TOOL_NAME_SEND_MESSAGE_TO_AGENT);
  /** 不允许在其它子智能体中使用的工具集合 */
  public static final Set<String> SUBAGENT_DISALLOWED_TOOLS = Set.of(
    TOOL_NAME_AGENT,
    TOOL_NAME_SEND_MESSAGE_TO_AGENT,
    "cron_list", "cron_get", "cron_create", "cron_delete", "cron_disable", "cron_enable", "cron_trigger",
    "search_skill", "install_skill"
  );
  /** 不允许在其它子智能体中使用的技能集合 */
  public static final Set<String> SUBAGENT_DISALLOWED_SKILLS = Set.of(
    PresetAgentSkillsLoader.SKILL_NAME_CRON,
    PresetAgentSkillsLoader.SKILL_NAME_FIND_PLATFORM_SKILLS,
    PresetAgentSkillsLoader.SKILL_NAME_SKILL_CREATOR
  );
  private static final SessionMsgMapper sessionMsgMapper = SpringUtil.getBean(SessionMsgMapper.class);
  private static final IChatSessionService chatSessionService = SpringUtil.getBean(IChatSessionService.class);
  private static final ISceneChatService sceneChatService = SpringUtil.getBean(ISceneChatService.class);
  private static final CallSceneHelper callSceneHelper = SpringUtil.getBean(CallSceneHelper.class);

  private AgentTools() {
  }

  /**
   * 构造子智能体的系统提示词
   *
   * @param subagents 子智能体列表
   */
  public static String buildSubagentPrompt(List<SimpleBotSceneDTO> subagents) {
    StringBuilder sb = new StringBuilder();
    sb.append("# 子智能体\n");
    sb.append("子智能体可以帮助完成特定领域的任务。处理用户请求前，先检查以下子智能体是否可以帮助完整任务，如果可以则使用 `").append(TOOL_NAME_AGENT).append("` 工具调用子智能体:");
    for (SimpleBotSceneDTO agent : subagents) {
      sb.append("- ").append(agent.getSceneName()).append(": ").append(agent.getSceneDesc().trim().replace("\n", " ")).append("\n");
    }
    return sb.toString().trim();
  }

  @Tool(name = TOOL_NAME_AGENT,
    description = """
      Launch a new agent to handle complex, multi-step tasks autonomously.

      Available agents:
      - worker: General-purpose agent for researching complex questions and executing multi-step tasks.
      - Other available agents are listed in system prompt, if any

      ## When not to use

      If the target is already known, use direct tools (for example: a file-read tool for a known path, a search tool for a specific symbol or string).

      ## Usage notes

      - Always include a short description summarizing what the agent will do
      - When the agent is done, it will return a single message back to you. The result returned by the agent is not visible to the user. To show the user the result, you should send a text message back to the user with a concise summary of the result.
      - Trust but verify: an agent's summary describes what it intended to do, not necessarily what it did. When an agent writes or edits code, check the actual changes before reporting the work as done.
      - To continue a previously started agent, use `send_message_to_agent` with the returned agentId. A new agent call starts a fresh agent with no memory of prior runs, so the prompt must be self-contained.
      - Clearly tell the agent whether you expect it to write code or just to do research (search, file reads, web fetches, etc.), since it is not aware of the user's intent

      ## Writing the prompt

      Brief the agent like a smart colleague who just walked into the room — it hasn't seen this conversation, doesn't know what you've tried, doesn't understand why this task matters.
      - Explain what you're trying to accomplish and why.
      - Describe what you've already learned or ruled out.
      - Give enough context about the surrounding problem that the agent can make judgment calls rather than just following a narrow instruction.
      - If you need a short response, say so ("report in under 200 words").
      - Lookups: hand over the exact command. Investigations: hand over the question — prescribed steps become dead weight when the premise is wrong.

      Terse command-style prompts produce shallow, generic work.

      **Never delegate understanding.** Don't write "based on your findings, fix the bug" or "based on the research, implement it." Those phrases push synthesis onto the agent instead of doing it yourself. Write prompts that prove you understood: include file paths, line numbers, what specifically to change.
      """)
  public static ToolExecutionResult agent(@ToolParam(description = "A short (3-5 word) description of the task") String description,
                                          @ToolParam(description = "The task for the agent to perform") String prompt,
                                          @ToolParam(description = "The subagent to start. Default to worker") @Nullable String subagent,
                                          ToolContext toolContext) {
    Assert.hasText(description, "Error: description is required");
    Assert.hasText(prompt, "Error: prompt is required");

    String agentId = "sub:" + SceneContextUtil.newContextId();
    try {
      // worker 智能体
      if (StringUtils.isEmpty(subagent) || "worker".equals(subagent)) {
        String result = executeWorkerAgent(toolContext, agentId, prompt);
        return ToolExecutionResult.success("agentId: " + agentId + "\nOutput: " + result);
      }
      // 检查子智能体类型是否存在
      List<SimpleBotSceneDTO> subagents = toolContext.subagents();
      if (CollectionUtils.isEmpty(subagents)) {
        return ToolExecutionResult.fail("Error: unknown subagent type: " + subagent + ", allowed values: worker");
      }
      SimpleBotSceneDTO agent = IterableUtils.find(subagents, a -> subagent.equals(a.getSceneName()));
      if (agent == null) {
        return ToolExecutionResult.fail("Error: unknown subagent type: " + subagent + ", allowed values: worker, " + subagents.stream().map(SimpleBotSceneDTO::getSceneName).collect(Collectors.joining(", ")));
      }
      return executeSubAgent(toolContext, agent.getSceneId(), agentId, prompt);
    }
    catch (Exception e) {
      logger.error("Failed to launch subagent: tenantId={}, agentType={}", toolContext.tenantId(), subagent, e);
      // 只在成功记录消息后才返回 agentId
      if (sessionMsgMapper.existsMessage(toolContext.sessionId(), agentId)) {
        return ToolExecutionResult.fail("agentId: " + agentId + "\nError: " + ExpUtil.getMsg(e));
      }
      return ToolExecutionResult.fail("Error: " + ExpUtil.getMsg(e));
    }
  }

  @Tool(
    name = TOOL_NAME_SEND_MESSAGE_TO_AGENT,
    description = """
      Send message to an existing agent.
      Use this tool only to continue the same task with the same agent context, after `agent` has created and returned an agentId.
      """
  )
  public static ToolExecutionResult sendMessageToAgent(@ToolParam(description = "agentId returned by a previous `agent` call in the current session") String agentId,
                                                       @ToolParam(description = "New message for the agent") String message,
                                                       ToolContext toolContext) {
    Assert.hasText(agentId, "Error: agentId is required");
    Assert.hasText(message, "Error: message is required");
    Assert.isTrue(sessionMsgMapper.existsMessage(toolContext.sessionId(), agentId), () -> "Error: agentId not found: " + agentId);

    try {
      Long sceneId = sessionMsgMapper.selectSceneIdByContextId(toolContext.sessionId(), agentId);
      // 为空表示 worker 智能体
      if (sceneId == null) {
        String reply = executeWorkerAgent(toolContext, agentId, message);
        return ToolExecutionResult.success(StringUtils.defaultString(reply));
      }
      return executeSubAgent(toolContext, sceneId, agentId, message);
    }
    catch (Exception e) {
      logger.error("Failed to send message to subagent: tenantId={}, agentId={}", toolContext.tenantId(), agentId, e);
      return ToolExecutionResult.fail("Error: " + ExpUtil.getMsg(e));
    }
  }

  /**
   * 调用子智能体
   */
  private static ToolExecutionResult executeSubAgent(ToolContext toolContext, Long sceneId, String agentId, String prompt) {
    ChatContext chatContext = toolContext.chatContext();
    Assert.notNull(chatContext, "Error: 调用环境异常，会话上下文不能为空");

    SceneChatParamsDTO sceneChatParams = callSceneHelper.buildSceneChatParams(chatContext);
    sceneChatParams.setSceneId(sceneId);
    sceneChatParams.setTransactionId(IDUtils.nextId());
    sceneChatParams.setContextId(agentId);
    sceneChatParams.setMessageContent(prompt);
    sceneChatParams.setReplyHandler(new NonStreamChatReplyHandler(chatContext.getClientId()));

    OrchestrationEngineResponse response = sceneChatService.run(sceneChatParams);
    boolean success = Boolean.TRUE.equals(response.getSuccess());
    if (!success) {
      return ToolExecutionResult.fail(response.getFailMsg());
    }

    // 提取回复
    Triple<String, ChatMessageType, Object> triple = response.extractReply();
    String replyText = triple.getLeft();
    ChatMessageType msgType = triple.getMiddle();
    Object msgContent = triple.getRight();

    return ToolExecutionResult.builder()
      .success(true)
      .result("agentId: " + agentId + "\nOutput: " + StringUtils.defaultString(replyText))
      // 返回页面时直接返回
      .returnDirect(msgType == ChatMessageType.PAGE || msgType == ChatMessageType.A2UI)
      .msgType(msgType)
      .msgContent(msgContent)
      .build();
  }

  /**
   * 调用 worker 子智能体
   */
  private static String executeWorkerAgent(ToolContext toolContext, String agentId, String prompt) {
    ChatRequestMessageDTO requestMessage = new ChatRequestMessageDTO();
    requestMessage.setType(ChatMessageType.INPUT.getCode());
    requestMessage.setRole(MessageRole.USER.getCode());
    requestMessage.setContent(prompt);

    // 初始化短期记忆
    LlmClient modelClient = toolContext.modelClient();
    Assert.notNull(modelClient, "Error: Invalid execution context");
    SessionBasedMemory memory = SessionBasedMemory.builder()
      .requestMessage(requestMessage)
      .modelClient(modelClient)
      .sessionId(toolContext.sessionId())
      .transactionId(IDUtils.nextId())
      .botId(toolContext.botId())
      .userId(toolContext.userId())
      .contextId(agentId)
      .build();
    memory.init();

    // 继承主智能体的工具，排除调用子智能体的工具以避免嵌套调用
    List<ToolCallback> inheritedTools = ListUtils.emptyIfNull(toolContext.toolCallbacks()).stream()
      .filter(t -> !DISALLOWED_TOOLS.contains(t.getToolName()))
      .toList();
    ToolContext workerToolContext = toolContext.toBuilder()
      .toolCallbacks(inheritedTools)
      .build();

    ChatTraceLogHandler traceLogHandler = new ChatTraceLogHandler();

    // 调用 ReAct 智能体
    ReActAgent workerAgent = ReActAgent.builder()
      .modelClient(modelClient)
      .systemPrompt(buildSystemPrompt(workerToolContext))
      .userPrompt(prompt)
      .toolCallbacks(inheritedTools)
      .toolContext(workerToolContext)
      .memory(memory)
      .logCreator(traceLogHandler::newLog)
      .build();
    try {
      ChatCompletionResponse response = workerAgent.call();
      return response.getMessageContent();
    }
    finally {
      traceLogHandler.saveLogs(memory.getUserMsgId());
    }
  }

  /**
   * 构建系统提示词
   */
  private static String buildSystemPrompt(ToolContext toolContext) {
    StringBuilder sb = new StringBuilder();
    sb.append("- 当前的 session_id: ").append(toolContext.sessionId()).append("\n");
    sb.append("- 当前的 user_id: ").append(toolContext.userId()).append("\n");
    sb.append("- 当前日期: ").append(DateUtil.formatDate()).append("\n");
    sb.append("- 工作目录: ").append(toolContext.workDir()).append("\n");
    if (toolContext.sandboxMode() == SandboxMode.CLIENT && StringUtils.isNotEmpty(toolContext.clientOs())) {
      sb.append("- 当前操作系统: ").append(toolContext.clientOs()).append("\n");
    }
    sb.append("- 重要提示:\n");
    sb.append("  * 你的输出不会直接展示给最终用户，而是返回给主智能体作为中间结果；请保持输出简洁、结论优先，避免寒暄和面向用户的冗长表达。\n");
    if (toolContext.sandboxMode() == SandboxMode.CLIENT) {
      sb.append("  * 需要返回文件或图片给用户时，优先使用 file:// 链接形式以方便用户点击打开，比如 [report.pdf](file:///app/workspace/report.pdf)\n");
    }
    else {
      sb.append("  * 需要返回文件或图片给用户时，不能返回文件路径，必须使用 get_download_url 获取文件/图片的下载链接。对于图片应优先使用 markdown 的图片语法。\n");
    }

    // 添加技能提示
    if (CollectionUtils.isNotEmpty(toolContext.skills())) {
      sb.append("\n").append(SkillsTool.buildSystemPrompt(toolContext.skills()));
    }

    return sb.toString();
  }

  /**
   * 会话日志处理器
   */
  private static final class ChatTraceLogHandler {
    private final List<ChatTraceLogBuilder> traceLogBuilders = new ArrayList<>();

    /**
     * 构造新日志
     */
    public ChatTraceLogBuilder newLog(String stepName) {
      ChatTraceLogBuilder builder = ChatTraceLogDTO.builder(stepName);
      traceLogBuilders.add(builder);
      return builder;
    }

    /**
     * 保存日志
     */
    public void saveLogs(Long userMsgId) {
      if (traceLogBuilders.isEmpty()) {
        return;
      }
      List<ChatTraceLogDTO> traceLogs = traceLogBuilders.stream().map(ChatTraceLogBuilder::build).collect(Collectors.toList());
      ThreadPools.getMsgSaver().submit(() -> {
        try {
          chatSessionService.saveChatTraceLogs(userMsgId, traceLogs);
        }
        catch (Exception e) {
          logger.error("Failed to save chat trace logs: userMsgId={}", userMsgId, e);
        }
      });
    }
  }
}
