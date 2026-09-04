package com.iwhalecloud.bote.dto.organization.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 组织成员查询参数
 *
 * @author zhao.xu104
 * @since 2025-09-01
 */
@Getter
@Setter
@ToString
@Schema(description = "组织成员查询参数")
public class OrganizationMemberQueryParams {
  @Schema(description = "企业空间ID")
  private Long spaceId;
  @Schema(description = "用户ID")
  private Long userId;
  @Schema(description = "用户名")
  private String userName;
  @Schema(description = "用户姓名")
  private String realName;
  @Schema(description = "组织ID")
  private Long orgId;
  @Schema(description = "成员角色")
  private String memberRole;
  @Schema(description = "成员类型")
  private String memberType;
  @Schema(description = "状态")
  private String statusCd;
  @Schema(description = "页码")
  private Integer pageNum;
  @Schema(description = "每页大小")
  private Integer pageSize;
}
