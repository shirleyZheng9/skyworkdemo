package com.iwhalecloud.bote.entity.skill;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
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
@DiffNode(name = "bt_agent_skill_tool")
@Schema(description = "Agent Skill 关联工具")
public class AgentSkillToolEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键")
  private Long id;

  @DiffField
  @Schema(description = "租户 ID")
  private Long tenantId;

  @DiffField
  @Schema(description = "技能 ID")
  private Long skillId;

  @DiffField
  @Schema(description = "工具 ID")
  private Long toolId;

  @DiffField
  @Schema(description = "工具名称")
  private String toolName;

  @DiffField
  @Schema(description = "工具编码")
  private String toolCode;

  @DiffField
  @Schema(description = "工具类型")
  private String toolType;
}
