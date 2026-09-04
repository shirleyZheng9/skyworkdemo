package com.iwhalecloud.bote.doc.module.document.entity;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Id;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档权限
 *
 * @author yangran
 * @since 2025-08-13
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_dc_document_permission")
@Schema(hidden = true)
public class DocumentPermissionEntity extends BaseEntity {
  @Id
  @DiffId
  @Schema(description = "权限ID")
  private Long permissionId;
  @DiffField(name = "document_id")
  @Schema(description = "文档ID")
  private String documentId;
  @DiffField(name = "subject_type")
  @Schema(description = "主体类型：USER-用户，ORG-部门，ROLE-角色，GROUP-分组")
  private String subjectType;
  @DiffField(name = "subject_id")
  @Schema(description = "主体ID（用户ID、部门ID、角色ID、分组ID）")
  private Long subjectId;
  @DiffField(name = "permission_type")
  @Schema(description = "权限类型：MANAGE-可管理，EDIT-可编辑，DOWNLOAD-可查看和下载，READ-可查看")
  private String permissionType;
  @DiffField(name = "granted_by")
  @Schema(description = "授权人ID")
  private Long grantedBy;
  @DiffField(name = "expires_at")
  @Schema(description = "权限过期时间")
  private Date expiresAt;

  @DiffField(name = "tenant_id")
  @Schema(description = "租户ID")
  private Long tenantId;

  @DiffField(name = "inherit_convert")
  @Schema(description = "是否是继承转换数据")
  private String inheritConvert;
}
