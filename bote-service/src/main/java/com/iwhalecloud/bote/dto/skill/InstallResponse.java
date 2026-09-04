package com.iwhalecloud.bote.dto.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 技能安装响应
 *
 * @author skill-square
 * @since 2026-03-18
 */
@Getter
@Setter
@ToString
@Schema(description = "技能安装响应")
public class InstallResponse {

  @Schema(description = "Agent Skill ID（bt_agent_skill.skill_id）")
  private Long agentSkillId;

  @Schema(description = "技能名称")
  private String skillName;
}
