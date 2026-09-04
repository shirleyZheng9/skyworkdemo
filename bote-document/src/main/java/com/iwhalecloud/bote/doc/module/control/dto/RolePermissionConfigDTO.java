package com.iwhalecloud.bote.doc.module.control.dto;

import com.iwhalecloud.bote.doc.module.control.vo.NodePermissionView;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 角色权限配置DTO
 *
 * <p>包含系统中所有角色的权限配置信息，供前端统一使用</p>
 *
 * @author Aiqing
 * @since 2025-08-25
 */
@Getter
@Setter
@ToString
@Schema(description = "角色权限配置")
public class RolePermissionConfigDTO {

  @Schema(description = "角色权限映射", example = "{ \"MANAGER\": {...}, \"EDITOR\": {...} }")
  private Map<String, NodePermissionView> rolePermissions;

  @Schema(description = "权限配置版本", example = "v1.0")
  private String version;

  @Schema(description = "配置生成时间戳", example = "1692960000000")
  private Long timestamp;

  @Schema(description = "空间租户id")
  private Long spaceTenantId;
}
