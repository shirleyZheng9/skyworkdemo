package com.iwhalecloud.bote.entity.portal;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 门户目录下的关联菜单 Entity
 *
 * @author chen.linfa
 * @since 2025-09-28
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_portal_dir_menu")
public class PortalDirMenuEntity extends BaseEntity {
  @Schema(description = "主键")
  private Long relId;

  @Schema(description = "目录 ID")
  private Long dirId;

  @Schema(description = "菜单 ID")
  private Long menuId;

  @Schema(description = "菜单名称")
  private String menuName;

  @Schema(description = "菜单类型（单页 page、iframe、新窗口 open）")
  private String menuType;

  @Schema(description = "菜单用途（内部界面 inner、导航栏 navigation）")
  private String menuUse;

  @Schema(description = "菜单图标")
  private String menuIcon;

  @Schema(description = "是否隐藏")
  private String isHidden;

  @Schema(description = "排序")
  private Integer sortby;
}
