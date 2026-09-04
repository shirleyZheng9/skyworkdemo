package com.iwhalecloud.bote.dto.skill;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iwhalecloud.bote.entity.skill.AgentSkillToolEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Agent Skill 关联工具
 *
 * @author chen.linfa
 * @since 2026-04-23
 */
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Agent Skill 关联工具")
public class AgentSkillToolDTO extends AgentSkillToolEntity {
}
