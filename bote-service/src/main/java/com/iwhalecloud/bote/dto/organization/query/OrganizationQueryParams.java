package com.iwhalecloud.bote.dto.organization.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 组织查询参数
 *
 * @author zhao.xu104
 * @since 2025-09-01
 */
@Getter
@Setter
@ToString
@Schema(description = "组织查询参数")
public class OrganizationQueryParams {
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "企业空间ID")
  private Long spaceId;
  @Schema(description = "组织编码")
  private String orgCode;
  @Schema(description = "组织名称")
  private String orgName;
  @Schema(description = "组织类型")
  private String orgType;
  @Schema(description = "上级组织ID")
  private Long parentOrgId;
  @Schema(description = "状态")
  private String statusCd;
  @Schema(description = "页码")
  private Integer pageNum;
  @Schema(description = "每页大小")
  private Integer pageSize;
}
