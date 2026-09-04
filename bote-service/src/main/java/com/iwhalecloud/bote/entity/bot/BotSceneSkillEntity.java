package com.iwhalecloud.bote.entity.bot;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 机器人场景技能
 *
 * @author chen.linfa
 * @since 2024-09-11
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_bot_scene_skill")
public class BotSceneSkillEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键")
  private Long sceneSkillId;
  @Schema(description = "机器人 ID")
  @DiffField(name = "SCENE_ID", parent = true)
  private Long sceneId;
  @Schema(description = "技能 ID")
  @DiffField(name = "SKILL_ID")
  private Long skillId;
  @Schema(description = "技能名称")
  @DiffField(name = "SKILL_NAME")
  private String skillName;
  @Schema(description = "技能编码")
  @DiffField(name = "SKILL_CODE")
  private String skillCode;
  @Schema(description = "技能类型")
  @DiffField(name = "SKILL_TYPE")
  private String skillType;
  @Schema(description = "技能详情 JSON")
  @DiffField(name = "SKILL_JSON")
  private String skillJson;
  @Schema(description = "租户 ID")
  @DiffField(name = "TENANT_ID")
  private Long tenantId;
  @Schema(description = "技能配置(JSON)")
  @DiffField(name = "SKILL_CONFIG_JSON")
  private String skillConfigJson;
  @Schema(description = "外部技能 ID")
  @DiffField(name = "EXT_SKILL_ID")
  private String extSkillId;
}
