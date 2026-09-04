package com.iwhalecloud.bote.dto.organization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.dto.portal.UserDTO;
import com.iwhalecloud.bote.entity.organization.OrgUserRoleEntity;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 组织用户角色 DTO
 *
 * @author tingyun.wang
 * @since 2025-10-30
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_org_user_role")
@JsonInclude(Include.NON_NULL)
public class OrgUserRoleDTO extends OrgUserRoleEntity {

  @Schema(description = "用户所属的全部组织名称")
  private String userOrgNames;
  @Schema(description = "用户名")
  private String userName;
  @Schema(description = "用户姓名")
  private String realName;
  @Schema(description = "手机号")
  private String phoneNo;
  @Schema(description = "添加角色的用户列表")
  private List<UserDTO> addRoleUserList;
  @Schema(description = "移除角色的用户ID列表")
  private List<Long> removeRoleUserIds;

}
