package com.iwhalecloud.bote.dto.organization.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 组织用户角色查询参数
 *
 * @author tingyun.wang
 * @since 2025-10-30
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "组织用户角色查询参数")
public class OrgUserRoleQuery extends PagingQueryParams {

  @Schema(description = "用户名")
  private String userName;
  @Schema(description = "组织用户角色")
  private String orgRole;
  @Schema(description = "工作空间ID")
  private Long spaceId;

}
