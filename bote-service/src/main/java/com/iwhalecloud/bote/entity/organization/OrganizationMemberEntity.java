package com.iwhalecloud.bote.entity.organization;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * 组织成员 Entity
 *
 * @author zhao.xu104
 * @since 2025-09-01
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_organization_member")
public class OrganizationMemberEntity extends BaseEntity {
  @DiffId
  @Schema(description = "成员ID")
  private Long memberId;

  @DiffField(name = "USER_ID")
  @Schema(description = "用户ID")
  private Long userId;

  @DiffField(name = "ORG_ID")
  @Schema(description = "组织ID")
  private Long orgId;

  @DiffField(name = "SPACE_ID")
  @Schema(description = "企业空间ID")
  private Long spaceId;

  @DiffField(name = "MEMBER_ROLE")
  @Schema(description = "成员角色：admin/member/guest")
  private String memberRole;

  @DiffField(name = "MEMBER_TYPE")
  @Schema(description = "成员类型：regular/temporary")
  private String memberType;

  @DiffField(name = "JOIN_DATE")
  @Schema(description = "加入日期")
  @JsonFormat(pattern = "yyyy-MM-dd", locale = "zh", timezone = "GMT+8")
  private Date joinDate;

  @DiffField(name = "LEAVE_DATE")
  @Schema(description = "离职日期")
  @JsonFormat(pattern = "yyyy-MM-dd", locale = "zh", timezone = "GMT+8")
  private Date leaveDate;

  @DiffField(name = "DIRECT_MANAGER_ID")
  @Schema(description = "直接上级用户ID")
  private Long directManagerId;

  @DiffField(name = "STATUS_CD")
  @Schema(description = "状态：00A/00X")
  private String statusCd;

  @DiffField(name = "PERMISSIONS")
  @Schema(description = "权限列表（JSON格式）")
  private String permissions;

  @DiffField(name = "EXT_FIELDS")
  @Schema(description = "扩展字段（JSON格式）")
  private String extFields;
}
