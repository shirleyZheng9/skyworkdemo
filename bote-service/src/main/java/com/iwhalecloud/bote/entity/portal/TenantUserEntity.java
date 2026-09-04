package com.iwhalecloud.bote.entity.portal;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 租户成员 Entity
 *
 * @author auto
 * @since 2024-09-13
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_tenant_user")
public class TenantUserEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键")
  private Long tenantUserId;
  @DiffField(name = "USER_ID", parent = true)
  @Schema(description = "用户 ID")
  private Long userId;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户 ID")
  private Long tenantId;
  @DiffField(name = "USER_ROLE")
  @Schema(description = "用户角色")
  private String userRole;
}
