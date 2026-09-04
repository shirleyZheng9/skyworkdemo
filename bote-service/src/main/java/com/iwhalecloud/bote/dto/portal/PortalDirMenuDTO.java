package com.iwhalecloud.bote.dto.portal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.entity.portal.PortalDirMenuEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 门户目录下的关联菜单 DTO
 *
 * @author chen.linfa
 * @since 2025-09-28
 */
@Getter
@Setter
@ToString(callSuper = true)
public class PortalDirMenuDTO extends PortalDirMenuEntity {

  @Schema(description = "菜单链接")
  private String menuUrl;

  @JsonIgnore
  public SimplePortalMenuDTO toProtalMenu() {
    SimplePortalMenuDTO menu = new SimplePortalMenuDTO();
    menu.setId(getRelId());
    menu.setParentId(getDirId());
    menu.setName(getMenuName());
    menu.setIcon(getMenuIcon());
    menu.setIsMenu(true);
    menu.setMenuId(getMenuId());
    menu.setMenuType(getMenuType());
    menu.setMenuUrl(menuUrl);
    menu.setSortby(getSortby());
    menu.setIsHidden(BaseConsts.TRUE.equals(getIsHidden()));
    return menu;
  }
}
