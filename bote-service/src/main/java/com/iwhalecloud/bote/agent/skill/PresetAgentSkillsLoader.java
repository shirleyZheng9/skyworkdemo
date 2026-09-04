package com.iwhalecloud.bote.agent.skill;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.lang.Nullable;

/**
 * 从 classpath 加载通用智能体预置 Agent Skills（{@code agent/skills/&lt;id&gt;/SKILL.md}）。
 * <p>
 * 增删预置技能请修改 {@link #PRESET_SKILL_IDS} 并添加对应资源文件。
 */
public final class PresetAgentSkillsLoader {
  private static final Logger logger = LoggerFactory.getLogger(PresetAgentSkillsLoader.class);

  private static final String RESOURCE_PREFIX = "agent/skills/";
  /** 技能名称: 定时任务 */
  public static final String SKILL_NAME_CRON = "cron";
  /** 技能名称: 搜索平台技能 */
  public static final String SKILL_NAME_FIND_PLATFORM_SKILLS = "find-platform-skills";
  /** 技能名称: 联网与浏览器技能 */
  public static final String SKILL_NAME_WEB_ACCESS = "web-access";
  /** 技能名称: 技能创建器 */
  public static final String SKILL_NAME_SKILL_CREATOR = "skill-creator";

  /**
   * 预置技能目录名（与 {@code resources/agent/skills/&lt;id&gt;/SKILL.md} 对应），顺序即加载顺序
   */
  private static final List<String> PRESET_SKILL_IDS = List.of(SKILL_NAME_CRON, SKILL_NAME_FIND_PLATFORM_SKILLS, SKILL_NAME_WEB_ACCESS, SKILL_NAME_SKILL_CREATOR);

  private PresetAgentSkillsLoader() {
  }

  /**
   * 加载全部预置技能；单个失败仅告警，不影响其余项。
   */
  public static List<AgentSkillSpec> loadAll(@Nullable File skillsDir) {
    List<AgentSkillSpec> out = new ArrayList<>(PRESET_SKILL_IDS.size());
    for (String skillId : PRESET_SKILL_IDS) {
      loadOne(skillId, skillsDir).ifPresent(out::add);
    }
    return out;
  }

  private static Optional<AgentSkillSpec> loadOne(String skillId, @Nullable File skillsDir) {
    String resourcePath = RESOURCE_PREFIX + skillId + "/SKILL.md";
    try (InputStream inputStream = new ClassPathResource(resourcePath).getInputStream()) {
      String raw = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
      if (skillsDir != null) {
        File file = skillsDir.toPath().resolve(skillId).resolve("SKILL.md").toFile();
        FileUtils.writeStringToFile(file, raw, StandardCharsets.UTF_8);
      }
      AgentSkillSpec spec = AgentSkillMarkdownParser.parseSkill(skillId, raw);
      if (StringUtils.isNotEmpty(spec.getName()) && StringUtils.isNotEmpty(spec.getContent())) {
        return Optional.of(spec);
      }
      logger.warn("Preset agent skill skipped (empty name or content): skillId={}, path={}", skillId, resourcePath);
    }
    catch (IOException e) {
      logger.warn("Failed to load preset agent skill: skillId={}, path={}", skillId, resourcePath, e);
    }
    catch (Exception e) {
      logger.warn("Failed to parse preset agent skill: skillId={}, path={}", skillId, resourcePath, e);
    }
    return Optional.empty();
  }
}
