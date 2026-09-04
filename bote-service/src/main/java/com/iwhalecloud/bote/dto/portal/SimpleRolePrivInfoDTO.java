package com.iwhalecloud.bote.dto.portal;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 角色的权限信息
 *
 * @author bianjp
 * @since 2025-03-06
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
public class SimpleRolePrivInfoDTO {
  /** 有权限的菜单路径列表 */
  private List<String> menus;
  /** 有权限的组件权限编码列表 */
  private List<String> components;
}
