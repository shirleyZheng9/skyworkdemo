package com.iwhalecloud.bote.doc.module.library.dto;

import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档库批量权限设置请求
 *
 * @author lizuyin
 * @since 2025-08-28
 */
@Getter
@Setter
@ToString
@Schema(description = "文档库批量权限设置请求")
public class LibraryBatchPermissionRequestDTO extends TenantBaseRO {

  @NotBlank(message = "操作类型不能为空")
  @Schema(description = "操作类型：BATCH_ADD-批量添加，BATCH_UPDATE-批量更新，BATCH_DELETE-批量删除")
  private String action;
  @NotNull(message = "权限成员列表不能为空")
  @NotEmpty(message = "权限成员列表不能为空")
  @Valid
  @Schema(description = "权限成员列表")
  private List<PermissionMemberDTO> members;

  @Getter
  @Setter
  @ToString
  @Schema(description = "权限成员数据")
  public static class PermissionMemberDTO {

    @NotBlank(message = "主体类型不能为空")
    @Schema(description = "主体类型：USER-用户，ORG-部门，ROLE-角色，GROUP-分组")
    private String subjectType;
    @NotNull(message = "主体ID不能为空")
    @Schema(description = "主体ID（用户ID、部门ID、角色ID、分组ID）")
    private Long subjectId;
    @Schema(description = "权限类型：MANAGE-可管理，EDIT-可编辑，READ-只读")
    private String permissionType;
  }
}

