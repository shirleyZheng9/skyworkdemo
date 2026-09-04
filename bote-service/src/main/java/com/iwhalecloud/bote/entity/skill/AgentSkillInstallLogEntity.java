package com.iwhalecloud.bote.entity.skill;

import java.time.OffsetDateTime;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 技能安装记录实体
 *
 * @author skill-square
 * @since 2026-03-18
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_agent_skill_install_log")
@Schema(description = "技能安装记录")
public class AgentSkillInstallLogEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键ID")
  private Long id;
  @DiffField
  @Schema(description = "租户ID")
  private Long tenantId;
  @DiffField
  @Schema(description = "用户ID")
  private Long userId;
  @DiffField
  @Schema(description = "智能体ID")
  private Long botId;
  @DiffField
  @Schema(description = "技能ID")
  private Long skillId;
  @DiffField
  @Schema(description = "技能名称")
  private String skillName;
  @DiffField
  @Schema(description = "安装来源: square/dialogue")
  private String installSource;
  @DiffField
  @Schema(description = "安装时间")
  private OffsetDateTime installTime;
}
