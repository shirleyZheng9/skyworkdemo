package com.iwhalecloud.bote.dto.skill;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iwhalecloud.bote.entity.skill.AgentSkillFileEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Agent Skill 文件 DTO
 *
 * @author qian.sisheng
 * @since 2026-04-28
 */
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Agent Skill 文件")
public class AgentSkillFileDTO extends AgentSkillFileEntity {
 @Schema(description = "是否可在线编辑（由后缀推导，仅查询时填充）")
 private Boolean textEditable;
 @Schema(description = "技能扩展信息（json）")
 private String skillExtJson;
 @Schema(description = "技能模板类型")
 private String skillTemplateType;
}

