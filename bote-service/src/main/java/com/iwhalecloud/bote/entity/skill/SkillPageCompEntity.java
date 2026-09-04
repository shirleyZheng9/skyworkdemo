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
 * 技能：页面组件 Entity
 *
 * @author lizuyin
 * @since 2026-01-14
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_skill_page_comp")
public class SkillPageCompEntity extends BaseEntity {

  @DiffId
  @Schema(description = "页面组件ID")
  private Long pageCompId;

  @DiffField(name = "PAGE_COMP_NAME")
  @Schema(description = "页面组件名称")
  private String pageCompName;

  @DiffField(name = "COMP_ICON")
  @Schema(description = "组件图标")
  private String compIcon;

  @DiffField(name = "COMP_JSON")
  @Schema(description = "组件JSON配置")
  private String compJson;

  @DiffField(name = "CATALOG_ITEM_ID")
  @Schema(description = "目录ID")
  private Long catalogItemId;

  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户ID")
  private Long tenantId;
}

