package com.iwhalecloud.bote.entity.agent;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 新增表记录启用技能 Entity
 *
 * @author linmengfan
 * @since 2026-03-05
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "BT_AI_SKILL")
public class AiSkillEntity extends BaseEntity {

  @DiffId
  @Schema(description = "主键")
  private Long id;
  @DiffField(name = "SPACE_ID")
  @Schema(description = "空间id")
  private Long spaceId;
  @DiffField(name = "BOT_ID")
  @Schema(description = "应用id")
  private Long botId;
  @DiffField(name = "SKILL_ID")
  @Schema(description = "技能的id")
  private Long skillId;
}
