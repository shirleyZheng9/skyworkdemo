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
 * 技能：文本 Entity
 *
 * @author auto
 * @since 2024-09-16
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_skill_text")
public class SkillTextEntity extends BaseEntity {
  @DiffId
  @Schema(description = "文本ID")
  private Long textId;
  @DiffField(name = "TEXT_CONTENT")
  @Schema(description = "文本内容")
  private String textContent;
  @DiffField(name = "TEXT_TITLE")
  @Schema(description = "文本标题")
  private String textTitle;
  @DiffField(name = "CATALOG_ITEM_ID")
  @Schema(description = "目录ID")
  private Long catalogItemId;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户ID")
  private Long tenantId;
}
