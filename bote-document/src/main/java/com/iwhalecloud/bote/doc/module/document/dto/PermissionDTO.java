package com.iwhalecloud.bote.doc.module.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 权限数据入参
 *
 * @author Aiqing
 * @since 2025/9/5
 */
@Getter
@Setter
@ToString
public class PermissionDTO {

  @Schema(description = "主体类型：USER-用户，ORG-部门，ROLE-角色，GROUP-分组")
  @NotEmpty(message = "授权主体类型不能为空")
  private String subjectType;
  @Schema(description = "授权主体ID")
  @NotNull(message = "授权主体ID不能为空")
  private Long subjectId;
  @Schema(description = "权限类型：MANAGE-可管理，EDIT-可编辑，DOWNLOAD-可查看和下载，READ-可查看")
  private String permissionType;
}
