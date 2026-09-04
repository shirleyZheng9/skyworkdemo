package com.iwhalecloud.bote.dto.skill;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iwhalecloud.bote.entity.skill.AgentSkillDirEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Agent Skill 目录 DTO
 *
 * @author qian.sisheng
 * @since 2026-04-28
 */
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Agent Skill 目录")
public class AgentSkillDirDTO extends AgentSkillDirEntity {
}

