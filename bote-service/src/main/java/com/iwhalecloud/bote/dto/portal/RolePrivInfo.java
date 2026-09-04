package com.iwhalecloud.bote.dto.portal;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 角色权限信息
 *
 * @author auto
 * @since 2024-10-14
 */
@Getter
@Setter
@ToString
public class RolePrivInfo {
  @Schema(description = "是否超级管理员")
  private Boolean superAdmin;
  @Schema(description = "用户角色权限")
  private Map<Long, Map<String, List<SimpleRolePrivDTO>>> rolePrivs;
}
