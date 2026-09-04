package com.iwhalecloud.bote.dto.portal;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 简单门户菜单
 *
 * @author chen.linfa
 * @since 2025-09-28
 */
@Getter
@Setter
@ToString
public class SimplePortalMenuDTO {

  @Schema(description = "主键")
  private Long id;

  @Schema(description = "父 ID")
  private Long parentId;

  @Schema(description = "目录/菜单名称")
  private String name;

  @Schema(description = "图标")
  private String icon;

  @Schema(description = "目录类型（导航栏 navigation、左侧栏 side、内部界面 inner）")
  private String dirType;

  @Schema(description = "是否菜单类型的叶子节点")
  private Boolean isMenu;

  @Schema(description = "菜单 ID")
  private Long menuId;

  @Schema(description = "菜单类型")
  private String menuType;

  @Schema(description = "菜单链接")
  private String menuUrl;

  @Schema(description = "排序")
  private Integer sortby;

  @Schema(description = "是否隐藏")
  private Boolean isHidden;

  @Schema(description = "子目录")
  private List<SimplePortalMenuDTO> children;
}
