package com.iwhalecloud.bote.dto.organization;

import com.iwhalecloud.bote.dto.portal.UserDTO;
import com.iwhalecloud.bote.entity.organization.OrganizationMemberEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 组织成员信息
 *
 * @author zhao.xu104
 * @since 2025-09-01
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "组织成员信息")
public class OrganizationMemberDTO extends OrganizationMemberEntity {
  @Schema(description = "权限列表")
  private List<String> permissionList;
  @Schema(description = "扩展字段")
  private Map<String, Object> extFieldsMap;
  @Schema(description = "用户信息")
  private UserDTO userInfo;
  @Schema(description = "直接上级用户信息")
  private UserDTO directManagerInfo;
  @Schema(description = "组织信息")
  private SimpleOrganizationDTO organizationInfo;
}
