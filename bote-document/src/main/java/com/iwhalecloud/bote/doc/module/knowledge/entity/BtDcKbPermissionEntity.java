package com.iwhalecloud.bote.doc.module.knowledge.entity;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 知识库权限表 Entity
 *
 * @author linmengfan
 * @since 2025-08-23
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "BT_DC_KB_PERMISSION")
public class BtDcKbPermissionEntity extends BaseEntity {

  @DiffId
  @Schema(description = "主键")
  private Long permissionId;
  @DiffField(name = "KB_ID")
  @Schema(description = "知识库ID")
  private Long kbId;
  @DiffField(name = "SUBJECT_TYPE")
  @Schema(description = "主体类型：USER-用户，DEPT-部门，ROLE-角色，GROUP-分组")
  private String subjectType;
  @DiffField(name = "SUBJECT_ID")
  @Schema(description = "主体ID（用户ID、部门ID、角色ID、分组ID）")
  private Long subjectId;
  @DiffField(name = "PERMISSION_TYPE")
  @Schema(description = "权限类型：MANAGE-可管理，EDIT-可编辑，READ-只读")
  private String permissionType;
  @DiffField(name = "GRANTED_BY")
  @Schema(description = "授权人ID")
  private Long grantedBy;
  @DiffField(name = "EXPIRES_AT")
  @Schema(description = "权限过期时间")
  private Date expiresAt;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户 ID")
  private Long tenantId;
}
