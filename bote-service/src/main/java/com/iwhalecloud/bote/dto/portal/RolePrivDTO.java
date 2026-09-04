package com.iwhalecloud.bote.dto.portal;

import com.iwhalecloud.bote.entity.portal.RolePrivEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 角色权限 DTO
 *
 * @author auto
 * @since 2024-10-14
 */
@Getter
@Setter
@ToString(callSuper = true)
public class RolePrivDTO extends RolePrivEntity {
  @Schema(description = "修改人名称")
  private String updatorName;
}
