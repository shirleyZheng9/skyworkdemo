package com.iwhalecloud.bote.agent.tools;

import com.iwhalecloud.bote.agent.annotation.Tool;
import com.iwhalecloud.bote.agent.annotation.ToolParam;
import com.iwhalecloud.bote.agent.memory.service.LongTermMemoryVectorService;
import com.iwhalecloud.bote.agent.tool.context.ToolContext;
import com.iwhalecloud.bote.agent.tool.exception.ToolExecutionException;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.dto.agent.AiWorkspaceDTO;
import com.iwhalecloud.bote.mapper.agent.AiWorkspaceManageMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.iwhalecloud.bss.litchi.util.SpringUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 长期记忆工具集
 *
 * <p>提供供大模型主动调用的记忆管理工具，包括检索、读取、编辑和写入。
 * 记忆文件存储在数据库（bt_ai_workspace 表）中，memory_type 字段标识文件类型：
 * <ul>
 *   <li>{@code LONG_TERM} — MEMORY.md 长期记忆</li>
 *   <li>{@code DAILY} — MEMORY-YYYY-MM-DD.md 每日记忆</li>
 * </ul>
 *
 * <p>当 Elasticsearch 可用时，{@code memory_search} 使用向量 + BM25 混合检索；
 * 否则自动降级为数据库关键字检索。</p>
 *
 * <p>每次写入记忆后，会异步触发 Elasticsearch 向量索引更新，确保下次检索结果的实时性。</p>
 *
 * @author wangtingyun
 * @since 2026-04-18
 */
public final class MemoryTools {
  private static final Logger logger = LoggerFactory.getLogger(MemoryTools.class);

  /** MEMORY.md 文件名（长期记忆） */
  public static final String LONG_TERM_FILE = "MEMORY.md";
  /** 工具名称: 检索记忆 */
  public static final String TOOL_NAME_MEMORY_SEARCH = "memory_search";
  /** 工具名称: 读取记忆 */
  public static final String TOOL_NAME_MEMORY_READ = "memory_read";
  /** 工具名称: 编辑记忆 */
  public static final String TOOL_NAME_MEMORY_EDIT = "memory_edit";
  /** 工具名称: 写入记忆 */
  public static final String TOOL_NAME_MEMORY_WRITE = "memory_write";
  /** 工具名称列表 */
  public static final List<String> TOOL_NAMES = List.of(TOOL_NAME_MEMORY_SEARCH, TOOL_NAME_MEMORY_READ, TOOL_NAME_MEMORY_EDIT, TOOL_NAME_MEMORY_WRITE);

  /** 系统提示词 */
  private static final String MEMORY_SYSTEM_PROMPT = """
    ## 记忆
    每次会话都是全新的。以下文件是你的记忆延续：

    - **每日笔记：** `MEMORY-YYYY-MM-DD.md`（如 `MEMORY-2026-04-18.md`）— 发生事件的原始记录
    - **长期记忆：** `MEMORY.md` — 精心整理的记忆，就像人类的长期记忆
    - **重要：** 读写记忆必须使用专用工具 `memory_read`、`memory_edit`、`memory_write`，不能使用文件系统工具

    用这些文件来记录重要的东西，包括决策、上下文、需要记住的事。除非用户明确要求，否则不要在记忆中记录敏感信息。

    ### MEMORY.md - 你的长期记忆

    - 出于**安全考虑** — 不应泄露给陌生人的个人信息
    - 记录重大事件、想法、决策、观点、经验教训
    - 这是你精选的记忆 — 提炼的精华，不是原始日志

    ### 何时记录

    - 当有人说"记住这个"（或者类似的话） → 更新 `MEMORY-YYYY-MM-DD.md`
    - 当你学到教训 → 更新 AGENTS.md 或 MEMORY.md
    - 当你犯了错 → 记下来，让未来的你避免重蹈覆辙
    - 对话中发现有价值的信息时，**先记下来，再回答问题**：
      * 对话中出现的专项话题内容以及对话中用户做出的重要决策或结论 → 记录到长期记忆 `MEMORY.md` 和每日笔记 `MEMORY-YYYY-MM-DD.md` 相关 section
      * 将对话发现的上下文、技术细节、工作流程 → 记录到长期记忆 `MEMORY.md` 和每日笔记 `MEMORY-YYYY-MM-DD.md` 相关 section
      * 任何你觉得未来会话可能用到的信息 → 立刻记下来

    **关键原则：** 不要总是等用户说"记住这个"。如果信息对未来有价值，主动记录。先记录，再回答 — 这样即使会话中断，信息也不会丢失。

    ### 何时检索
    回答关于过往工作、决策、日期、人员、偏好或待办的问题前：
    1. 先用 `memory_search` 搜索相关记忆（同时检索 MEMORY.md 和所有每日笔记）
    2. 如需阅读完整的记忆文件，用 `memory_read` 读取 `MEMORY.md` 或 `MEMORY-YYYY-MM-DD.md`
    """.trim();

  private static final AiWorkspaceManageMapper workspaceManageMapper = SpringUtil.getBean(AiWorkspaceManageMapper.class);
  private static final LongTermMemoryVectorService vectorService = SpringUtil.getBean(LongTermMemoryVectorService.class);

  private MemoryTools() {
  }

  /**
   * 构造系统提示词
   */
  public static String buildSystemPrompt() {
    return MEMORY_SYSTEM_PROMPT;
  }

  /**
   * 根据关键字检索记忆内容
   *
   * <p>先尝试向量 + BM25 混合检索（ES 可用时），否则降级为数据库关键字搜索。
   * 检索范围包含 MEMORY.md 和所有 MEMORY-YYYY-MM-DD.md。</p>
   */
  @Tool(
    name = "memory_search",
    description = "Search long-term memory (MEMORY.md and daily MEMORY-YYYY-MM-DD.md) by keyword. "
      + "**When to use:** "
      + "1) BEFORE answering questions about past conversations, decisions, user preferences, or historical context. "
      + "2) When the user asks about something you discussed before or references earlier work. "
      + "3) When you need to recall user's stated preferences, requirements, or constraints. "
      + "4) Before starting a complex task to check if there are relevant past experiences or guidelines. "
      + "**Search strategy:** First try to find specific daily MEMORY-YYYY-MM-DD.md files based on the user's query context (infer the date from the question). If unable to determine which day's memory to search or the specific day's memory file has no relevant content, then search the long-term memory file MEMORY.md. "
      + "Returns relevant memory snippets ranked by relevance. "
      + "If no relevant memory is found, the result will indicate that.",
    hideToolCall = true
  )
  public static String memorySearch(
      @ToolParam(description = "File name: 'MEMORY.md' or 'MEMORY-YYYY-MM-DD.md'") String fileName,
      @ToolParam(description = "Content of the user's question") String query,
      ToolContext toolContext) {
    if (StringUtils.isEmpty(query)) {
      return "";
    }

    Long userId = toolContext.userId();
    Long botId = toolContext.sceneId() != null ? toolContext.sceneId() : toolContext.botId();
    Long spaceId = toolContext.spaceId();
    Long tenantId = toolContext.tenantId();

    List<String> results;

    // 优先使用 ES 混合检索
    if (vectorService.isAvailable()) {
      results = vectorService.hybridSearch(tenantId, userId, botId, spaceId, fileName, query, 5);
    }
    else {
      // 降级：从数据库中对所有记忆文件做关键字过滤
      results = fallbackSearch(userId, botId, spaceId, tenantId, query);
    }

    if (CollectionUtils.isEmpty(results)) {
      return "No relevant memory found for: " + query;
    }

    // 格式化返回结果
    StringBuilder sb = new StringBuilder("Found ").append(results.size()).append(" relevant memory snippet(s):\n\n");
    for (String result : results) {
      sb.append("---\n").append(result.trim()).append("\n");
    }
    return sb.toString();
  }

  /**
   * 读取记忆文件的完整内容
   *
   * <p>支持读取 MEMORY.md（长期记忆）以及 MEMORY-YYYY-MM-DD.md（每日记忆）。</p>
   */
  @Tool(
    name = "memory_read",
    description = "Read the full content of a memory file. "
      + "**When to use:** "
      + "1) When you need to review the complete long-term memory or daily memory. "
      + "2) Before editing memory to understand the current content and structure. "
      + "3) When the user asks to see all stored memories. "
      + "Use 'MEMORY.md' for long-term memory, or 'MEMORY-YYYY-MM-DD.md' for daily memory (e.g. 'MEMORY-2026-04-18.md'). "
      + "Returns the complete file content.",
    hideToolCall = true
  )
  public static String memoryRead(
      @ToolParam(description = "File name to read: 'MEMORY.md' or 'MEMORY-YYYY-MM-DD.md'") String fileName,
      ToolContext toolContext) {
    validateMemoryFileName(fileName);

    AiWorkspaceDTO file = loadMemoryFile(fileName, toolContext);
    if (file == null) {
      return "Memory file '" + fileName + "' does not exist yet.";
    }
    return file.getFileContent();
  }

  /**
   * 替换记忆文件中的指定文本或者追加新的记忆内容
   *
   * <p>将 {@code oldString} 替换为 {@code newString}，支持多行匹配。
   * 替换完成后异步更新 Elasticsearch 向量索引。</p>
   */
  @Tool(
    name = "memory_edit",
    description = "Replace the specified text in the memory file or add new memory content. "
      + "**When to use:** "
      + "1) AFTER completing an important task to record what was done and key decisions. "
      + "2) When the user provides new preferences, requirements, or constraints that should be remembered. "
      + "3) When you learn important context about the user's project or goals. "
      + "4) To update outdated information with new findings. "
      + "Supports 'MEMORY.md' and 'MEMORY-YYYY-MM-DD.md'. "
      + "Leave oldString blank to append new content. Provide oldString to replace specific text. "
      + "**Edit strategy:** First modify specific daily MEMORY-YYYY-MM-DD.md files based on the query context (infer the date), and simultaneously update the long-term memory file MEMORY.md as well.",
    hideToolCall = true
  )
  public static String memoryEdit(
      @ToolParam(description = "File name: 'MEMORY.md' or 'MEMORY-YYYY-MM-DD.md'") String fileName,
      @ToolParam(description = "The exact text to replace (left blank if it is additional content)") String oldString,
      @ToolParam(description = "The new text to replace it with or to append") String newString,
      ToolContext toolContext) {
    validateMemoryFileName(fileName);
    Assert.hasLength(newString, "Error: newString is required");
    // 查询记忆文件内容
    AiWorkspaceDTO file = loadMemoryFile(fileName, toolContext);
    String currentContent = file != null ? StringUtils.defaultString(file.getFileContent()) : "";
    // 追加或替换内容
    String newContent = StringUtils.isBlank(oldString) ? currentContent + "\n\n" + newString : replaceContent(currentContent, oldString, newString);
    // 更新记忆内容
    saveMemoryFile(file, fileName, newContent, toolContext);
    // 异步更新向量存储
    asyncUpdateVector(toolContext, fileName, newContent);
    return "Successfully edited " + fileName;
  }

  /**
   * 覆盖记忆文件的全部内容
   *
   * <p>用 {@code content} 完全替换文件内容。写入后异步更新 Elasticsearch 向量索引。</p>
   */
  @Tool(
    name = "memory_write",
    description = "Overwrite the entire content of a memory file. "
      + "**When to use:** "
      + "1) When you need to completely restructure the memory content. "
      + "2) After reading the file with memory_read and making comprehensive changes. "
      + "WARNING: This replaces ALL existing content. Use memory_read first to avoid data loss. "
      + "Supports 'MEMORY.md' and 'MEMORY-YYYY-MM-DD.md'.",
    hideToolCall = true
  )
  public static String memoryWrite(
      @ToolParam(description = "File name: 'MEMORY.md' or 'MEMORY-YYYY-MM-DD.md'") String fileName,
      @ToolParam(description = "The new content to write to the file") String content,
      ToolContext toolContext) {
    validateMemoryFileName(fileName);
    Assert.hasLength(content, "Error: content is required");

    AiWorkspaceDTO file = loadMemoryFile(fileName, toolContext);
    saveMemoryFile(file, fileName, content, toolContext);
    asyncUpdateVector(toolContext, fileName, content);
    return "Successfully wrote " + fileName;
  }

  /**
   * 校验记忆文件名格式
   *
   * <p>合法格式：{@code MEMORY.md} 或 {@code MEMORY-YYYY-MM-DD.md}。</p>
   */
  private static void validateMemoryFileName(String fileName) {
    Assert.hasLength(fileName, "Error: fileName is required");
    boolean isLongTerm = LONG_TERM_FILE.equals(fileName);
    boolean isDaily = fileName.matches("MEMORY-\\d{4}-\\d{2}-\\d{2}\\.md");
    Assert.isTrue(isLongTerm || isDaily,
        "Error: Invalid memory file name. Use 'MEMORY.md' or 'MEMORY-YYYY-MM-DD.md' (e.g. 'MEMORY-2026-04-18.md')");
  }

  /**
   * 根据文件名解析记忆类型
   */
  private static String resolveMemoryType(String fileName) {
    return LONG_TERM_FILE.equals(fileName) ? BaseConsts.MEMORY_TYPE_LONG_TERM : BaseConsts.MEMORY_TYPE_DAILY;
  }

  /**
   * 从数据库加载记忆文件
   */
  @Nullable
  private static AiWorkspaceDTO loadMemoryFile(String fileName, ToolContext toolContext) {
    return workspaceManageMapper.selectMemoryFileByName(toolContext.spaceId(), toolContext.tenantId(),
      toolContext.botId(), toolContext.sceneId(), toolContext.userId(), fileName);
  }

  /**
   * 保存记忆文件（新建或更新）
   */
  private static void saveMemoryFile(@Nullable AiWorkspaceDTO existing, String fileName, String content,
      ToolContext toolContext) {
    try {
      if (existing != null && existing.getId() != null) {
        // 更新现有记录
        workspaceManageMapper.updateFileContentById(existing.getId(), content);
      }
      else {
        // 新建记录
        AiWorkspaceDTO workspace = new AiWorkspaceDTO();
        workspace.setId(Sequences.AI_WORKSPACE_ID.next());
        workspace.setSpaceId(toolContext.spaceId());
        workspace.setTenantId(toolContext.tenantId());
        workspace.setBotId(toolContext.botId());
        workspace.setSceneId(toolContext.sceneId());
        workspace.setFileName(fileName);
        workspace.setFileContent(content);
        workspace.setMemoryType(resolveMemoryType(fileName));
        workspace.setCreatorId(toolContext.userId());
        workspace.setUpdatorId(toolContext.userId());
        workspace.setStatusCd(BaseConsts.STATUS_CD_VALID);
        workspaceManageMapper.insertAiWorkspace(workspace);
      }
    }
    catch (Exception e) {
      throw new ToolExecutionException("Error: 保存记忆文件失败: " + ExpUtil.getMsg(e), e);
    }
  }

  /**
   * 异步更新 Elasticsearch 向量索引
   *
   * <p>使用公共线程池异步执行，不阻塞当前工具调用的响应。</p>
   */
  private static void asyncUpdateVector(ToolContext toolContext, String fileName, String content) {
    if (!vectorService.isAvailable()) {
      return;
    }
    Long tenantId = toolContext.tenantId();
    Long userId = toolContext.userId();
    Long spaceId = toolContext.spaceId();
    // 兼容自助规划智能体场景
    Long botId = toolContext.sceneId() != null ? toolContext.sceneId() : toolContext.botId();
    ThreadPools.getCommon().submit(() -> {
      try {
        vectorService.updateMemoryVector(tenantId, userId, botId, spaceId, fileName, content);
      }
      catch (Exception e) {
        logger.error("异步更新记忆向量索引失败: fileName={}, error={}", fileName, ExpUtil.getMsg(e));
      }
    });
  }

  /**
   * 降级检索：ES 不可用时，从数据库所有记忆文件中关键字过滤
   */
  private static List<String> fallbackSearch(Long userId, Long botId, Long spaceId, Long tenantId, String query) {
    List<String> results = new ArrayList<>();
    String lowerQuery = query.toLowerCase();

    // 搜索长期记忆
    searchInFiles(userId, botId, spaceId, tenantId, BaseConsts.MEMORY_TYPE_LONG_TERM, lowerQuery, results);
    // 搜索每日记忆
    if (results.size() < 5) {
      searchInFiles(userId, botId, spaceId, tenantId, BaseConsts.MEMORY_TYPE_DAILY, lowerQuery, results);
    }

    return results;
  }

  /**
   * 从特定类型的记忆文件中逐段查找包含关键字的内容
   */
  private static void searchInFiles(Long userId, Long botId, Long spaceId, Long tenantId, String memoryType,
      String lowerQuery, List<String> results) {
    List<AiWorkspaceDTO> files = workspaceManageMapper.selectMemoryFileList(spaceId, tenantId, botId, userId, memoryType);
    for (AiWorkspaceDTO file : CollectionUtils.emptyIfNull(files)) {
      if (StringUtils.isBlank(file.getFileContent())) {
        continue;
      }
      // 按段落切分后过滤包含关键字的段落，每个文件最多取 3 个匹配片段
      List<String> matchedChunks = LongTermMemoryVectorService.splitMarkdown(file.getFileContent())
          .stream()
          .filter(chunk -> chunk.toLowerCase().contains(lowerQuery))
          .limit(3)
          .toList();
      results.addAll(matchedChunks);
      if (results.size() >= 5) {
        break;
      }
    }
  }

  /**
   * 换行符不敏感的精确字符串替换（与 PromptFileEditTools 保持一致的逻辑）
   */
  private static String replaceContent(String content, String oldString, String newString) {
    if (content.contains(oldString)) {
      return content.replace(oldString, newString);
    }
    // 换行符不一致时的兼容处理
    String[] oldParts = oldString.split("\\R", -1);
    StringBuilder regexBuilder = new StringBuilder();
    for (int i = 0; i < oldParts.length; i++) {
      if (i > 0) {
        regexBuilder.append("(?:\\r\\n|\\r|\\n)");
      }
      regexBuilder.append(Pattern.quote(oldParts[i]));
    }
    Matcher matcher = Pattern.compile(regexBuilder.toString()).matcher(content);
    return matcher.replaceAll(Matcher.quoteReplacement(newString));
  }
}
