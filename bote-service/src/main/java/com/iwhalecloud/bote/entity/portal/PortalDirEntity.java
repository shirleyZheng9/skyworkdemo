package com.iwhalecloud.bote.entity.portal;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 门户目录 Entity
 *
 * @author chen.linfa
 * @since 2025-09-28
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_portal_menu")
public class PortalDirEntity extends BaseEntity {
  @Schema(description = "主键")
  private Long dirId;

  @Schema(description = "父 ID")
  private Long parentId;

  @Schema(description = "目录名称")
  private String dirName;

  @Schema(description = "目录类型（导航栏 navigation、左侧栏 side、内部界面 inner）")
  private String dirType;

  @Schema(description = "目录图标")
  private String dirIcon;

  @Schema(description = "排序")
  private Integer sortby;

  @Schema(description = "关联菜单 ID")
  private Long menuId;

  @Schema(description = "菜单类型（单页 page、iframe、新窗口 open）")
  private String menuType;
}
