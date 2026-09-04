package com.iwhalecloud.bote.doc.module.user.dto;

import com.iwhalecloud.bote.doc.common.model.OrgDTO;
import com.iwhalecloud.bote.doc.common.model.PortalUserDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collections;
import java.util.List;
import lombok.Data;

/**
 * 组织层级信息，含用户
 *
 * @author Aiqing
 * @since 2025-09-04
 */
@Data
public class OrgUserDTO {
  @Schema(description = "当前部门包含的人员")
  private List<PortalUserDTO> userInfos;
  @Schema(description = "当前部门包含的部门")
  private List<OrgDTO> orgInfos;
  @Schema(description = "当前部门路径")
  private List<OrgDTO> pathOrgs;

  public static OrgUserDTO empty() {
    OrgUserDTO userDTO = new OrgUserDTO();
    userDTO.setUserInfos(Collections.emptyList());
    userDTO.setOrgInfos(Collections.emptyList());
    userDTO.setPathOrgs(Collections.emptyList());
    return userDTO;
  }
}
