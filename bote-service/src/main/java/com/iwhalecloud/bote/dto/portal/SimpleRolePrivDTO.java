package com.iwhalecloud.bote.dto.portal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 简单角色权限 DTO
 *
 * @author auto
 * @since 2024-10-14
 */
@Getter
@Setter
@ToString
public class SimpleRolePrivDTO {
  @Schema(description = "角色编码")
  private String roleCode;
  @Schema(description = "权限 ID")
  private Long privId;
  @Schema(description = "权限编码")
  private String privCode;
  @Schema(description = "权限名称")
  private String privName;
  @Schema(description = "菜单地址")
  private String privUrl;
  @Schema(description = "权限类型")
  private String privType;
}
