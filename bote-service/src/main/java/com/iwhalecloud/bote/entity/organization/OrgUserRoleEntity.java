package com.iwhalecloud.bote.entity.organization;

import com.cmsr.tiangong.diffc.annotations.DiffField;
import com.cmsr.tiangong.diffc.annotations.DiffId;
import com.cmsr.tiangong.diffc.annotations.DiffNode;
import com.cmsr.tiangong.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 组织用户角色 Entity
 *
 * @author tingyun.wang
 * @since 2025-10-30
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_org_user_role")
public class OrgUserRoleEntity extends BaseEntity {

  @DiffId
  @Schema(description = "主键ID")
  private Long orgRoleId;

  @DiffField(name = "USER_ID")
  @Schema(description = "用户ID")
  private Long userId;

  @DiffField(name = "ORG_ROLE")
  @Schema(description = "组织角色")
  private String orgRole;

  @DiffField(name = "SPACE_ID")
  @Schema(description = "工作空间ID")
  private Long spaceId;

}
