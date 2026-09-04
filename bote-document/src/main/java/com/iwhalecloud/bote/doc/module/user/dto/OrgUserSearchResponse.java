package com.iwhalecloud.bote.doc.module.user.dto;

import com.iwhalecloud.bote.doc.common.model.OrgDTO;
import com.iwhalecloud.bote.doc.common.model.PortalUserDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 组织和用户搜索结果
 *
 * @author lizuyin
 * @since 2025/10/10
 */
@Getter
@Setter
@ToString
public class OrgUserSearchResponse {
  @Schema(description = "用户列表")
  private List<PortalUserDTO> users;

  @Schema(description = "组织列表")
  private List<OrgDTO> orgs;


  public static OrgUserSearchResponse empty() {
    OrgUserSearchResponse response = new OrgUserSearchResponse();
    response.setUsers(Collections.emptyList());
    response.setOrgs(Collections.emptyList());
    return response;
  }
}


