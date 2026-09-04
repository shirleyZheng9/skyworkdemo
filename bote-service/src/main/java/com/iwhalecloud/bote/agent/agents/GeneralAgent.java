package com.iwhalecloud.bote.agent.agents;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Suppliers;
import com.iwhalecloud.bote.agent.event.AgentEvent;
import com.iwhalecloud.bote.agent.event.ErrorAgentEvent;
import com.iwhalecloud.bote.agent.event.NonStreamResponseEvent;
import com.iwhalecloud.bote.agent.event.StreamResponseEndEvent;
import com.iwhalecloud.bote.agent.memory.impl.SessionBasedMemory;
import com.iwhalecloud.bote.agent.skill.AgentSkillMarkdownParser;
import com.iwhalecloud.bote.agent.skill.AgentSkillSpec;
import com.iwhalecloud.bote.agent.skill.SyncAgentSkillHelper;
import com.iwhalecloud.bote.agent.tool.InternalToolsLoader;
import com.iwhalecloud.bote.agent.tool.callback.SkillToolCallback;
import com.iwhalecloud.bote.agent.tool.callback.ToolCallback;
import com.iwhalecloud.bote.agent.tool.context.ToolContext;
import com.iwhalecloud.bote.agent.tools.A2uiTools;
import com.iwhalecloud.bote.agent.tools.AgentTools;
import com.iwhalecloud.bote.agent.tools.MemoryTools;
import com.iwhalecloud.bote.agent.tools.SessionStateTools;
import com.iwhalecloud.bote.agent.tools.SkillsTool;
import com.iwhalecloud.bote.agent.tools.TaskTools;
import com.iwhalecloud.bote.cache.AgentSkillCache;
import com.iwhalecloud.bote.cache.GeneraAgentCache;
import com.iwhalecloud.bote.cache.ModelClientCache;
import com.iwhalecloud.bote.cache.SceneCache;
import com.iwhalecloud.bote.common.consts.ModelConsts;
import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.common.enums.SandboxMode;
import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.agent.SimpleAiDefinitionDTO;
import com.iwhalecloud.bote.dto.agent.SimpleAiWorkspaceDTO;
import com.iwhalecloud.bote.dto.bot.SimpleBotSceneDTO;
import com.iwhalecloud.bote.dto.chat.ChatRequestDTO;
import com.iwhalecloud.bote.dto.model.SkillToolDTO;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bote.dto.skill.LlmSkillItem;
import com.iwhalecloud.bote.dto.skill.SimpleAgentSkillDTO;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.dto.ToolCall;
import com.iwhalecloud.bote.mapper.agent.GeneralAgentQueryMapper;
import com.iwhalecloud.bote.sandbox.api.SandboxClient;
import com.iwhalecloud.bote.sandbox.dto.SandboxRunRequest;
import com.iwhalecloud.bote.sandbox.dto.SandboxRunResult;
import com.iwhalecloud.bote.service.agent.IAiWorkspaceManageService;
import com.iwhalecloud.bote.service.chat.context.ChatContext;
import com.iwhalecloud.bote.service.model.helper.SkillToolConverter;
import com.iwhalecloud.bote.service.orchestration.reply.handlers.ChatReplyHandler;
import com.iwhalecloud.bote.service.skill.support.AgentSkillOrchestrationSupport;
import com.iwhalecloud.bote.websocket.context.WebSocketChatContext;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.DateUtil;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 通用智能体
 *
 * @author bianjp
 * @since 2026-03-09
 */
@SuppressWarnings("PMD.GuardLogStatement")
@SuppressFBWarnings("VA_FORMAT_STRING_USES_NEWLINE")
public class GeneralAgent {
  private static final Logger logger = LoggerFactory.getLogger(GeneralAgent.class);
  private static final SceneCache sceneCache = SpringUtil.getBean(SceneCache.class);
  private static final GeneraAgentCache generaAgentCache = SpringUtil.getBean(GeneraAgentCache.class);
  private static final ModelClientCache modelClientCache = SpringUtil.getBean(ModelClientCache.class);
  private static final AgentSkillCache agentSkillCache = SpringUtil.getBean(AgentSkillCache.class);
  private static final GeneralAgentQueryMapper generalAgentQueryMapper = SpringUtil.getBean(GeneralAgentQueryMapper.class);
  private static final boolean sandboxEnabled = SpringUtil.getProperty("bote.sandbox.engine.enabled", Boolean.class, false);
  private static final IAiWorkspaceManageService aiWorkspaceManageService = SpringUtil.getBean(IAiWorkspaceManageService.class);
  private static final AgentSkillOrchestrationSupport agentSkillOrchestrationSupport = SpringUtil.getBean(AgentSkillOrchestrationSupport.class);
  /** 本地工作区目录 */
  private static final File LOCAL_WORKSPACES_DIR = getLocalWorkspacesDir();

  private final ChatContext context;
  @Nullable
  private final WebSocketChatContext webSocketChatContext;
  /** 沙箱模式 */
  private final SandboxMode sandboxMode;
  /** 沙箱客户端 */
  private final SandboxClient sandboxClient;
  /** 工作目录，按会话创建，用于存放临时文件 */
  private String workDir;
  /** 客户端的路径分隔符。仅在 WebSocket 模式下使用，根据客户端的操作系统进行设置 */
  private String clientPathSeparator;

  private final ChatRequestDTO request;
  /** 用户 ID */
  private final Long userId;
  /** 应用对应的空间 ID */
  private final Long tenantId;
  /** 应用 ID */
  private final Long botId;
  /** 用户所在的空间 ID */
  private final Long spaceId;
  /** 智能体，仅用于 claw 类型的智能体 */
  @Nullable
  private final SimpleBotSceneDTO scene;
  /** 用户的通用智能体配置 */
  private SimpleAiDefinitionDTO generalAgentConfig;
  /** 事件处理器 */
  private final Consumer<AgentEvent> eventHandler;
  private final Supplier<SceneChatParamsDTO> sceneChatParamsSupplier = Suppliers.memoize(this::createSceneChatParams);

  public GeneralAgent(ChatContext context, Consumer<AgentEvent> eventHandler) {
    this(context, null, null, eventHandler);
  }

  public GeneralAgent(ChatContext context, @Nullable WebSocketChatContext webSocketChatContext, Consumer<AgentEvent> eventHandler) {
    this(context, null, webSocketChatContext, eventHandler);
  }

  public GeneralAgent(ChatContext context, @Nullable SimpleBotSceneDTO scene, @Nullable WebSocketChatContext webSocketChatContext, Consumer<AgentEvent> eventHandler) {
    this.context = context;
    this.webSocketChatContext = webSocketChatContext;
    this.request = context.getRequest();
    this.userId = context.getUserId();
    this.tenantId = context.getRequest().getTenantId();
    this.botId = context.getRequest().getBotId();
    this.scene = scene;
    this.spaceId = context.getRequest().getSpaceId();
    // 使用 WebSocket 模式时，工作目录使用用户本地的目录
    if (webSocketChatContext != null) {
      this.sandboxMode = SandboxMode.CLIENT;
    }
    else {
      this.sandboxMode = sandboxEnabled ? SandboxMode.REMOTE : SandboxMode.LOCAL;
    }
    this.sandboxClient = sandboxMode == SandboxMode.REMOTE ? new SandboxClient(userId) : null;
    this.eventHandler = sandboxClient == null ? eventHandler : eventHandler.andThen(e -> {
      // 每次调用大模型结束后都续期一次沙箱
      if (e instanceof NonStreamResponseEvent || e instanceof StreamResponseEndEvent) {
        sandboxClient.renew();
      }
    });
    // 标记通用智能体
    this.context.setIsClaw(true);
  }

  /**
   * 执行
   */
  public void execute() {
    SessionBasedMemory memory = null;
    try {
      init();

      LlmClient modelClient = modelClientCache.getLlmClient(generalAgentConfig.getModelTenantId(), generalAgentConfig.getModelId());
      memory = SessionBasedMemory.builder()
        .modelClient(modelClient)
        .requestMessage(request.getMessage())
        .sessionId(context.getSessionId())
        .contextId(context.getContextId())
        .transactionId(context.getTransactionId())
        .botId(botId)
        .sceneId(scene != null ? scene.getSceneId() : null)
        .userId(userId)
        .spaceId(spaceId)
        .tenantId(tenantId)
        .reminderGenerators(List.of(
          TaskTools.getTaskReminderGenerator(context.getSessionId(), context.getContextId()),
          SessionStateTools.getSessionStateReminderGenerator(context.getSessionId(), context.getContextId())
        ))
        .build();
      memory.init();
      context.setUserMsgId(memory.getUserMsgId());

      // 环境变量，执行 shell 命令时使用
      Map<String, String> envVariables = getEnvVariables(generalAgentConfig, context.getSessionId());

      // 初始化沙箱
      initSandbox(envVariables);

      List<SimpleAgentSkillDTO> enabledAgentSkills = queryEnabledAgentSkills();
      // 是否启用了 A2UI 技能
      boolean a2uiEnabled = enabledAgentSkills.stream().anyMatch(s -> A2uiTools.SKILL_NAME.equals(s.getSkillCode()));
      // A2UI 技能仅用于标记启用 A2UI 功能，不作为实际技能使用
      enabledAgentSkills = a2uiEnabled ? enabledAgentSkills.stream().filter(s -> !A2uiTools.SKILL_NAME.equals(s.getSkillCode())).toList() : enabledAgentSkills;

      // 同步技能
      List<AgentSkillSpec> agentSkills = SyncAgentSkillHelper.syncSkills(userId, tenantId, enabledAgentSkills, sandboxMode, sandboxClient, workDir, clientPathSeparator, webSocketChatContext);
      // 作为子智能体时，排除一些技能
      if (context.isSubagent()) {
        agentSkills = agentSkills.stream().filter(s -> !AgentTools.SUBAGENT_DISALLOWED_SKILLS.contains(s.getName())).toList();
      }

      // 构造工具列表
      List<ToolCallback> toolCallbacks = buildToolCallbacks(a2uiEnabled, agentSkills, memory);

      // 子智能体
      List<SimpleBotSceneDTO> subagents = CollectionUtils.isNotEmpty(generalAgentConfig.getSubagentSceneIds()) ? sceneCache.batchGet(tenantId, generalAgentConfig.getSubagentSceneIds()) : List.of();

      List<String> preferredSkillNames = getPreferredSkillNames(agentSkills, request.getAgentSkillIds());
      String systemPrompt = buildSystemPrompt(agentSkills, preferredSkillNames, subagents, a2uiEnabled);
      String userPrompt = memory.buildUserPrompt(request.getMessage().getContent(), request.getMessage().getFileIds(), request.getMessage().getParams());
      ToolContext toolContext = ToolContext.builder()
        .chatContext(context)
        .sessionId(context.getSessionId())
        .userId(context.getUserId())
        .botId(botId)
        .sceneId(scene != null ? scene.getSceneId() : null)
        .tenantId(tenantId)
        .spaceId(spaceId)
        .webSocketChatContext(webSocketChatContext)
        .sandboxMode(sandboxMode)
        .sandboxClient(sandboxClient)
        .envVariables(envVariables)
        .skills(agentSkills)
        .subagents(subagents)
        .toolCallbacks(toolCallbacks)
        .loadSkillListener(skill -> addNewSkillTools(skill, toolCallbacks))
        .workDir(workDir)
        .modelClient(modelClient)
        .build();

      ReActAgent agent = ReActAgent.builder()
        .modelClient(modelClient)
        .systemPrompt(systemPrompt)
        .userPrompt(userPrompt)
        .toolCallbacks(toolCallbacks)
        .skills(agentSkills)
        .toolContext(toolContext)
        .memory(memory)
        .logCreator(context::newLog)
        .build();
      // 调用子智能体时使用非流式
      if (context.isSubagent()) {
        agent.call(eventHandler);
      }
      else {
        agent.stream(eventHandler);
      }
    }
    catch (Exception e) {
      logger.error("Failed to process chat: request={}", request, e);
      String msg = ExpUtil.getMsg(e);
      eventHandler.accept(new ErrorAgentEvent(msg, e));
      if (memory != null) {
        memory.saveError(msg, ExceptionUtils.getStackTrace(e));
      }
    }
    finally {
      context.complete();
    }
  }

  /**
   * 初始化
   */
  private void init() {
    if (scene != null) {
      this.generalAgentConfig = sceneCache.getAiDefinition(scene, tenantId);
    }
    else {
      this.generalAgentConfig = generaAgentCache.getAiDefinition(tenantId, botId, userId, spaceId);
    }
  }

  /**
   * 构造工具列表
   */
  private List<ToolCallback> buildToolCallbacks(boolean a2uiEnabled, List<AgentSkillSpec> skills, SessionBasedMemory memory) {
    List<ToolCallback> toolCallbacks = InternalToolsLoader.loadToolsForGeneralAgent(sandboxMode, request.getOs());
    // 添加 A2UI 相关工具
    if (a2uiEnabled) {
      toolCallbacks.addAll(InternalToolsLoader.loadTools(A2uiTools.class));
    }

    // 添加 MCP 服务的工具
    addMcpTools(toolCallbacks);
    // 添加知识库的工具
    addKnowledgeTools(toolCallbacks);
    // 添加处理 Agent Skill 的工具
    if (!skills.isEmpty()) {
      toolCallbacks.addAll(InternalToolsLoader.loadTools(SkillsTool.class));
      // 添加技能关联的工具
      addSkillTools(toolCallbacks, skills, memory);
    }

    // WebSocket 模式: 不需要获取文件下载地址(文件就在用户本地); 在用户本机执行浏览器操作，不需要也不支持 VNC
    if (sandboxMode == SandboxMode.CLIENT) {
      toolCallbacks.removeIf(t -> "get_download_url".equals(t.getToolName()) || "get_vnc_url".equals(t.getToolName()));
    }

    // 作为子智能体时，排除一些工具
    if (context.isSubagent()) {
      toolCallbacks.removeIf(t -> AgentTools.SUBAGENT_DISALLOWED_TOOLS.contains(t.getToolName()));
    }
    return toolCallbacks;
  }

  /**
   * 构造系统提示词
   */
  private String buildSystemPrompt(List<AgentSkillSpec> agentSkills, List<String> preferredSkillNames, List<SimpleBotSceneDTO> subagents, boolean a2uiEnabled) {
    String envContext = buildEnvContext(a2uiEnabled, preferredSkillNames);

    StringBuilder sb = new StringBuilder();
    sb.append(envContext).append("\n\n");

    sb.append("""
      ## 智能体人设
      你有 3 个持久化存储的人设文件:
      - AGENTS.md: 角色定义、工作流程、规则与指南。定义你的身份设定、操作规范、工作流程，包括记忆管理策略、安全准则、工具使用说明等
      - PROFILE.md: 用户资料。记录用户的个人资料，让你更了解用户，提供个性化服务。
      - SOUL.md: 核心身份与行为原则。定义你的价值观、风格和行为准则。这是你的"灵魂"，决定你的个性特征和处事方式。

      每一轮对话时都会在系统提示词中发送这 3 个人设文件的最新完整内容。

      对话中发现有价值的信息时，**先记下来，再回答问题**：
      - 用户对你的角色设定 → 更新 `AGENTS.md` 中的「身份」section
      - 用户提到的个人信息（名字、偏好、习惯、工作方式）→ 更新 `PROFILE.md` 的「用户资料」section
      * 用户表达的喜好或不满 → 更新 `PROFILE.md` 的「用户资料」section

      注意: 读写 AGENTS.md, PROFILE.md, SOUL.md 必须使用专用工具: read_prompt_file, edit_prompt_file, write_prompt_file
      """.trim()).append("\n\n");

    sb.append(MemoryTools.buildSystemPrompt()).append("\n\n");

    // 添加 Agent Skill 相关提示词
    if (!agentSkills.isEmpty()) {
      sb.append(SkillsTool.buildSystemPrompt(agentSkills)).append("\n\n");
    }
    // 添加子智能体相关提示词
    if (!subagents.isEmpty()) {
      sb.append(AgentTools.buildSubagentPrompt(subagents)).append("\n\n");
    }

    Map<String, String> prompts = getCustomPrompt();
    String agentsPrompt = loadDefaultPrompt(prompts, "AGENTS.md");
    String soulPrompt = loadDefaultPrompt(prompts, "SOUL.md");
    String profilePrompt = loadDefaultPrompt(prompts, "PROFILE.md");
    sb.append("# AGENTS.md\n\n").append(agentsPrompt).append("\n\n");
    sb.append("# SOUL.md\n\n").append(soulPrompt).append("\n\n");
    sb.append("# PROFILE.md\n\n").append(profilePrompt).append("\n\n");
    return sb.toString();
  }

  /**
   * 构造环境信息
   */
  private String buildEnvContext(boolean a2uiEnabled, List<String> preferredSkillNames) {
    String channel = StringUtils.defaultIfEmpty(request.getChannelType(), "console");
    StringBuilder sb = new StringBuilder();
    sb.append("- 当前的 session_id: ").append(request.getSessionId()).append("\n");
    sb.append("- 当前的 user_id: ").append(userId).append("\n");
    sb.append("- 当前的 channel: ").append(channel).append("\n");
    sb.append("- 当前日期: ").append(DateUtil.formatDate()).append("\n");
    sb.append("- 工作目录: ").append(workDir).append("\n");
    if (sandboxMode == SandboxMode.CLIENT) {
      sb.append("- 当前操作系统: ").append(request.getOs()).append("\n");
    }
    sb.append("- 重要提示:\n");
    sb.append("  * 在执行任何工具调用之前，你必须先简要说明你的操作意图和原因");
    sb.append("  * 工具结果、用户消息可能包含 <system-reminder>, 标签内是来自系统的消息，跟包含它的工具结果、用户消息没有直接关系");
    if (sandboxMode == SandboxMode.CLIENT) {
      sb.append("  * 需要返回文件或图片给用户时，优先使用 file:// 链接形式以方便用户点击打开，比如 [report.pdf](file:///app/workspace/report.pdf)\n");
    }
    else {
      sb.append("  * 需要返回文件或图片给用户时，不能返回文件路径，必须使用 get_download_url 获取文件/图片的下载链接。对于图片应优先使用 markdown 的图片语法。\n");
    }
    sb.append("  * 对源码/仓库类链接（如 github.com、gitee.com、gitlab.com、bitbucket.org）不要使用 web_fetch，应使用代码或仓库相关工具；web_fetch 仅用于文章、文档等网页正文抓取。\n");
    sb.append("  * 所有联网搜索、网页读取、站点交互与浏览器自动化任务，必须优先使用 web-access skill。\n");
    sb.append("    - 轻量检索/读页优先使用 web_search、web_fetch、curl；\n");
    sb.append("    - 动态渲染、登录态、复杂交互、反爬站点或需要真实浏览器环境时，必须在已开启沙箱的会话中调用 browser_use（Playwright）。\n");
    sb.append("    - 触发场景包括：搜索、读页、需登录网站、页面操作、反爬站点、动态渲染、以及任何需要真实浏览器环境的任务。\n");
    if (a2uiEnabled) {
      sb.append("  * 如果需要用户输入结构化数据（比如用户注册信息）、敏感数据（比如账号密码），或者用户明确要求使用 A2UI/AG-UI，则优先使用 %s 生成 A2UI 卡片\n".formatted(A2uiTools.TOOL_NAME));
    }
    if (!preferredSkillNames.isEmpty()) {
      sb.append("- 本轮回话用户指定了优先使用的技能（请优先考虑使用；若任务需要也可使用其他已加载技能）：")
        .append(String.join("、", preferredSkillNames)).append("。\n");
    }
    return sb.toString().trim();
  }

  /**
   * 根据请求指定的 agentSkillIds 从已加载技能中解析出对应的技能名称列表（用于系统提示中的“优先使用”说明）。
   */
  private List<String> getPreferredSkillNames(List<AgentSkillSpec> agentSkills, List<Long> preferredIds) {
    if (CollectionUtils.isEmpty(preferredIds)) {
      return List.of();
    }
    List<String> names = new ArrayList<>();
    for (AgentSkillSpec spec : agentSkills) {
      Long skillId = parseSkillIdFromBaseDir(spec.getBaseDir());
      if (skillId != null && preferredIds.contains(skillId) && StringUtils.isNotEmpty(spec.getName())) {
        names.add(spec.getName());
      }
    }
    return names;
  }

  /**
   * 从 baseDir 中解析 skillId：取路径中最后一个纯数字段。
   * 例如 /app/skills/1349285997062651904/pdf/ → 1349285997062651904
   */
  @Nullable
  private Long parseSkillIdFromBaseDir(String baseDir) {
    if (StringUtils.isEmpty(baseDir)) {
      return null;
    }
    String[] segments = baseDir.replace('\\', '/').replaceAll("/+$", "").split("/");
    for (int i = segments.length - 1; i >= 0; i--) {
      if (StringUtils.isNotEmpty(segments[i]) && StringUtils.isNumeric(segments[i])) {
        return Long.parseLong(segments[i]);
      }
    }
    return null;
  }

  /**
   * 加载工作区自定义提示词（AGENTS.md / SOUL.md / PROFILE.md 等）。
   * 授权类应用（调用方与配置归属用户不一致）下，若调用方无工作区文件，则使用提供方（应用创建者）工作区中的提示词。
   */
  private Map<String, String> getCustomPrompt() {
    List<SimpleAiWorkspaceDTO> list;
    if (scene != null) {
      list = sceneCache.getClawWorkspaces(scene, tenantId);
    }
    else {
      list = generalAgentQueryMapper.selectCustomPromptList(tenantId, botId, userId);
      if (CollectionUtils.isEmpty(list) && !Objects.equals(generalAgentConfig.getUserId(), userId)) {
        list = generalAgentQueryMapper.selectCustomPromptList(tenantId, botId, generalAgentConfig.getUserId());
        if (CollectionUtils.isNotEmpty(list)) {
          // 将提供方自定义的系统提示词，私有化到当前用户（异步落库，不阻塞本轮对话）
          List<SimpleAiWorkspaceDTO> snapshot = new ArrayList<>(list);
          ThreadPools.getCommon().submit(() -> aiWorkspaceManageService.syncAiWorkspace(tenantId, botId, userId, snapshot));
        }
      }
    }
    if (list.isEmpty()) {
      return Map.of();
    }
    return list.stream()
      .filter(w -> StringUtils.isNotEmpty(w.getFileName()))
      .collect(Collectors.toMap(SimpleAiWorkspaceDTO::getFileName, w -> StringUtils.defaultString(w.getFileContent()), (a, b) -> b, LinkedHashMap::new));
  }

  /**
   * 加载默认提示词，优先使用用户自定义的
   */
  private String loadDefaultPrompt(Map<String, String> prompts, String fileName) {
    if (prompts.containsKey(fileName) && StringUtils.isNotEmpty(prompts.get(fileName))) {
      return prompts.get(fileName);
    }
    String file = "agent/general/prompt/default/" + fileName;
    try (InputStream inputStream = new ClassPathResource(file).getInputStream()) {
      return AgentSkillMarkdownParser.parseContent(IOUtils.toString(inputStream, StandardCharsets.UTF_8));
    }
    catch (Exception e) {
      throw new BssException("读取默认提示词失败: file=" + file, e);
    }
  }

  /**
   * 加载用户启用的 MCP 服务
   */
  private void addMcpTools(List<ToolCallback> toolCallbacks) {
    List<Long> platformMcpIds = generalAgentConfig.getPlatformMcpIds();
    List<Long> mcpIds = generalAgentConfig.getMcpIds();
    if (CollectionUtils.isEmpty(platformMcpIds) && CollectionUtils.isEmpty(mcpIds)) {
      return;
    }
    List<LlmSkillItem> skills = new ArrayList<>(CollectionUtils.size(platformMcpIds) + CollectionUtils.size(mcpIds));
    for (Long mcpId : ListUtils.emptyIfNull(platformMcpIds)) {
      LlmSkillItem dto = new LlmSkillItem();
      dto.setSkillType(StepType.MCP);
      dto.setPlatform(true);
      dto.setSkillId(mcpId);
      skills.add(dto);
    }
    for (Long mcpId : ListUtils.emptyIfNull(mcpIds)) {
      LlmSkillItem dto = new LlmSkillItem();
      dto.setSkillType(StepType.MCP);
      dto.setPlatform(false);
      dto.setSkillId(mcpId);
      skills.add(dto);
    }

    List<SkillToolDTO> skillTools = SkillToolConverter.convert(tenantId, generalAgentConfig.getModelId(), skills, false);
    skillTools.forEach(t -> toolCallbacks.add(new SkillToolCallback(sceneChatParamsSupplier.get(), t)));
  }


  /**
   * 加载用户启用的知识库
   */
  private void addKnowledgeTools(List<ToolCallback> toolCallbacks) {
    List<Long> knowledgeIds = generalAgentConfig.getKnowledgeIds();
    if (CollectionUtils.isEmpty(knowledgeIds)) {
      return;
    }
    List<LlmSkillItem> skills = new ArrayList<>(knowledgeIds.size());
    for (Long knowledgeId : knowledgeIds) {
      LlmSkillItem dto = new LlmSkillItem();
      dto.setSkillType(StepType.KNOWLEDGE_RETRIEVAL);
      dto.setPlatform(false);
      dto.setSkillId(knowledgeId);
      skills.add(dto);
    }
    List<SkillToolDTO> skillTools = SkillToolConverter.convert(tenantId, generalAgentConfig.getModelId(), skills, false);
    skillTools.forEach(t -> toolCallbacks.add(new SkillToolCallback(sceneChatParamsSupplier.get(), t)));
  }

  /**
   * 添加技能关联的工具
   */
  private void addSkillTools(List<ToolCallback> toolCallbacks, List<AgentSkillSpec> skills, SessionBasedMemory memory) {
    List<ToolCall> toolCalls = memory.getToolCalls();
    if (toolCalls.isEmpty()) {
      return;
    }
    // 获取已加载技能名称集合
    Set<String> loadedSkillNames = toolCalls.stream()
      .map(toolCall -> {
        if (SkillsTool.TOOL_SKILL.equals(toolCall.getFunction().getName())) {
          Map<String, Object> arguments = JsonUtil.parseJson(toolCall.getFunction().getArguments(), new TypeReference<>() {
          });
          return MapUtils.getString(arguments, "skill");
        }
        return null;
      })
      .filter(Objects::nonNull)
      .collect(Collectors.toSet());
    if (loadedSkillNames.isEmpty()) {
      return;
    }

    // 获取已加载技能关联的工具，去重
    List<LlmSkillItem> skillItems = skills.stream()
      .filter(skill -> CollectionUtils.isNotEmpty(skill.getTools()) && loadedSkillNames.contains(skill.getName()))
      .flatMap(skill -> skill.getTools().stream())
      .distinct()
      .map(t -> {
        LlmSkillItem dto = new LlmSkillItem();
        dto.setSkillType(t.getToolType());
        dto.setPlatform(false);
        dto.setSkillId(t.getToolId());
        return dto;
      }).toList();
    if (skillItems.isEmpty()) {
      return;
    }

    List<SkillToolDTO> skillTools = SkillToolConverter.convert(tenantId, generalAgentConfig.getModelId(), skillItems, false);
    for (SkillToolDTO skillTool : skillTools) {
      if (toolCallbacks.stream().noneMatch(t -> t.getToolName().equals(skillTool.getTool().getFunction().getName()))) {
        toolCallbacks.add(new SkillToolCallback(sceneChatParamsSupplier.get(), skillTool));
      }
    }
  }

  /**
   * 添加新技能的工具
   */
  private void addNewSkillTools(AgentSkillSpec skill, List<ToolCallback> toolCallbacks) {
    if (CollectionUtils.isEmpty(skill.getTools())) {
      return;
    }
    List<LlmSkillItem> skillItems = skill.getTools().stream()
      .distinct()
      .map(t -> {
        LlmSkillItem dto = new LlmSkillItem();
        dto.setSkillType(t.getToolType());
        dto.setPlatform(false);
        dto.setSkillId(t.getToolId());
        return dto;
      })
      .toList();

    List<SkillToolDTO> skillTools = SkillToolConverter.convert(tenantId, ModelConsts.DEFAULT_MODEL, skillItems, false);
    for (SkillToolDTO skillTool : skillTools) {
      if (toolCallbacks.stream().noneMatch(t -> t.getToolName().equals(skillTool.getTool().getFunction().getName()))) {
        toolCallbacks.add(new SkillToolCallback(sceneChatParamsSupplier.get(), skillTool));
      }
    }
  }

  private SceneChatParamsDTO createSceneChatParams() {
    // 虚拟一个场景会话参数，供工具执行器使用
    SceneChatParamsDTO sceneChatParams = new SceneChatParamsDTO();
    sceneChatParams.setDebug(false);
    sceneChatParams.setLogEnabled(false);
    sceneChatParams.setTenantId(tenantId);
    sceneChatParams.setBotId(botId);
    sceneChatParams.setConversationId(context.getSessionId());
    sceneChatParams.setTransactionId(context.getTransactionId());
    sceneChatParams.setReplyHandler(new ChatReplyHandler(context));
    sceneChatParams.setChatContext(context);
    return sceneChatParams;
  }

  /**
   * 初始化沙箱
   */
  private void initSandbox(Map<String, String> envVariables) {
    // 工作目录名称，对于 claw 类型智能体使用 contextId (调试时 sessionId 固定为 1, 运行时同一个会话下可能有多个智能体，无法根据 sessionId 区分), 对于运行态的 BoteClaw 使用 sessionId
    String workDirName = scene != null ? context.getContextId() : context.getSessionId().toString();

    // 远程沙箱(Linux 容器)
    if (sandboxMode == SandboxMode.REMOTE) {
      this.workDir = "/app/workspaces/" + workDirName;
      this.clientPathSeparator = "";
      sandboxClient.init(envVariables);
      SandboxRunResult createDirResult = sandboxClient.execute(SandboxRunRequest.builder().command("mkdir -p " + workDir + "/.skills").build());
      Assert.isTrue(createDirResult.isSuccess(), () -> "创建沙箱目录失败: " + createDirResult.getErrorMessage());
    }
    // 桌面客户端，路径分隔符由客户端操作系统决定
    else if (sandboxMode == SandboxMode.CLIENT) {
      this.workDir = StringUtils.stripEnd(this.request.getWorkDir(), "\\/");
      this.clientPathSeparator = Strings.CI.contains(this.request.getOs(), "windows") ? "\\" : "/";
    }
    // 本地临时目录
    else if (sandboxMode == SandboxMode.LOCAL) {
      this.clientPathSeparator = "";
      // 按会话创建工作目录，且不做清理，确保多轮对话时使用同一个目录
      // 没有很好的清理策略，可能导致目录残留
      try {
        File workDirFile = new File(LOCAL_WORKSPACES_DIR, workDirName);
        FileUtils.forceMkdir(workDirFile);
        this.workDir = workDirFile.getAbsolutePath();
      }
      catch (Exception e) {
        throw new BssException("创建工作目录失败: " + e.getMessage(), e);
      }
    }
  }

  /**
   * 获取环境变量
   */
  private static Map<String, String> getEnvVariables(SimpleAiDefinitionDTO generalAgentConfig, Long sessionId) {
    Map<String, String> envVariables = new LinkedHashMap<>();
    // 添加用户自定义环境变量
    if (MapUtils.isNotEmpty(generalAgentConfig.getEnvVariables())) {
      envVariables.putAll(generalAgentConfig.getEnvVariables());
    }
    // 添加系统预置环境变量
    envVariables.put("BOTE_SESSION_ID", sessionId.toString());
    LoginInfo loginInfo = SessionUtil.getOptionalLoginInfo();
    if (loginInfo != null) {
      envVariables.put("BOTE_USER_ID", loginInfo.getUserId().toString());
      if (loginInfo.getExtUserId() != null) {
        envVariables.put("BOTE_EXT_USER_ID", loginInfo.getExtUserId());
      }
    }
    return envVariables;
  }

  /**
   * 查询用户启用的 Agent Skills
   */
  private List<SimpleAgentSkillDTO> queryEnabledAgentSkills() {
    List<Long> platformAgentSkillIds = generalAgentConfig.getPlatformAgentSkillIds();
    List<Long> agentSkillIds = generalAgentConfig.getAgentSkillIds();

    List<SimpleAgentSkillDTO> agentSkills = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(platformAgentSkillIds)) {
      agentSkills.addAll(agentSkillCache.batchGet(-1L, platformAgentSkillIds));
    }
    // 排除调试指定的技能 ID (可能还未上架）
    Long debugSkillId = request.getDebugSkillId();
    if (CollectionUtils.isNotEmpty(agentSkillIds)) {
      if (debugSkillId != null) {
        agentSkillIds = agentSkillIds.stream().filter(id -> !debugSkillId.equals(id)).toList();
      }
      if (!agentSkillIds.isEmpty()) {
        agentSkills.addAll(agentSkillCache.batchGet(tenantId, agentSkillIds));
      }
    }
    // 添加调试技能
    if (debugSkillId != null) {
      SimpleAgentSkillDTO skill = new SimpleAgentSkillDTO();
      skill.setSkillId(debugSkillId);
      skill.setTenantId(tenantId);
      skill.setDebug(true);
      skill.setTools(agentSkillOrchestrationSupport.getSkillTools(tenantId, debugSkillId));
      agentSkills.add(skill);
    }

    // 忽略不合法的 Agent Skill
    return agentSkills.stream().filter(skill -> {
      if (!Boolean.TRUE.equals(skill.getDebug()) && skill.getMd5sum() == null) {
        logger.warn("Ignore invalid agent skill, file not exist: userId={}, skillId={}, fileId={}", userId, skill.getSkillId(), skill.getFileId());
        return false;
      }
      return true;
    }).toList();
  }

  /**
   * 获取本地工作区目录
   */
  @Nullable
  private static File getLocalWorkspacesDir() {
    // 启用沙箱时不使用本地目录
    if (sandboxEnabled) {
      return null;
    }
    // 支持配置本地工作区目录
    String dir = SpringUtil.getProperty("bote.agent.workspaces.dir");
    if (StringUtils.isNotEmpty(dir)) {
      return new File(dir);
    }
    // 默认使用临时目录
    return new File(FileUtils.getTempDirectory(), "bote-agent-workspaces");
  }
}
