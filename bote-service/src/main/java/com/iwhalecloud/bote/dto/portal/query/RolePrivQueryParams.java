package com.iwhalecloud.bote.dto.portal.query;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 角色权限参数
 *
 * @author auto
 * @since 2024-10-14
 */
@Getter
@Setter
@ToString
@Schema(description = "角色权限参数")
public class RolePrivQueryParams {
  @Schema(description = "角色编码")
  private String roleCode;
  @Schema(description = "权限 ID 集合")
  private List<Long> privIds;
  @Schema(description = "权限类型")
  private String privType;
}
