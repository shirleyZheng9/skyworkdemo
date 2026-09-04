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
 * Agent Skill 实体对象
 *
 * @author bianjp
 * @since 2026-02-03
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_agent_skill")
@Schema(description = "Agent Skill 实体对象")
public class AgentSkillEntity extends BaseEntity {
  @DiffId
  @Schema(description = "技能 ID")
  private Long skillId;
  @DiffField
  @Schema(description = "租户 ID")
  private Long tenantId;
  @DiffField
  @Schema(description = "应用 ID")
  private Long botId;
  @DiffField
  @Schema(description = "技能名称")
  private String skillName;
  @DiffField
  @Schema(description = "技能文件名")
  private String skillFileName;
  @DiffField
  @Schema(description = "技能类型: 在线开发：online、本地上传：local")
  private String skillType;
  @DiffField
  @Schema(description = "技能模板类型: 默认模板：default、本体场景：ontologyScene")
  private String skillTemplateType;
  @DiffField
  @Schema(description = "技能扩展信息")
  private String skillExtJson;
  @DiffField
  @Schema(description = "技能编码")
  private String skillCode;
  @DiffField
  @Schema(description = "目录 ID")
  private Long catalogItemId;
  @DiffField
  @Schema(description = "文件信息 ID")
  private Long fileInfoId;
  @DiffField
  @Schema(description = "来源：空为租户来源，10A为通用智能体来源，此时租户id存储的是空间id")
  private String dataFrom;
  @DiffField
  @Schema(description = "关联SKILL广场skill_id")
  private Long skillSquareId;
  @DiffField
  @Schema(description = "已安装技能版本（与广场 skill 的 version 对齐）")
  private String skillVersion;
}
