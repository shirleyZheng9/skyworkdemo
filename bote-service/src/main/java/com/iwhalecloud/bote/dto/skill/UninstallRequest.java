package com.iwhalecloud.bote.dto.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 技能移除请求
 *
 * @author skill-square
 * @since 2026-03-18
 */
@Getter
@Setter
@ToString
@Schema(description = "技能移除请求")
public class UninstallRequest {

  @NotNull(message = "botId 不能为空")
  @Schema(description = "智能体ID", requiredMode = Schema.RequiredMode.REQUIRED)
  private Long botId;

  @NotNull(message = "agentSkillId 不能为空")
  @Schema(description = "Agent Skill ID（bt_agent_skill.skill_id）", requiredMode = Schema.RequiredMode.REQUIRED)
  private Long agentSkillId;
}
