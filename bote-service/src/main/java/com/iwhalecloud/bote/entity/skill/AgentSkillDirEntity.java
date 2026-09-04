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
 * Agent Skill 目录
 *
 * @author qian.sisheng
 * @since 2026-04-28
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_agent_skill_dir")
@Schema(description = "Agent Skill 目录")
public class AgentSkillDirEntity extends BaseEntity {
  @DiffId
  @Schema(description = "目录主键")
  private Long dirId;
  @DiffField
  @Schema(description = "父目录 ID，一级目录固定 -1")
  private Long parentDirId;
  @DiffField
  @Schema(description = "技能 ID")
  private Long skillId;
  @DiffField
  @Schema(description = "租户 ID")
  private Long tenantId;
  @DiffField
  @Schema(description = "目录名称")
  private String dirName;
}

