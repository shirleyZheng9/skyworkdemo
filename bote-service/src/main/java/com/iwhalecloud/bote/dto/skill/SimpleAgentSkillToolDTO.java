package com.iwhalecloud.bote.dto.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Agent Skill Tool 简单信息
 *
 * @author chen.linfa
 * @since 2026-04-23
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // GeneralAgent 中对技能的工具列表合并、去重使用
public class SimpleAgentSkillToolDTO {
  @Schema(description = "技能 ID")
  private Long skillId;
  @Schema(description = "工具 ID")
  @EqualsAndHashCode.Include
  private Long toolId;
  @Schema(description = "工具名称")
  private String toolName;
  @Schema(description = "工具编码")
  private String toolCode;
  @Schema(description = "工具类型")
  @EqualsAndHashCode.Include
  private String toolType;
}
