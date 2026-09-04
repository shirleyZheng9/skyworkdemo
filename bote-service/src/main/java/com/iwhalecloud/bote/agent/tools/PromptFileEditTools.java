package com.iwhalecloud.bote.agent.tools;

import com.iwhalecloud.bote.agent.annotation.Tool;
import com.iwhalecloud.bote.agent.annotation.ToolParam;
import com.iwhalecloud.bote.agent.tool.context.ToolContext;
import com.iwhalecloud.bote.agent.tool.exception.ToolExecutionException;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.dto.agent.AiWorkspaceDTO;
import com.iwhalecloud.bote.mapper.agent.AiWorkspaceManageMapper;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.io.ClassPathResource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 提示词文件编辑工具
 *
 * <p>我们使用数据库而非本地文件存储提示词，因此需要使用特殊工具编辑，不能使用文件系统工具</p>
 *
 * @author bianjp
 * @since 2026-03-14
 */
@Component
@RequiredArgsConstructor
public class PromptFileEditTools {
  /** 提示词文件列表 */
  private static final List<String> PROMPT_FILES = List.of("AGENTS.md", "PROFILE.md", "SOUL.md");

  private final AiWorkspaceManageMapper workspaceManageMapper;

  /**
   * 读取提示词文件
   */
  @Tool(name = "read_prompt_file", description = "Read the content of a prompt file. Support only these files: AGENTS.md, PROFILE.md, SOUL.md")
  public String readPromptFile(@ToolParam(description = "The prompt file to read", enumValues = {"AGENTS.md", "PROFILE.md", "SOUL.md"}) String file,
                               ToolContext toolContext) {
    validateFile(file);
    return loadPromptContent(file, toolContext).getRight();
  }

  /**
   * 加载提示词文件内容
   */
  private Pair<Long, String> loadPromptContent(String file, ToolContext toolContext) {
    AiWorkspaceDTO workspace = workspaceManageMapper.selectFileContentByFileName(toolContext.spaceId(), toolContext.botId(), toolContext.userId(), file);
    String content = workspace != null ? workspace.getFileContent() : null;
    // 未配置提示词时取默认值
    if (StringUtils.isEmpty(content)) {
      try (InputStream inputStream = new ClassPathResource("agent/general/prompt/default/" + file).getInputStream()) {
        content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
      }
      catch (Exception e) {
        throw new ToolExecutionException("Error: 读取默认提示词失败: file=" + file + ", error=" + ExpUtil.getMsg(e), e);
      }
    }
    return Pair.of(workspace != null ? workspace.getId() : null, content);
  }

  /**
   * 校验文件参数
   */
  private static void validateFile(String file) {
    Assert.hasLength(file, "Error: file is required");
    Assert.isTrue(PROMPT_FILES.contains(file), () -> "Error: Unknown prompt file, allowed files: " + String.join(", ", PROMPT_FILES));
  }

  /**
   * 编辑提示词文件
   */
  @Tool(name = "edit_prompt_file", description = "Performs exact string replacements in prompt file. Support only these files: AGENTS.md, PROFILE.md, SOUL.md")
  public String editPromptFile(@ToolParam(description = "The prompt file to edit", enumValues = {"AGENTS.md", "PROFILE.md", "SOUL.md"}) String file,
                               @ToolParam(description = "The text to replace") String oldString,
                               @ToolParam(description = "The text to replace it with (must be different from oldString)") String newString,
                               ToolContext toolContext) {
    validateFile(file);
    Assert.hasLength(oldString, "Error: oldString is required");
    Assert.hasLength(newString, "Error: newString is required");
    Assert.isTrue(!oldString.equals(newString), "Error: oldString and newString must be different");
    Pair<Long, String> result = loadPromptContent(file, toolContext);
    String newContent = replacePromptContent(result.getRight(), oldString, newString);
    return savePrompt(result.getLeft(), file, newContent, toolContext);
  }

  /**
   * 写入提示词文件
   */
  @Tool(name = "write_prompt_file", description = "Write the content to a prompt file. Support only these files: AGENTS.md, PROFILE.md, SOUL.md")
  public String writePromptFile(@ToolParam(description = "The prompt file to write", enumValues = {"AGENTS.md", "PROFILE.md", "SOUL.md"}) String file,
                                @ToolParam(description = "The content to write to the file") String content,
                                ToolContext toolContext) {
    validateFile(file);
    Assert.hasLength(content, "Error: content is required");
    Long id = workspaceManageMapper.selectIdByFileName(toolContext.spaceId(), toolContext.botId(), toolContext.userId(), file);
    return savePrompt(id, file, content, toolContext);
  }

  /**
   * Replace oldString with newString in a newline-insensitive way.
   *
   * <p>Java strings may contain different line endings depending on the OS (CRLF/LF/CR). To make
   * multi-line replacements reliable, we treat all newline variants in oldString as equivalent
   * when matching against the stored file content.</p>
   */
  private static String replacePromptContent(String content, String oldString, String newString) {
    // Fast path: exact literal replace works when line endings match.
    if (content.contains(oldString)) {
      return content.replace(oldString, newString);
    }

    // Slow path: line-ending-insensitive matching.
    // Split oldString by any Unicode linebreak sequence, then join with a regex that matches
    // all common newline variants in the target content.
    String[] oldParts = oldString.split("\\R", -1);
    StringBuilder regexBuilder = new StringBuilder();
    for (int i = 0; i < oldParts.length; i++) {
      if (i > 0) {
        regexBuilder.append("(?:\\r\\n|\\r|\\n)");
      }
      regexBuilder.append(Pattern.quote(oldParts[i]));
    }

    Pattern pattern = Pattern.compile(regexBuilder.toString());
    Matcher matcher = pattern.matcher(content);
    return matcher.replaceAll(Matcher.quoteReplacement(newString));
  }

  /**
   * 保存提示词
   */
  private String savePrompt(@Nullable Long id, String file, String content, ToolContext toolContext) {
    if (id == null) {
      AiWorkspaceDTO workspace = new AiWorkspaceDTO();
      workspace.setId(Sequences.AI_WORKSPACE_ID.next());
      workspace.setSpaceId(toolContext.spaceId());
      workspace.setFileName(file);
      workspace.setFileContent(content);
      workspace.setCreatorId(toolContext.userId());
      workspace.setUpdatorId(toolContext.userId());
      workspace.setStatusCd(BaseConsts.STATUS_CD_VALID);
      workspaceManageMapper.insertAiWorkspace(workspace);
    }
    else {
      workspaceManageMapper.updateFileContentById(id, content);
    }
    return "Successfully updated " + file;
  }
}
