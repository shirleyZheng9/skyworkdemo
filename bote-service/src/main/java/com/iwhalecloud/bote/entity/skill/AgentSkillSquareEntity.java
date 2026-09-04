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
 * SKILL广场技能元数据实体
 *
 * @author skill-square
 * @since 2026-03-18
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_agent_skill_square")
@Schema(description = "SKILL广场技能元数据")
public class AgentSkillSquareEntity extends BaseEntity {
  @DiffId
  @Schema(description = "技能ID")
  private Long skillId;
  @DiffField
  @Schema(description = "技能编码")
  private String skillCode;
  @DiffField
  @Schema(description = "技能名称")
  private String skillName;
  @DiffField
  @Schema(description = "技能简介")
  private String skillDesc;
  @DiffField
  @Schema(description = "技能类型: platform/community")
  private String skillType;
  @DiffField
  @Schema(description = "标签JSON")
  private String tags;
  @DiffField
  @Schema(description = "版本号")
  private String version;
  @DiffField
  @Schema(description = "安装次数")
  private Integer installCount;
  @DiffField
  @Schema(description = "SKILL.md内容")
  private String skillContent;
  @DiffField
  @Schema(description = "package.zip文件ID")
  private Long fileInfoId;
  @DiffField
  @Schema(description = "来源: square/skills.sh/clawhub")
  private String source;
  @DiffField
  @Schema(description = "上架状态: T/F")
  private String onlineStatus;
  @DiffField
  @Schema(description = "所有者用户ID")
  private Long ownerUserId;
  @DiffField
  @Schema(description = "所有者租户ID")
  private Long ownerTenantId;

  @Schema(description = "所有者用户名")
  private String ownerUserName;
  @Schema(description = "是否安装")
  private Boolean installed;
  @Schema(description = "agent skill 主键")
  private String agentSkillId;
}
