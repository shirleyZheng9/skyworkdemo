package com.iwhalecloud.bote.dto.portal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iwhalecloud.bote.entity.portal.PortalDirEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 门户目录 DTO
 *
 * @author chen.linfa
 * @since 2025-09-28
 */
@Getter
@Setter
@ToString(callSuper = true)
public class PortalDirDTO extends PortalDirEntity {

  @Schema(description = "菜单链接")
  private String menuUrl;

  @JsonIgnore
  public SimplePortalMenuDTO toProtalMenu() {
    SimplePortalMenuDTO menu = new SimplePortalMenuDTO();
    menu.setId(getDirId());
    menu.setParentId(getParentId());
    menu.setName(getDirName());
    menu.setIcon(getDirIcon());
    menu.setDirType(getDirType());
    menu.setIsMenu(false);
    menu.setMenuId(getMenuId());
    menu.setMenuType(getMenuType());
    menu.setMenuUrl(menuUrl);
    menu.setSortby(getSortby());
    menu.setIsHidden(false);
    return menu;
  }
}
