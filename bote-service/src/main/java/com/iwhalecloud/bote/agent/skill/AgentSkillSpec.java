package com.iwhalecloud.bote.agent.skill;

import com.iwhalecloud.bote.dto.skill.SimpleAgentSkillToolDTO;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Agent Skill 规格
 *
 * @author bianjp
 * @since 2026-02-03
 */
@Getter
@Setter
@ToString
public class AgentSkillSpec {
  /** 名称 */
  private String name;
  /** 描述 */
  private String description;
  /** 基础目录 */
  private String baseDir;
  /** SKILL.md 文件内容(不含元数据) */
  private String content;
  /** 工具列表 */
  private List<SimpleAgentSkillToolDTO> tools;

  public AgentSkillSpec(String baseDir, Map<String, String> frontMatter, String content) {
    this.baseDir = baseDir;
    this.name = frontMatter.get("name");
    this.description = frontMatter.get("description");
    this.content = content;
  }
}
