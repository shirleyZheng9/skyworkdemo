package com.iwhalecloud.bote.doc.module.knowledge.dto.query;

import com.iwhalecloud.bote.doc.common.model.PageParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 知识库权限表查询参数
 *
 * @author linmengfan
 * @since 2025-08-23
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "知识库权限表查询参数")
public class BtDcKbPermissionQueryParams extends PageParams {


  @Schema(description = "知识库ID")
  private Long kbId;

  @Schema(description = "主体类型：USER-用户，DEPT-部门，ROLE-角色，GROUP-分组")
  private String subjectType;

  @Schema(description = "主体ID（用户ID、部门ID、角色ID、分组ID）")
  private Long subjectId;

  @Schema(description = "权限类型：MANAGE-可管理，EDIT-可编辑，READ-只读")
  private String permissionType;

  @Schema(description = "租户标识")
  private Long tenantId;
}
