package com.iwhalecloud.bote.dto.organization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 组织用户信息 DTO
 *
 * @author wangtingyun
 * @since 2025-09-17
 */
@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class OrganizationUserDTO {

  @Schema(description = "当前组织ID")
  private Long orgId;
  @Schema(description = "当前组织名称")
  private String orgName;
  @Schema(description = "组织路径")
  private String orgPath;
  @Schema(description = "用户ID")
  private Long userId;
  @Schema(description = "用户名称")
  private String realName;
  @Schema(description = "组织成员数量")
  private Integer memberCount;
  @Schema(description = "组织级别")
  private Integer orgLevel;
  @Schema(description = "子组织")
  private List<OrganizationUserDTO> children;
  @Schema(description = "组织下的用户列表")
  private List<OrganizationUserDTO> orgUserList;

  public void addOrgUserList(List<OrganizationUserDTO> newOrgUserList) {
    if (CollectionUtils.isEmpty(newOrgUserList)) {
      return;
    }
    if (orgUserList == null) {
      orgUserList = new ArrayList<>();
    }
    orgUserList.addAll(newOrgUserList);
  }

}
