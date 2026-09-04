package com.iwhalecloud.bote.agent.tools;

import com.iwhalecloud.bote.agent.annotation.Tool;
import com.iwhalecloud.bote.agent.annotation.ToolParam;
import com.iwhalecloud.bote.agent.skill.AgentSkillMarkdownParser;
import com.iwhalecloud.bote.agent.skill.AgentSkillSpec;
import com.iwhalecloud.bote.agent.tool.context.ToolContext;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

/**
 * Agent Skill 工具
 *
 * @author bianjp
 * @since 2026-02-03
 */
@SuppressFBWarnings("VA_FORMAT_STRING_USES_NEWLINE")
public final class SkillsTool {
  /** 工具名称: 技能 */
  public static final String TOOL_SKILL = "skill";
  /** 系统提示词前缀 */
  private static final String SKILLS_SYSTEM_PROMPT_PREFIX = """
    # 技能(Agent Skills)
    技能是指令、脚本、资源的集合，提供特定能力或领域知识。每个技能都有一个 SKILL.md 文件，包含技能的详细说明。

    技能使用要求:
    - 处理用户请求前，先检查可用技能中是否有技能可以帮助更有效地完成任务
    - 使用技能前，必须先调用 skill 工具加载技能的 base directory 和详细说明
    - 使用技能中引用的相对路径时，必须根据技能的 base directory 解析为绝对路径

    可用技能:
    """;

  private SkillsTool() {
  }

  /**
   * 构建系统提示词
   */
  public static String buildSystemPrompt(List<AgentSkillSpec> skills) {
    StringBuilder sb = new StringBuilder(SKILLS_SYSTEM_PROMPT_PREFIX.trim());
    sb.append("\n");
    for (AgentSkillSpec skill : skills) {
      sb.append("- ").append(skill.getName()).append(": ").append(skill.getDescription().trim()).append("\n");
    }
    return sb.toString().trim();
  }

  /**
   * 加载技能
   *
   * <p>递归处理所有 SKILL.md 文件</p>
   */
  public static List<AgentSkillSpec> loadSkills(String rootDirectory) {
    Path rootPath = Paths.get(rootDirectory).toAbsolutePath();
    if (!Files.exists(rootPath)) {
      throw new BssException("目录不存在: " + rootDirectory);
    }
    if (!Files.isDirectory(rootPath)) {
      throw new BssException("路径不是目录: " + rootDirectory);
    }

    List<AgentSkillSpec> skills = new ArrayList<>();
    Collection<File> files = FileUtils.listFiles(rootPath.toFile(), new NameFileFilter("SKILL.md"), TrueFileFilter.TRUE);
    for (File file : files) {
      skills.add(AgentSkillMarkdownParser.parseSkill(file.getParentFile().getAbsolutePath(), file));
    }
    return skills;
  }

  /**
   * 技能工具
   */
  @Tool(
    name = TOOL_SKILL,
    description = """
      加载技能的详细说明(SKILL.md 文件内容)

      输出结果以技能的 base directory 开头，然后是技能的详细说明
      """)
  public static String skill(@ToolParam(description = "The skill name. E.g., pdf or xlsx") String skill, ToolContext toolContext) {
    AgentSkillSpec spec = IterableUtils.find(toolContext.skills(), s -> s.getName().equals(skill));
    if (spec != null) {
      Consumer<AgentSkillSpec> loadSkillListener = toolContext.loadSkillListener();
      if (loadSkillListener != null) {
        loadSkillListener.accept(spec);
      }
      return "Base directory for this skill: %s\n\n%s".formatted(spec.getBaseDir(), spec.getContent());
    }
    return "Skill not found: " + skill;
  }

}
