package com.iwhalecloud.bote.doc.common.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Comparator;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 用户信息
 *
 * @author Aiqing
 * @since 2025/8/18
 */
@Getter
@Setter
@ToString
public class PortalUserDTO {

  @Schema(description = "用户ID")
  private Long userId;
  @Schema(description = "用户编码或者用户名")
  private String userCode;
  @Schema(description = "用户展示名称")
  private String userName;
  @Schema(description = "用户所属组织信息")
  private List<OrgDTO> orgList;

  /**
   * 获取用户的最大级别组织
   *
   * @return 组织信息
   */
  public OrgDTO getMaxLevelOrg() {
    if (orgList == null || orgList.isEmpty()) {
      return null;
    }
    return this.orgList.stream()
      .max(Comparator.comparingInt(OrgDTO::getOrgLevel)
        .thenComparing(OrgDTO::getOrgId))
      .orElse(null);
  }
}
