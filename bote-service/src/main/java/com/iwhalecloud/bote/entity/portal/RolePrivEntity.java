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
 * 角色权限 Entity
 *
 * @author auto
 * @since 2024-10-14
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_role_priv")
public class RolePrivEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键")
  private Long relaId;
  @DiffField(name = "PRIV_ID")
  @Schema(description = "权限 ID")
  private Long privId;
  @DiffField(name = "ROLE_CODE")
  @Schema(description = "角色编码")
  private String roleCode;
}
